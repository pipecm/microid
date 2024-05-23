package com.simplyfelipe.microid.util;

import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static com.simplyfelipe.microid.entity.RoleName.UNDEFINED;

public final class RoleUtil {

    private RoleUtil() {}

    public static List<Role> buildRoleList(List<RoleName> roleNames) {
        List<Role> defaultRoles = new ArrayList<>();

        if (ObjectUtils.isEmpty(roleNames) || roleNames.stream().noneMatch(RoleName.USER::equals)) {
            defaultRoles.add(new Role(RoleName.USER));
        }

        return Stream
                .concat(defaultRoles.stream(), roleNames.stream().map(Role::new))
                .filter(role -> !UNDEFINED.equals(role.getRoleName()))
                .distinct()
                .toList();
    }
}
