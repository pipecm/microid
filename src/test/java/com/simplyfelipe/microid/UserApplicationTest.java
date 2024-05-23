package com.simplyfelipe.microid;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.mapper.UserMapper;
import com.simplyfelipe.microid.repository.UserRepository;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
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
    private static final String USER_ALREADY_EXISTS_MSG = "User user@mail.com already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User user@mail.com does not exist";
    private static final String USER_DEACTIVATED_OK = "User with ID %s deactivated successfully";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private MockMvc mockMvc;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void whenCreatingNonExistingUserThenUserIsCreatedOK() throws Exception {

        UserDto request = UserDto.builder().email(USER_EMAIL).password(USER_PASSWORD).build();
        ServiceResponse<UserDto> response = objectMapper.readValue(
                this.mockMvc
                        .perform(post(USERS_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request)))
                        .andDo(print())
                        .andExpect(status().isCreated())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        User userSaved = userRepository.findByEmailIgnoreCase(USER_EMAIL).orElse(new User());

        assertThat(response.getCode()).isEqualTo(HttpStatus.CREATED.value());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.CREATED.name());
        assertThat(userMapper.map(userSaved)).isEqualTo(response.getBody());
    }
}
