package com.cloudbread.domain.crawling.domain.repository;

import com.cloudbread.domain.crawling.domain.entity.TipContent;
import com.cloudbread.domain.crawling.domain.enums.TipCategoryName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TipContentRepository extends JpaRepository<TipContent, Long> {

    // 주차 기준 전체 조회
    List<TipContent> findByTip_WeekNumber(Integer weekNumber);

    // 주차 + 카테고리 기준 조회 (BABY, MOM, NUTRITION)
    //List<TipContent> findByTip_WeekNumberAndCategory_Name(Integer weekNumber, TipCategoryName category);
    List<TipContent> findByTip_WeekNumberAndCategory_Name(int weekNumber, TipCategoryName name);

}