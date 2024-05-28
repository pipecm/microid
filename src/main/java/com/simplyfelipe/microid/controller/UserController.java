package com.simplyfelipe.microid.controller;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.response.ServiceResponse;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

public interface UserController {
    ResponseEntity<ServiceResponse<Object>> findUsers(String email, Boolean active, String role);
    ResponseEntity<ServiceResponse<Object>> createUser(UserDto userDto);
    ResponseEntity<ServiceResponse<Object>> updateUser(UUID id, UserDto userDto);
    ResponseEntity<ServiceResponse<Object>> deactivateUser(UUID id);
}
