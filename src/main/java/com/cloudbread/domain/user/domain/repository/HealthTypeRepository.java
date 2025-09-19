package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.HealthType;
import com.cloudbread.domain.user.domain.enums.HealthTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HealthTypeRepository extends JpaRepository<HealthType, Long> {
    boolean existsByName(HealthTypeEnum type);
}
