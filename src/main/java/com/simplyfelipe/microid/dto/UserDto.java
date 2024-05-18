package com.simplyfelipe.microid.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.simplyfelipe.microid.entity.RoleName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserDto {
    private UUID id;
    private String email;
    private String password;
    private Boolean active;
    private LocalDateTime createdOn;
    private LocalDateTime lastUpdatedOn;
    private List<RoleName> roles;
    private LocalDateTime lastLogin;
}
