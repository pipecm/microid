package com.simplyfelipe.microid.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.simplyfelipe.microid.config.AuthenticationConfiguration;
import com.simplyfelipe.microid.config.SecurityConfiguration;
import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.jwt.JwtUtil;
import com.simplyfelipe.microid.jwt.JwtUtilConfig;
import com.simplyfelipe.microid.jwt.JwtUtilParameters;
import com.simplyfelipe.microid.response.ServiceResponse;
import com.simplyfelipe.microid.service.UserService;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserControllerImpl.class)
@Import({ AuthenticationConfiguration.class, SecurityConfiguration.class, JwtUtil.class, JwtUtilParameters.class, JwtUtilConfig.class })
class UserControllerImplTest {

    private static final String USERS_ENDPOINT = "/users";
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "12345";
    private static final String USER_ID = "508c9366-2cc2-4250-a88e-cbc1e2e74883";
    private static final String USER_ALREADY_EXISTS_MSG = "User user@mail.com already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User user@mail.com does not exist";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    private static ObjectMapper objectMapper;

    @BeforeAll
    public static void init() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void whenCreatingNonExistingUserThenUserIsCreatedOK() throws Exception {
        UserDto userBefore = UserDto.builder().email(USER_EMAIL).password(USER_PASSWORD).build();
        UserDto userAfter = UserDto.builder().id(UUID.fromString(USER_ID)).email(USER_EMAIL).active(true).build();

        when(userService.createUser(userBefore)).thenReturn(userAfter);

        this.mockMvc
                .perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                .andExpect(status().isCreated());

        verify(userService).createUser(userBefore);
    }

    @Test
    void whenCreatingAlreadyExistingUserThenErrorReceived() throws Exception {
        UserDto userBefore = UserDto.builder().email(USER_EMAIL).password(USER_PASSWORD).build();

        when(userService.createUser(userBefore)).thenThrow(new ServiceException(HttpStatus.CONFLICT, USER_ALREADY_EXISTS_MSG));

        this.mockMvc
                .perform(post(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.name()))
                .andExpect(jsonPath("$.message").value(USER_ALREADY_EXISTS_MSG));

        verify(userService).createUser(userBefore);
    }

    @Test
    void whenUpdatingExistingUserThenUserIsUpdatedOK() throws Exception {
        UserDto userBefore = UserDto.builder().id(UUID.fromString(USER_ID)).email(USER_EMAIL).password(USER_PASSWORD).active(true).build();
        UserDto userAfter = UserDto.builder().id(UUID.fromString(USER_ID)).email(USER_EMAIL).password(null).active(true).build();

        when(userService.updateUser(userBefore)).thenReturn(userAfter);

        ServiceResponse<UserDto> response = objectMapper.readValue(
                this.mockMvc
                        .perform(put(USERS_ENDPOINT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(userBefore)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        verify(userService).updateUser(userBefore);

        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(HttpStatus.OK.value());
        assertThat(response.getStatus()).isEqualTo(HttpStatus.OK.name());
        assertThat(response.getBody()).isEqualTo(userAfter);
    }

    @Test
    void whenUpdatingNonExistingUserThenErrorReceived() throws Exception {
        UserDto userBefore = UserDto.builder().id(UUID.fromString(USER_ID)).email(USER_EMAIL).password(USER_PASSWORD).active(true).build();

        when(userService.updateUser(userBefore)).thenThrow(new UsernameNotFoundException(USER_DOES_NOT_EXIST_MSG));

        this.mockMvc
                .perform(put(USERS_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userBefore)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value(HttpStatus.NOT_FOUND.value()))
                .andExpect(jsonPath("$.status").value(HttpStatus.NOT_FOUND.name()))
                .andExpect(jsonPath("$.message").value(USER_DOES_NOT_EXIST_MSG));

        verify(userService).updateUser(userBefore);
    }
}