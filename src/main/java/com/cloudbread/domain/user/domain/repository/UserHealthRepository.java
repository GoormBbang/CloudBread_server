package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.UserHealth;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserHealthRepository extends JpaRepository<UserHealth, Long> {

    @Query("select uh from UserHealth uh where uh.user.id = :userId")
    List<UserHealth> findByUserId(@Param("userId") Long userId);

    @Modifying
    @Query("delete from UserHealth uh where uh.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);

    @Query("""
           select uh
           from UserHealth uh
           join fetch uh.healthType ht
           where uh.user.id = :userId
           """)
    List<UserHealth> findWithHealthTypeByUserId(Long userId);

}
