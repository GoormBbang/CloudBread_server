package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.DietType;
import com.cloudbread.domain.user.domain.enums.DietTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DietTypeRepository extends JpaRepository<DietType, Long> {
    boolean existsByName(DietTypeEnum type);
}
