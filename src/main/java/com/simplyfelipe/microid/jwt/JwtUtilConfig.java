package com.simplyfelipe.microid.jwt;

import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JwtUtilConfig {

    @Bean
    public JwtParser jwtParser() {
        return Jwts.parser()
                .verifyWith(Keys.hmacShaKeyFor(jwtUtilParameters().getSecretKey().getBytes()))
                .build();
    }

    @Bean
    public JwtUtilParameters jwtUtilParameters() {
        return new JwtUtilParameters();
    }
}
