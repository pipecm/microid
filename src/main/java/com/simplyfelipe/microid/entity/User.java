package com.simplyfelipe.microid.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Getter
@Setter
@Table(name = "mid_user")
public class User extends org.springframework.security.core.userdetails.User {
    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column
    private String email;

    @Column
    private String password;

    @Column(name = "is_active")
    private boolean active;

    @Column(name = "created_on")
    private LocalDate createdOn;

    @Column(name = "last_update")
    private LocalDate lastUpdatedOn;

    @Column(name = "last_login")
    private LocalDate lastLogin;

    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinTable(
            name = "mid_user_role",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id"))
    private List<Role> roles;

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
