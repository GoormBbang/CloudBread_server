package com.cloudbread.domain.food.domain.entity;

import com.cloudbread.domain.food.domain.enums.Unit;
import com.cloudbread.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "nutrients")
@Getter
public class Nutrient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name; // "carbs", "proteins", ..

    @Column(name = "nutrient_amount")
    private BigDecimal nutrientAmount; // 영양성분함량기준량

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Unit unit; // g, mg, μg

    @Builder
    public Nutrient(String name, BigDecimal nutrientAmount, Unit unit) {
        this.name = name;
        this.nutrientAmount = nutrientAmount;
        this.unit = unit;
    }

}
