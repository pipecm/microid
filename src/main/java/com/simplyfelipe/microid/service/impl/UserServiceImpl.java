package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import com.simplyfelipe.microid.mapper.UserMapper;
import com.simplyfelipe.microid.repository.RoleRepository;
import com.simplyfelipe.microid.repository.UserRepository;
import com.simplyfelipe.microid.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_ALREADY_EXISTS_MSG = "User %s already exists";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findUsers(FindUsersFilters filters) {
        return userRepository.findAll(filters.getSpecifications())
                .stream()
                .map(userMapper::map)
                .toList();
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userRepository.findByEmailIgnoreCase(userDto.getEmail()).ifPresent(this::userAlreadyExists);
        User newUser = userMapper.map(userDto);
        newUser.setRoles(processRoles(newUser.getRoles()));
        return userMapper.map(userRepository.save(newUser));
    }

    @Override
    public UserDto updateUser() {
        return null;
    }

    @Override
    public UserDto deactivateUser() {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    private void userAlreadyExists(User user) {
        throw new ServiceException(HttpStatus.CONFLICT, String.format(USER_ALREADY_EXISTS_MSG, user.getEmail()));
    }

    private List<Role> processRoles(List<Role> roles) {
        return roles.stream()
                .map(role -> roleRepository.findByNameIgnoreCase(role.getName()).orElseGet(() -> saveRole(role)))
                .toList();
    }

    private Role saveRole(Role role) {
        return roleRepository.save(role);
    }
}
