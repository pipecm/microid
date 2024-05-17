package com.simplyfelipe.microid.filter;

import com.simplyfelipe.microid.entity.Role;
import com.simplyfelipe.microid.entity.RoleName;
import com.simplyfelipe.microid.entity.User;
import jakarta.persistence.criteria.Join;
import lombok.Builder;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static com.simplyfelipe.microid.entity.RoleName.UNDEFINED;

@Builder
public class FindUsersFilters {
    private static final String KEY_EMAIL = "email";
    private static final String KEY_ACTIVE = "active";
    private static final String KEY_ROLES = "roles";
    private static final String KEY_NAME = "name";

    private String email;
    private Boolean active;
    private RoleName roleName;

    public Specification<User> getSpecifications() {

        if (noSpecifications()) {
            return (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder.isTrue(root.isNotNull());
        }

        List<Specification<User>> specificationList = new ArrayList<>();

        if (!ObjectUtils.isEmpty(email)) {
            Specification<User> hasEmail = (root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(criteriaBuilder.upper(root.get(KEY_EMAIL)), email.toUpperCase());
            specificationList.add(hasEmail);
        }

        if (!ObjectUtils.isEmpty(active)) {
            Specification<User> isActive = (root, criteriaQuery, criteriaBuilder) ->
                    criteriaBuilder.equal(root.get(KEY_ACTIVE), active);
            specificationList.add(isActive);
        }

        if (Optional.ofNullable(roleName).map(role -> !UNDEFINED.equals(role)).orElse(false)) {
            Specification<User> hasRole = (root, criteriaQuery, criteriaBuilder) -> {
                Join<User, Role> usersAndRoles = root.join(KEY_ROLES);
                return criteriaBuilder.equal(criteriaBuilder.upper(usersAndRoles.get(KEY_NAME)), roleName.value.toUpperCase());
            };
            specificationList.add(hasRole);
        }

        return Specification.allOf(specificationList);
    }

    private boolean noSpecifications() {
        return (Stream.of(email, active, roleName).allMatch(ObjectUtils::isEmpty) ||
               (Stream.of(email, active).allMatch(ObjectUtils::isEmpty) && UNDEFINED.equals(roleName)));
    }
}
