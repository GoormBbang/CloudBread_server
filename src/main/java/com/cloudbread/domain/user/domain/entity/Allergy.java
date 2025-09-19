package com.cloudbread.domain.user.domain.entity;

import com.cloudbread.domain.model.BaseEntity;
import com.cloudbread.domain.user.domain.enums.HealthTypeEnum;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "allergies")
@Getter
public class Allergy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name; // 알레르기명
}