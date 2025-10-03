package com.cloudbread.domain.crawling.domain.repository;

import com.cloudbread.domain.crawling.domain.entity.TipCategory;
import com.cloudbread.domain.crawling.domain.enums.TipCategoryName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipCategoryRepository extends JpaRepository<TipCategory, Long> {
    Optional<TipCategory> findByName(TipCategoryName name);
}
