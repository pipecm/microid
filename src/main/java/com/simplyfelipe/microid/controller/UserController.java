package com.simplyfelipe.microid.controller;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface UserController {
    ResponseEntity<ServiceResponse<?>> findUsers(String email, Boolean active, String role);
    ResponseEntity<ServiceResponse<?>> createUser(UserDto userDto);
    ResponseEntity<ServiceResponse<?>> updateUser(UUID id, UserDto userDto);
    ResponseEntity<ServiceResponse<?>> deactivateUser(UUID id);
}
