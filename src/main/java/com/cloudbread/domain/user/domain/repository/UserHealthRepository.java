package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.UserHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserHealthRepository extends JpaRepository<UserHealth, Long> {
    @Modifying
    @Query("delete from UserHealth uh where uh.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
