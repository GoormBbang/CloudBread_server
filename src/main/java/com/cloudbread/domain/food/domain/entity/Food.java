package com.cloudbread.domain.food.domain.entity;

import com.cloudbread.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "foods")
@Getter
public class Food extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name; // 음식명

    @Column(name = "image_url")
    private String imageUrl; // 음식이미지 (엑셀표에서는 제공 x)

    @Column(name = "source_name")
    private String sourceName; // 출처명 (ex. 식약처)

    @Column(name = "external_id")
    private String externalId; // 식품코드

    private BigDecimal calories; // 1회 섭취량 칼로리

    private String category; // 음식 카테고리 ex) 가정식(분석 함량)

    @Builder
    public Food(String name, String imageUrl, String sourceName, String externalId, BigDecimal calories, String category) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.sourceName = sourceName;
        this.externalId = externalId;
        this.calories = calories;
        this.category = category;
    }

    public boolean merge(String name, String sourceName, BigDecimal calories, String category) {
        boolean changed = false;
        if (name != null && !name.equals(this.name)) { this.name = name; changed = true; }
        if (sourceName != null && !sourceName.equals(this.sourceName)) { this.sourceName = sourceName; changed = true; }
        if (calories != null && (this.calories == null || this.calories.compareTo(calories)!=0)) {
            this.calories = calories; changed = true;
        }
        if (category != null && !category.equals(this.category)) { this.category = category; changed = true; }
        return changed;
    }
}