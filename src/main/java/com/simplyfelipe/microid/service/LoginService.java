package com.simplyfelipe.microid.service;

import com.simplyfelipe.microid.request.LoginRequest;
import com.simplyfelipe.microid.response.LoginResponse;

public interface LoginService {
    LoginResponse login(LoginRequest loginRequest);
}
