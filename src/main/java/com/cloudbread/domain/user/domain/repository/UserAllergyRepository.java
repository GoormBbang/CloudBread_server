package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.UserAllergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserAllergyRepository extends JpaRepository<UserAllergy, Long> {

    @Query("select ua from UserAllergy ua where ua.user.id = :userId")// userId 기준으로 해당 유저의 알레르기 조회
    List<UserAllergy> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from UserAllergy ua where ua.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}