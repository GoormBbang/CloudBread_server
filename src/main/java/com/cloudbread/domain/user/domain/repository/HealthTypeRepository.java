package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.HealthType;
import com.cloudbread.domain.user.domain.enums.HealthTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface HealthTypeRepository extends JpaRepository<HealthType, Long> {
    boolean existsByName(HealthTypeEnum type);
    @Query("select h from HealthType h order by h.id")
    List<HealthType> findAllOrderById();

    long countByIdIn(List<Long> ids);
    default void assertAllExist(List<Long> ids) {
        if (!ids.isEmpty() && countByIdIn(ids) != ids.size())
            throw new IllegalArgumentException("Invalid healthTypeIds");
    }
}
