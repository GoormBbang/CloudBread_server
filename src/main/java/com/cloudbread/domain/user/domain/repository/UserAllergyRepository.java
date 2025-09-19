package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.UserAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {
    @Modifying
    @Query("delete from UserAllergy ua where ua.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
