package com.cloudbread.domain.crawling.domain.repository;

import com.cloudbread.domain.crawling.domain.entity.Tips;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TipsRepository extends JpaRepository<Tips, Long> {
    Optional<Tips> findByWeekNumber(Integer weekNumber);
}
