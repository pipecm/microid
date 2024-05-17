package com.simplyfelipe.microid.controller;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.springframework.http.ResponseEntity;

public interface UserController {
    ResponseEntity<ServiceResponse<?>> findUsers(String email, Boolean active, String role);
    ResponseEntity<ServiceResponse<?>> createUser(UserDto userDto);
    ResponseEntity<ServiceResponse<?>> updateUser(UserDto userDto);
    ResponseEntity<ServiceResponse<?>> deactivateUser(UserDto userDto);
}
