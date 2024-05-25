package com.simplyfelipe.microid.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simplyfelipe.microid.BaseTest;
import com.simplyfelipe.microid.config.AuthenticationConfiguration;
import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import com.simplyfelipe.microid.response.ServiceResponse;
import com.simplyfelipe.microid.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserControllerImpl.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import(AuthenticationConfiguration.class)
class UserControllerImplTest extends BaseTest {
    private static final String USER_LIST_DTO_RESPONSE_PATH = "src/test/resources/responses/user_dto_list_response.json";
    private static final UUID USER_ID = UUID.fromString("508c9366-2cc2-4250-a88e-cbc1e2e74883");
    private static final String USERS_ENDPOINT = "/users";
    private static final String USERS_ID_PATH = "/{id}";
    private static final String USERS_FILTER_PATH = "?active=%s&role=%s";
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "12345";
    private static final String USER_ALREADY_EXISTS_MSG = "User user@mail.com already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User user@mail.com does not exist";
    private static final String USER_DEACTIVATED_OK = "User with ID %s deactivated successfully";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Test
    void whenSearchWithoutFiltersThenReturnAllUsers() throws Exception {
        List<UserDto> usersFoundDto = readFile(USER_LIST_DTO_RESPONSE_PATH, new TypeReference<>() {});
        when(userService.findUsers(any(FindUsersFilters.class))).thenReturn(usersFoundDto);

        ServiceResponse<List<UserDto>> response = objectMapper.readValue(
                this.mockMvc
                        .perform(get(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        verify(userService).findUsers(any(FindUsersFilters.class));

        assertResponseWithBody(response, usersFoundDto, HttpStatus.OK);
    }

    @ParameterizedTest
    @CsvSource({"true,USER", "true,ADMIN", "false,USER", "false,ADMIN"})
    void whenSearchWithFiltersThenReturnUsersThatFulfillTheFilters(boolean active, RoleName roleName) throws Exception {
        List<UserDto> usersFoundDto = readFile(USER_LIST_DTO_RESPONSE_PATH, new TypeReference<>() {});
        List<UserDto> usersDtoFiltered = usersFoundDto.stream()
                .filter(u -> u.getActive() == active)
                .filter(u -> u.getRoles().stream().anyMatch(r -> r.equals(roleName)))
                .toList();

        when(userService.findUsers(any(FindUsersFilters.class))).thenReturn(usersDtoFiltered);

        ServiceResponse<List<UserDto>> response = objectMapper.readValue(
                this.mockMvc
                        .perform(get(USERS_ENDPOINT + String.format(USERS_FILTER_PATH, active, roleName.value))
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        verify(userService).findUsers(any(FindUsersFilters.class));

        assertResponseWithBody(response, usersDtoFiltered, HttpStatus.OK);
    }

    @Test
    void whenCreatingNonExistingUserThenUserIsCreatedOK() throws Exception {
        UserDto userBefore = UserDto.builder().email(USER_EMAIL).password(USER_PASSWORD).build();
        UserDto userAfter = UserDto.builder().id(USER_ID).email(USER_EMAIL).active(true).build();

        when(userService.createUser(userBefore)).thenReturn(userAfter);

        ServiceResponse<UserDto> response = objectMapper.readValue(
                this.mockMvc
                        .perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {}
        );

        verify(userService).createUser(userBefore);

        assertResponseWithBody(response, userAfter, HttpStatus.CREATED);
    }

    @Test
    void whenCreatingAlreadyExistingUserThenErrorReceived() throws Exception {
        UserDto userBefore = UserDto.builder().email(USER_EMAIL).password(USER_PASSWORD).build();

        when(userService.createUser(userBefore)).thenThrow(new ServiceException(HttpStatus.CONFLICT, USER_ALREADY_EXISTS_MSG));

        ServiceResponse<Void> response = objectMapper.readValue(
                this.mockMvc
                        .perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                        .andExpect(status().isConflict())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {}
        );

        verify(userService).createUser(userBefore);

        assertResponseWithMessage(response, USER_ALREADY_EXISTS_MSG, HttpStatus.CONFLICT);
    }

    @Test
    void whenUpdatingExistingUserThenUserIsUpdatedOK() throws Exception {
        UserDto userBefore = UserDto.builder().id(USER_ID).email(USER_EMAIL).password(USER_PASSWORD).active(true).build();
        UserDto userAfter = UserDto.builder().id(USER_ID).email(USER_EMAIL).password(null).active(true).build();

        when(userService.updateUser(USER_ID, userBefore)).thenReturn(userAfter);

        ServiceResponse<UserDto> response = objectMapper.readValue(
                this.mockMvc
                        .perform(put(USERS_ENDPOINT + USERS_ID_PATH, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        verify(userService).updateUser(USER_ID, userBefore);

        assertResponseWithBody(response, userAfter, HttpStatus.OK);
    }

    @Test
    void whenUpdatingNonExistingUserThenErrorReceived() throws Exception {
        UserDto userBefore = UserDto.builder().id(USER_ID).email(USER_EMAIL).password(USER_PASSWORD).active(true).build();

        when(userService.updateUser(USER_ID, userBefore)).thenThrow(new UsernameNotFoundException(USER_DOES_NOT_EXIST_MSG));

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

        verify(userService).updateUser(USER_ID, userBefore);

        assertResponseWithMessage(response, USER_DOES_NOT_EXIST_MSG, HttpStatus.NOT_FOUND);
    }

    @Test
    void whenDeactivatingExistingUserThenUserIsDeactivatedOK() throws Exception {

        doNothing().when(userService).deactivateUser(USER_ID);

        ServiceResponse<String> response = objectMapper.readValue(
                this.mockMvc
                        .perform(delete(USERS_ENDPOINT + USERS_ID_PATH, USER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        verify(userService).deactivateUser(USER_ID);

        assertResponseWithMessage(response, String.format(USER_DEACTIVATED_OK, USER_ID), HttpStatus.OK);
    }
}