package com.simplyfelipe.microid.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class LoginResponse {
    private String email;
    private String token;
    private LocalDateTime expiration;
}
