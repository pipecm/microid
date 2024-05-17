package com.simplyfelipe.microid.mapper;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.simplyfelipe.microid.entity.RoleName.UNDEFINED;

@Component
@AllArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User map(UserDto userDto) {
        User user = new User(userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()), buildRoleList(userDto));
        user.setActive(userDto.isActive());
        user.setCreatedOn(userDto.getCreatedOn());
        user.setLastUpdatedOn(userDto.getLastUpdatedOn());
        user.setLastLogin(userDto.getLastLogin());
        return user;
    }

    public UserDto map(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .active(user.isActive())
                .createdOn(user.getCreatedOn())
                .lastUpdatedOn(user.getLastUpdatedOn())
                .lastLogin(user.getLastLogin())
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .admin(user.getRoles().stream().map(Role::getRoleName).anyMatch(RoleName.ADMIN::equals))
                .build();
    }

    private List<Role> buildRoleList(UserDto userDto) {
        List<Role> defaultRoles = new ArrayList<>();

        if (userDto.getRoles().stream().noneMatch(RoleName.USER::equals)) {
            defaultRoles.add(new Role(RoleName.USER));
        }

        if (userDto.isAdmin() && userDto.getRoles().stream().noneMatch(RoleName.ADMIN::equals)) {
            defaultRoles.add(new Role(RoleName.ADMIN));
        }

        return Stream
                .concat(defaultRoles.stream(), userDto.getRoles().stream().map(Role::new))
                .filter(role -> !UNDEFINED.equals(role.getRoleName()))
                .distinct()
                .toList();
    }
}
