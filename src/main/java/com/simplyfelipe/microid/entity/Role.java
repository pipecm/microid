package com.simplyfelipe.microid.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import java.util.List;

@Entity
@Data
@Table(name = "mid_role")
public class Role implements GrantedAuthority {

    @Id
    @Column
    private int id;

    @Column
    private String name;

    @Column
    private boolean active;

    @ToString.Exclude
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "roles")
    private List<User> users;

    public Role(RoleName roleName) {
        this.id = roleName.code;
        this.name = roleName.value;
        this.active = true;
    }

    @Override
    public String getAuthority() {
        return name;
    }

    public RoleName getRoleName() {
        return RoleName.byName(name);
    }
}
