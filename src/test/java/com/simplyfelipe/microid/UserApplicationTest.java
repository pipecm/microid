package com.simplyfelipe.microid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import com.simplyfelipe.microid.mapper.UserMapper;
import com.simplyfelipe.microid.repository.UserRepository;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.shaded.org.apache.commons.lang3.ObjectUtils;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.simplyfelipe.microid.entity.RoleName.UNDEFINED;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class UserApplicationTest extends ApplicationTest {
    private static final UUID USER_ID = UUID.fromString("508c9366-2cc2-4250-a88e-cbc1e2e74883");
    private static final String USERS_ENDPOINT = "/users";
    private static final String USERS_ID_PATH = "/{id}";
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "12345";
    private static final String EXISTING_USER_EMAIL = "admin@microid.com";
    private static final String EXISTING_USER_PASSWORD = "abcde";
    private static final String USER_ALREADY_EXISTS_MSG = "User admin@microid.com already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User 508c9366-2cc2-4250-a88e-cbc1e2e74883 does not exist";
    private static final String USER_DEACTIVATED_OK = "User with ID %s deactivated successfully";
    private static final String EMAIL_FILTER_PATH = "email=%s";
    private static final String ACTIVE_FILTER_PATH = "active=%s";
    private static final String ROLE_FILTER_PATH = "role=%s";
    private static final String USER_LIST_RESPONSE_PATH = "src/test/resources/responses/user_list_response.json";
    private static final String USER_REPOSITORY_FILTERS_PATH = "/csv/user_repository_filters.csv";
    private static final String NULL_PARAM = "null";
    private static final String EMPTY_STRING = "";
    private static final String QUESTION_MARK = "?";
    private static final String PATH_DELIMITER = "&";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MockMvc mockMvc;

    private List<User> allUsers;

    @BeforeEach
    public void setUp() throws Exception {
        allUsers = userRepository.saveAll(readFile(USER_LIST_RESPONSE_PATH, new TypeReference<>() {}));
    }

    @AfterEach
    public void tearDown() {
        userRepository.deleteAll();
    }

    @ParameterizedTest
    @CsvFileSource(resources = USER_REPOSITORY_FILTERS_PATH)
    void whenSearchWithFiltersThenReturnOnlyUsersThatFulfillTheFilters(String paramEmail,
                                                                       String paramActive,
                                                                       String paramRole,
                                                                       int expectedSize) throws Exception {

        String email = NULL_PARAM.equals(paramEmail) ? null : paramEmail;
        Boolean isActive = NULL_PARAM.equals(paramActive) ? null : Boolean.parseBoolean(paramActive);
        RoleName roleName = NULL_PARAM.equals(paramRole) ? UNDEFINED : RoleName.byName(paramRole);

        ServiceResponse<List<UserDto>> response = objectMapper.readValue(
                this.mockMvc
                        .perform(get(USERS_ENDPOINT + buildUrlPath(email, isActive, roleName))
                                .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        FindUsersFilters filters = FindUsersFilters.builder().email(email).active(isActive).roleName(roleName).build();
        List<UserDto> usersFound = userRepository.findAll(filters.getSpecifications())
                .stream()
                .map(userMapper::map)
                .toList();

        assertResponseWithBodySize(response, usersFound, HttpStatus.OK, expectedSize);
    }

    @Test
    void whenCreatingNonExistingUserThenUserIsCreatedOK() throws Exception {

        UserDto request = UserDto.builder().email(USER_EMAIL).password(USER_PASSWORD).build();
        ServiceResponse<UserDto> response = objectMapper.readValue(
                this.mockMvc
                        .perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        UserDto userDtoSaved = userRepository.findByEmailIgnoreCase(USER_EMAIL)
                .map(userMapper::map)
                .orElse(new UserDto());

        assertResponseWithBody(response, userDtoSaved, HttpStatus.CREATED);
    }

    @Test
    void whenCreatingAlreadyExistingUserThenErrorReceived() throws Exception {
        UserDto request = UserDto.builder().email(EXISTING_USER_EMAIL).password(EXISTING_USER_PASSWORD).build();
        ServiceResponse<Void> response = objectMapper.readValue(
                this.mockMvc
                        .perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                        .andExpect(status().isConflict())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {}
        );

        assertResponseWithMessage(response, USER_ALREADY_EXISTS_MSG, HttpStatus.CONFLICT);
    }

    @Test
    void whenUpdatingExistingUserThenUserIsUpdatedOK() throws Exception {
        UserDto userBefore = userRepository.findByEmailIgnoreCase(EXISTING_USER_EMAIL)
                .map(userMapper::map)
                .orElse(new UserDto());

        userBefore.setRoles(List.of(RoleName.USER));

        ServiceResponse<UserDto> response = objectMapper.readValue(
                this.mockMvc
                        .perform(put(USERS_ENDPOINT + USERS_ID_PATH, userBefore.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        UserDto userDtoUpdated = userRepository.findByEmailIgnoreCase(EXISTING_USER_EMAIL)
                .map(userMapper::map)
                .orElse(new UserDto());

        assertResponseWithBody(response, userDtoUpdated, HttpStatus.OK);
    }

    @Test
    void whenUpdatingNonExistingUserThenErrorReceived() throws Exception {
        UserDto userBefore = UserDto.builder().id(USER_ID).email(USER_EMAIL).password(USER_PASSWORD).active(false).build();

        ServiceResponse<Void> response = objectMapper.readValue(
                this.mockMvc
                        .perform(put(USERS_ENDPOINT + USERS_ID_PATH, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                        .andExpect(status().isNotFound())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {}
        );

        assertResponseWithMessage(response, USER_DOES_NOT_EXIST_MSG, HttpStatus.NOT_FOUND);
    }

    @Test
    void whenDeactivatingExistingUserThenUserIsDeactivatedOK() throws Exception {
        UUID userId = userRepository.findByEmailIgnoreCase(EXISTING_USER_EMAIL)
                .map(User::getId)
                .orElse(UUID.randomUUID());

        ServiceResponse<String> response = objectMapper.readValue(
                this.mockMvc
                        .perform(delete(USERS_ENDPOINT + USERS_ID_PATH, userId)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        assertResponseWithMessage(response, String.format(USER_DEACTIVATED_OK, userId), HttpStatus.OK);
    }

    private String buildUrlPath(String email, Boolean active, RoleName roleName) {
        if (noFilters(email, active, roleName)) {
            return EMPTY_STRING;
        }

        String emailFilter = ObjectUtils.isEmpty(email) ? EMPTY_STRING : String.format(EMAIL_FILTER_PATH, email);
        String activeFilter = ObjectUtils.isEmpty(active) ? EMPTY_STRING : String.format(ACTIVE_FILTER_PATH, active);
        String roleFilter = ObjectUtils.isEmpty(roleName) || UNDEFINED.equals(roleName) ?
                EMPTY_STRING : String.format(ROLE_FILTER_PATH, roleName.value);

        return QUESTION_MARK + Stream.of(emailFilter, activeFilter, roleFilter)
                .filter(ObjectUtils::isNotEmpty)
                .collect(Collectors.joining(PATH_DELIMITER));
    }

    private boolean noFilters(String email, Boolean active, RoleName roleName) {
        return (ObjectUtils.allNull(email, active, roleName) || (ObjectUtils.allNull(email, active) && UNDEFINED.equals(roleName)));
    }
}
