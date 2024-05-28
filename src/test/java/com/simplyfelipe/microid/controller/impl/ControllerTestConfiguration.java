package com.simplyfelipe.microid.controller.impl;

import com.simplyfelipe.microid.entity.User;
import com.simplyfelipe.microid.jwt.JwtUtil;
import com.simplyfelipe.microid.jwt.JwtUtilConfig;
import com.simplyfelipe.microid.jwt.JwtUtilParameters;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;

import java.util.List;

@TestConfiguration
@Import({JwtUtil.class, JwtUtilConfig.class, JwtUtilParameters.class})
public class ControllerTestConfiguration {
    private static final String USER_EMAIL = "user@mail.com";
    private static final String USER_PASSWORD = "12345";
    private static final String ADMIN_EMAIL = "admin@mail.com";
    private static final String ADMIN_PASSWORD = "abcde";
    private static final String USER_ROLE = "USER";
    private static final String ADMIN_ROLE = "ADMIN";

    @Bean
    @Primary
    public UserDetailsService userDetailsService() {
        UserDetails commonUser = User.builder()
                .username(USER_EMAIL)
                .password(USER_PASSWORD)
                .roles(List.of(USER_ROLE).toArray(new String[0]))
                .authorities(List.of(USER_ROLE).toArray(new String[0]))
                .build();

        UserDetails adminUser = User.builder()
                .username(ADMIN_EMAIL)
                .password(ADMIN_PASSWORD)
                .roles(List.of(USER_ROLE, ADMIN_ROLE).toArray(new String[0]))
                .authorities(List.of(USER_ROLE, ADMIN_ROLE).toArray(new String[0]))
                .build();

        return new InMemoryUserDetailsManager(List.of(commonUser, adminUser));
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}

