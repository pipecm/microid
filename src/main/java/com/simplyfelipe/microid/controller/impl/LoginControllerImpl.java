package com.simplyfelipe.microid.controller.impl;

import com.simplyfelipe.microid.controller.LoginController;
import com.simplyfelipe.microid.request.LoginRequest;
import com.simplyfelipe.microid.response.ServiceResponse;
import com.simplyfelipe.microid.service.LoginService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/login")
@AllArgsConstructor
public class LoginControllerImpl implements LoginController {

    private static final String LOGIN_SUCCESSFUL_MSG = "Login successful";

    private final LoginService loginService;

    @Override
    @PostMapping
    public ResponseEntity<ServiceResponse<Object>> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(ServiceResponse.builder()
                .code(HttpStatus.OK.value())
                .status(HttpStatus.OK.name())
                .message(LOGIN_SUCCESSFUL_MSG)
                .body(loginService.login(loginRequest))
                .build());
    }
}
