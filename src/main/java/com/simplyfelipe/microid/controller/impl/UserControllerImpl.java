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

import java.util.UUID;

@RestController
@RequestMapping("/users")
@AllArgsConstructor
public class UserControllerImpl implements UserController {

    private static final String USER_DEACTIVATED_OK = "User with ID %s deactivated successfully";

    private final UserService userService;

    @Override
    @GetMapping
    public ResponseEntity<ServiceResponse<Object>> findUsers(@RequestParam(required = false) String email,
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
    public ResponseEntity<ServiceResponse<Object>> createUser(@RequestBody UserDto userDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ServiceResponse.builder()
                        .code(HttpStatus.CREATED.value())
                        .status(HttpStatus.CREATED.name())
                        .body(userService.createUser(userDto))
                        .build()
                );
    }

    @Override
    @PutMapping("/{id}")
    public ResponseEntity<ServiceResponse<Object>> updateUser(@PathVariable UUID id, @RequestBody UserDto userDto) {
        return ResponseEntity.ok()
                .body(ServiceResponse.builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK.name())
                        .body(userService.updateUser(id, userDto))
                        .build()
                );
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<ServiceResponse<Object>> deactivateUser(@PathVariable UUID id) {
        userService.deactivateUser(id);
        return ResponseEntity.ok()
                .body(ServiceResponse.builder()
                        .code(HttpStatus.OK.value())
                        .status(HttpStatus.OK.name())
                        .message(String.format(USER_DEACTIVATED_OK, id.toString()))
                        .build()
                );
    }
}
