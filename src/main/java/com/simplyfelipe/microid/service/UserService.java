package com.simplyfelipe.microid.service;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;
import java.util.UUID;

public interface UserService extends UserDetailsService {
    List<UserDto> findUsers(FindUsersFilters filters);
    User findByEmail(String email);
    UserDto createUser(UserDto userDto);
    UserDto updateUser(UUID id, UserDto userDto);
    void deactivateUser(UUID id);
}
