package com.simplyfelipe.microid.controller;

import com.simplyfelipe.microid.request.LoginRequest;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.springframework.http.ResponseEntity;

public interface LoginController {
    ResponseEntity<ServiceResponse<?>> login(LoginRequest loginRequest);
}
