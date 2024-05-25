package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.repository.RoleRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private RoleServiceImpl roleService;

    @Test
    void whenProcessRolesThenReturnProcessedRoles() {
        Role userRole = new Role(RoleName.USER);
        Role adminRole = new Role(RoleName.ADMIN);
        List<Role> unprocessedRoles = List.of(userRole, adminRole);

        when(roleRepository.findByNameIgnoreCase(RoleName.USER.name())).thenReturn(Optional.of(userRole));
        when(roleRepository.findByNameIgnoreCase(RoleName.ADMIN.name())).thenReturn(Optional.empty());
        when(roleRepository.save(adminRole)).thenReturn(adminRole);

        List<Role> processedRoles = roleService.processRoles(unprocessedRoles);

        verify(roleRepository).findByNameIgnoreCase(RoleName.USER.name());
        verify(roleRepository).findByNameIgnoreCase(RoleName.ADMIN.name());
        verify(roleRepository).save(adminRole);

        assertThat(processedRoles).isEqualTo(unprocessedRoles);
    }
}