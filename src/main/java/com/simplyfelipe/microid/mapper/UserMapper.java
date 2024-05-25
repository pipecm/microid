package com.simplyfelipe.microid.mapper;

import com.simplyfelipe.microid.dto.UserDto;
import com.simplyfelipe.microid.entity.Login;
import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.simplyfelipe.microid.util.RoleUtil.buildRoleList;

@Component
@AllArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public User map(UserDto userDto) {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        List<RoleName> roles = Optional.ofNullable(userDto).map(UserDto::getRoles).orElse(Collections.emptyList());
        User user = new User(userDto.getEmail(), passwordEncoder.encode(userDto.getPassword()), buildRoleList(roles));
        user.setActive(Optional.of(userDto).map(UserDto::getActive).orElse(true));
        user.setCreatedOn(Optional.of(userDto).map(UserDto::getCreatedOn).map(time -> time.truncatedTo(ChronoUnit.MICROS)).orElse(now));
        user.setLastUpdatedOn(now);
        return user;
    }

    public UserDto map(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .active(user.getActive())
                .createdOn(user.getCreatedOn().truncatedTo(ChronoUnit.MICROS))
                .lastUpdatedOn(user.getLastUpdatedOn().truncatedTo(ChronoUnit.MICROS))
                .roles(user.getRoles().stream().map(Role::getRoleName).toList())
                .lastLogin(processLoginHistory(user))
                .build();
    }

    private LocalDateTime processLoginHistory(User user) {
        return Optional.ofNullable(user)
                .map(User::getLoginHistory)
                .stream()
                .flatMap(Collection::stream)
                .map(Login::getLoginAt)
                .max(LocalDateTime::compareTo)
                .map(time -> time.truncatedTo(ChronoUnit.MICROS))
                .orElse(null);
    }
}
