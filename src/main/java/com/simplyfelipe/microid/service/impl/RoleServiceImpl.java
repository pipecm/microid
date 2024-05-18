package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.repository.RoleRepository;
import com.simplyfelipe.microid.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> processRoles(List<Role> roles) {
        return roles.stream()
                .map(role -> roleRepository.findByNameIgnoreCase(role.getName()).orElseGet(() -> saveRole(role)))
                .toList();
    }

    private Role saveRole(Role role) {
        return roleRepository.save(role);
    }
}
