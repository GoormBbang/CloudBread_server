//package com.cloudbread.domain.crawling.domain.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//@Table(name = "tip_content")
//public class TipContent {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    // FK → tips.id
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "tip_id", nullable = false)
//    private Tips tip;
//
//    // FK → tip_category.id
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "category_id", nullable = false)
//    private TipCategory category;
//
//    @Column(nullable = false, length = 255)
//    private String title;
//
//    @Column(columnDefinition = "TEXT")
//    private String description;
//}
