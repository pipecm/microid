package com.simplyfelipe.microid.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "mid_user")
public class User extends org.springframework.security.core.userdetails.User {
    private static final String DEFAULT_USERNAME = "default_user";
    private static final String DEFAULT_PASSWORD = "default_password";
    private static final List<Role> DEFAULT_ROLES = Collections.emptyList();

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String email;

    @Column
    private String password;

    @Column(name = "is_active")
    private Boolean active;

    @Column(name = "created_on")
    private LocalDateTime createdOn;

    @Column(name = "last_update")
    private LocalDateTime lastUpdatedOn;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "mid_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

    @OneToMany(mappedBy = "user")
    private List<Login> loginHistory;

    public User() {
        super(DEFAULT_USERNAME, DEFAULT_PASSWORD, DEFAULT_ROLES);
    }

    public User(String email, String password, List<Role> roles) {
        super(email, password, roles);
        this.email = email;
        this.password = password;
        this.roles = roles;
    }

    @Override
    public String getUsername() {
        return this.email;
    }
}
