package com.simplyfelipe.microid.mapper;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.Login;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.simplyfelipe.microid.util.RoleUtil.buildRoleList;

@Component
@AllArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User map(UserDto userDto) {
        LocalDateTime now = LocalDateTime.now();
        User user = new User(userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()), buildRoleList(userDto.getRoles()));
        user.setActive(Optional.of(userDto).map(UserDto::getActive).orElse(true));
        user.setCreatedOn(Optional.of(userDto).map(UserDto::getCreatedOn).orElse(now));
        user.setLastUpdatedOn(now);
        return user;
    }

    public UserDto map(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .active(user.getActive())
                .createdOn(user.getCreatedOn())
                .lastUpdatedOn(user.getLastUpdatedOn())
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .lastLogin(user.getLoginHistory().stream().map(Login::getLoginAt).max(LocalDateTime::compareTo).orElse(null))
                .build();
    }
}
