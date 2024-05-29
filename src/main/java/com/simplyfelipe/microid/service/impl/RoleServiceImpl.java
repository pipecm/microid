package com.simplyfelipe.microid.service.impl;

import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.repository.RoleRepository;
import com.simplyfelipe.microid.service.RoleService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@SuppressWarnings("squid:S6204")
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public List<Role> processRoles(List<Role> roles) {
        return roles.stream()
                .map(role -> roleRepository.findByNameIgnoreCase(role.getName()).orElseGet(() -> saveRole(role)))
                .collect(Collectors.toList());
    }

    private Role saveRole(Role role) {
        return roleRepository.save(role);
    }
}
