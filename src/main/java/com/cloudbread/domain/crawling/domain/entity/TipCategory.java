package com.cloudbread.domain.crawling.domain.entity;

import com.cloudbread.domain.crawling.domain.enums.TipCategoryName;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipCategory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)   // DB에는 문자열로 저장됨 ("MOM", "BABY", "NUTRITION")
    private TipCategoryName name;
}

