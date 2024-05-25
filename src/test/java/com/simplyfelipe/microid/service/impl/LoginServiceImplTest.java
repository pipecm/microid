package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.entity.Login;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.jwt.JwtUtil;
import com.simplyfelipe.microid.repository.LoginRepository;
import com.simplyfelipe.microid.request.LoginRequest;
import com.simplyfelipe.microid.response.LoginResponse;
import com.simplyfelipe.microid.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoginServiceImplTest {

    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "12345";
    private static final String LOGIN_TOKEN = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJ1c2VyQGFwcGx5ZGlnaXRhbC5jb20iLCJlbWFpbCI6InVzZXJAYXBwbHlkaWdpdGFsLmNvbSIsImV4cCI6MTkzMTYxNDMzMH0.BYuxQ3MGNpfGMAXxYFcELZgBVpHZgOUybvu28sThGQMnAh-YyrM5nGSn9XU0ked9XRoMJsKlxJTpmNSEz55RYg";
    private static final Date TOKEN_EXPIRATION = Date.from(Instant.now().plusSeconds(900L));
    private static final LocalDateTime LDT_TOKEN_EXPIRATION = TOKEN_EXPIRATION.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    private static final String INVALID_LOGIN_MSG = "Invalid email and/or password";
    private static final String NO_EXCEPTION_THROWN = "No exception thrown";
    private static final UUID LOGIN_ID = UUID.fromString("d568123c-0520-460f-84d9-1b127569978a");

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private LoginRepository loginRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private LoginServiceImpl loginService;

    @Test
    void whenLoginWithCorrectCredentialsThenLoginOK() {
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, USER_PASSWORD);
        UsernamePasswordAuthenticationToken beforeAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD);
        UsernamePasswordAuthenticationToken afterAuth = spy(beforeAuth);
        User user = new User(USER_EMAIL, USER_PASSWORD, List.of(new Role(RoleName.USER)));
        Login login = new Login(user);

        doReturn(USER_EMAIL).when(afterAuth).getName();

        when(authenticationManager.authenticate(beforeAuth)).thenReturn(afterAuth);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        when(loginRepository.save(any(Login.class))).thenReturn(login);
        when(jwtUtil.createToken(user)).thenReturn(LOGIN_TOKEN);
        when(jwtUtil.getExpiration(LOGIN_TOKEN)).thenReturn(TOKEN_EXPIRATION);

        LoginResponse loginResponse = loginService.login(loginRequest);

        verify(authenticationManager).authenticate(beforeAuth);
        verify(userService).findByEmail(USER_EMAIL);
        verify(loginRepository).save(any(Login.class));
        verify(jwtUtil).createToken(user);
        verify(jwtUtil).getExpiration(LOGIN_TOKEN);

        assertThat(loginResponse).isNotNull();
        assertThat(loginResponse.getEmail()).isEqualTo(USER_EMAIL);
        assertThat(loginResponse.getToken()).isEqualTo(LOGIN_TOKEN);
        assertThat(loginResponse.getExpiration()).isEqualTo(LDT_TOKEN_EXPIRATION);
    }

    @Test
    void whenLoginWithWrongCredentialsThenLoginFails() {
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, USER_PASSWORD);
        UsernamePasswordAuthenticationToken beforeAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD);

        when(authenticationManager.authenticate(beforeAuth)).thenThrow(new BadCredentialsException(INVALID_LOGIN_MSG));

        try {
            loginService.login(loginRequest);
            fail(NO_EXCEPTION_THROWN);
        } catch (ServiceException serviceException) {
            assertThat(serviceException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(serviceException.getMessage()).isEqualTo(INVALID_LOGIN_MSG);
        }

        verify(authenticationManager).authenticate(beforeAuth);
    }

    @Test
    void whenGotExceptionAtLoginThenLoginFails() {
        LoginRequest loginRequest = new LoginRequest(USER_EMAIL, USER_PASSWORD);
        UsernamePasswordAuthenticationToken beforeAuth = new UsernamePasswordAuthenticationToken(USER_EMAIL, USER_PASSWORD);
        UsernamePasswordAuthenticationToken afterAuth = spy(beforeAuth);
        User user = new User(USER_EMAIL, USER_PASSWORD, List.of(new Role(RoleName.USER)));
        Login login = new Login(user);
        login.setId(LOGIN_ID);

        when(authenticationManager.authenticate(beforeAuth)).thenReturn(afterAuth);
        when(userService.findByEmail(USER_EMAIL)).thenReturn(user);
        when(loginRepository.save(any(Login.class))).thenReturn(login);
        when(jwtUtil.createToken(user)).thenThrow(new IllegalArgumentException());

        doNothing().when(loginRepository).setLoginFailed(LOGIN_ID);

        try {
            loginService.login(loginRequest);
            fail(NO_EXCEPTION_THROWN);
        } catch (ServiceException serviceException) {
            assertThat(serviceException.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST);
            assertThat(serviceException.getMessage()).isEqualTo(INVALID_LOGIN_MSG);
        }

        verify(authenticationManager).authenticate(beforeAuth);
        verify(userService).findByEmail(USER_EMAIL);
        verify(loginRepository).save(any(Login.class));
        verify(jwtUtil).createToken(user);
        verify(loginRepository).setLoginFailed(LOGIN_ID);
    }
}