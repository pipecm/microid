package com.simplyfelipe.microid.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mid_login_history")
@Data
@NoArgsConstructor
public class Login {

    @Id
    @Column
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    private User user;

    @Column(name = "login_at")
    private LocalDateTime loginAt;

    @Column
    private Boolean success;

    public Login(User user) {
        this.user = user;
        this.loginAt = LocalDateTime.now();
        this.success = true;
    }
}
