package com.simplyfelipe.microid.repository;

import com.simplyfelipe.microid.entity.Login;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface LoginRepository extends JpaRepository<Login, UUID> {
    @Modifying
    @Query("update Login l set l.success = false where l.id = :id")
    void setLoginFailed(UUID id);
}
