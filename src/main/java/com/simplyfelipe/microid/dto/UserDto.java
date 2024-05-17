package com.simplyfelipe.microid.dto;

import com.simplyfelipe.microid.entity.RoleName;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class UserDto {
    private UUID id;
    private String email;
    private String password;
    private boolean active;
    private LocalDate createdOn;
    private LocalDate lastUpdatedOn;
    private LocalDate lastLogin;
    private List<RoleName> roles;
    private boolean admin;
}
