package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
    boolean existsByName(String name);
}
