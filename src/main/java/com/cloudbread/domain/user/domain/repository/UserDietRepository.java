package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.UserDiet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserDietRepository extends JpaRepository<UserDiet, Long> {

    @Query("select ud from UserDiet ud where ud.user.id = :userId")
    List<UserDiet> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from UserDiet ud where ud.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
