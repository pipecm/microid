package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.entity.Login;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.jwt.JwtUtil;
import com.simplyfelipe.microid.repository.LoginRepository;
import com.simplyfelipe.microid.request.LoginRequest;
import com.simplyfelipe.microid.response.LoginResponse;
import com.simplyfelipe.microid.service.LoginService;
import com.simplyfelipe.microid.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.time.ZoneId;
import java.util.Optional;

@Service
@AllArgsConstructor
public class LoginServiceImpl implements LoginService {

    private static final String INVALID_LOGIN_MSG = "Invalid email and/or password";
    private static final String EMPTY_PASSWORD = "";

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final LoginRepository loginRepository;
    private final UserService userService;

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        Login login = null;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));

            User user = userService.findByEmail(authentication.getName());
            login = loginRepository.save(new Login(user));

            user.setPassword(EMPTY_PASSWORD);
            String token = jwtUtil.createToken(user);

            return LoginResponse.builder()
                    .email(user.getEmail())
                    .token(token)
                    .expiration(jwtUtil.getExpiration(token).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime())
                    .build();

        } catch (IllegalArgumentException | BadCredentialsException e) {
            if (Optional.ofNullable(login).map(Login::getId).map(id -> !ObjectUtils.isEmpty(id)).orElse(false)) {
                loginRepository.setLoginFailed(login.getId());
            }
            throw new ServiceException(HttpStatus.BAD_REQUEST, INVALID_LOGIN_MSG);
        }
    }
}
