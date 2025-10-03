//package com.cloudbread.domain.crawling.domain.entity;
//
//import jakarta.persistence.*;
//import lombok.*;
//
//import java.util.ArrayList;
//import java.util.List;
//
//@Entity
//@Getter
//@NoArgsConstructor(access = AccessLevel.PROTECTED)
//@AllArgsConstructor
//@Builder
//@Table(name = "tips")
//public class Tips {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "week_number", nullable = false)
//    private Integer weekNumber;
//
//    @OneToMany(mappedBy = "tip", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TipContent> contents = new ArrayList<>();
//}
