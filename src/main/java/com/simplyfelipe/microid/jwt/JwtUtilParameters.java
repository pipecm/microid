package com.simplyfelipe.microid.jwt;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.security.jwt")
public class JwtUtilParameters {
    private String secretKey = "not-set";
    private Long expiration = 900_000L;
}
