package com.simplyfelipe.microid.controller.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.simplyfelipe.microid.BaseTest;
import com.simplyfelipe.microid.config.AuthenticationConfiguration;
import com.simplyfelipe.microid.request.LoginRequest;
import com.simplyfelipe.microid.response.LoginResponse;
import com.simplyfelipe.microid.response.ServiceResponse;
import com.simplyfelipe.microid.service.LoginService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = LoginControllerImpl.class)
@ContextConfiguration(classes = ControllerTestConfiguration.class)
@Import(AuthenticationConfiguration.class)
@EnableMethodSecurity(proxyTargetClass = true)
class LoginControllerImplTest extends BaseTest {
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "12345";
    private static final String LOGIN_ENDPOINT = "/login";
    private static final String LOGIN_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGFwcGx5ZGlnaXRhbC5jb20iLCJlbWFpbCI6InVzZXJAYXBwbHlkaWdpdGFsLmNvbSIsImV4cCI6MTkzMTYxNDMzMH0.BYuxQ3MGNpfGMAXxYFcELZgBVpHZgOUybvu28sThGQMnAh-YyrM5nGSn9XU0ked9XRoMJsKlxJTpmNSEz55RYg";
    private static final LocalDateTime LDT_TOKEN_EXPIRATION = LocalDateTime.now().plusMinutes(15L).atZone(ZoneId.systemDefault()).toLocalDateTime();

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoginService loginService;

    @Test
    void whenLoginWithCorrectCredentialsThenLoginOK() throws Exception {
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, USER_PASSWORD);
        LoginResponse loginResponse = LoginResponse.builder()
                                                .email(USER_EMAIL)
                                                .token(LOGIN_TOKEN)
                                                .expiration(LDT_TOKEN_EXPIRATION)
                                                .build();

        when(loginService.login(loginRequest)).thenReturn(loginResponse);

        ServiceResponse<LoginResponse> serviceResponse = objectMapper.readValue(
                this.mockMvc
                        .perform(post(LOGIN_ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                        .andExpect(status().isOk())
                        .andReturn()
                        .getResponse()
                        .getContentAsString(),
                new TypeReference<>() {});

        verify(loginService).login(loginRequest);

        assertResponseWithBody(serviceResponse, loginResponse, HttpStatus.OK);
    }
}