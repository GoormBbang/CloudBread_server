package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.DietType;
import com.cloudbread.domain.user.domain.enums.DietTypeEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DietTypeRepository extends JpaRepository<DietType, Long> {
    boolean existsByName(DietTypeEnum type);
    @Query("select d from DietType d order by d.id")
    List<DietType> findAllOrderById();
    default void assertAllExist(List<Long> ids) {
        if (!ids.isEmpty()) {
            long cnt = countByIdIn(ids);
            if (cnt != ids.size())
                throw new IllegalArgumentException("Invalid dietTypeIds");
        }
    }
    long countByIdIn(List<Long> ids);
}
