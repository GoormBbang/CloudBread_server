package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("SELECT u.dueDate FROM User u WHERE u.id = :userId")
    LocalDate findDueDateByUserId(@Param("userId") Long userId);

    @Query("select u from User u where u.activated = true")
    List<User> findAllActivated();

}
