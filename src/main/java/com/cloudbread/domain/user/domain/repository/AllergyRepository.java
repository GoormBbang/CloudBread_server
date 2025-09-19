package com.cloudbread.domain.user.domain.repository;

import com.cloudbread.domain.user.domain.entity.Allergy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AllergyRepository extends JpaRepository<Allergy, Long> {
    boolean existsByName(String name);

    @Query("select a from Allergy a order by a.id")
    List<Allergy> findAllOrderById();
}
