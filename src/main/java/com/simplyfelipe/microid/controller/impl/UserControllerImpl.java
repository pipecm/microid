package com.simplyfelipe.microid.controller.impl;

import com.simplyfelipe.microid.controller.UserController;
import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import com.simplyfelipe.microid.response.ServiceResponse;
import com.simplyfelipe.microid.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserControllerImpl implements UserController {

    private final UserService userService;

    @Override
    @GetMapping
    public ResponseEntity<ServiceResponse<?>> findUsers(@RequestParam(required = false) String email,
                                                        @RequestParam(required = false) Boolean active,
                                                        @RequestParam(required = false) String role) {

        return ResponseEntity.ok(
                ServiceResponse.builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK.name())
                        .body(userService.findUsers(
                                FindUsersFilters.builder()
                                        .roleName(RoleName.byName(role))
                                        .email(email)
                                        .active(active)
                                        .build())
                        )
                        .build()
        );
    }

    @Override
    @PostMapping
    public ResponseEntity<ServiceResponse<?>> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ServiceResponse.builder()
                        .code(HttpStatus.CREATED.value())
                        .status(HttpStatus.CREATED.name())
                        .body(userService.createUser(userDto))
                        .build()
                );
    }

    @Override
    public ResponseEntity<ServiceResponse<?>> updateUser(UserDto userDto) {
        return null;
    }

    @Override
    public ResponseEntity<ServiceResponse<?>> deactivateUser(UserDto userDto) {
        return null;
    }
}
