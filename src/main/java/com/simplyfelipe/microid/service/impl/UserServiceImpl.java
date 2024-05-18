package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.exception.ServiceException;
import com.simplyfelipe.microid.filter.FindUsersFilters;
import com.simplyfelipe.microid.mapper.UserMapper;
import com.simplyfelipe.microid.repository.UserRepository;
import com.simplyfelipe.microid.service.RoleService;
import com.simplyfelipe.microid.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

import static com.simplyfelipe.microid.util.RoleUtil.buildRoleList;

@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {
    private static final String USER_ALREADY_EXISTS_MSG = "User %s already exists";
    private static final String USER_DOES_NOT_EXIST_MSG = "User %s does not exist";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final UserMapper userMapper;

    @Override
    public List<UserDto> findUsers(FindUsersFilters filters) {
        return userRepository.findAll(filters.getSpecifications())
                .stream()
                .map(userMapper::map)
                .toList();
    }

    @Override
    public User findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(email).orElseThrow(() -> userDoesNotExist(email));
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        userRepository.findByEmailIgnoreCase(userDto.getEmail()).ifPresent(this::userAlreadyExists);
        User newUser = userMapper.map(userDto);
        newUser.setRoles(roleService.processRoles(newUser.getRoles()));
        return userMapper.map(userRepository.save(newUser));
    }

    @Override
    public UserDto updateUser(UserDto userDto) {
        User found = userRepository
                .findByEmailIgnoreCase(userDto.getEmail())
                .orElseThrow(() -> userDoesNotExist(userDto.getEmail()));

        found.setPassword(userDto.getPassword());
        found.setActive(userDto.getActive());
        found.setRoles(roleService.processRoles(buildRoleList(userDto.getRoles())));
        found.setLastUpdatedOn(LocalDateTime.now());

        return userMapper.map(userRepository.save(found));
    }

    @Override
    public UserDto deactivateUser(String email) {
        User found = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() -> userDoesNotExist(email));

        found.setActive(false);
        found.setLastUpdatedOn(LocalDateTime.now());

        return userMapper.map(userRepository.save(found));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByEmail(username);
    }

    private void userAlreadyExists(User user) {
        throw new ServiceException(HttpStatus.CONFLICT, String.format(USER_ALREADY_EXISTS_MSG, user.getEmail()));
    }

    private UsernameNotFoundException userDoesNotExist(String username) {
        return new UsernameNotFoundException(String.format(USER_DOES_NOT_EXIST_MSG, username));
    }
}
