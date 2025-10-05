package com.cloudbread.domain.photo_analyses.domain.entity;

import com.cloudbread.domain.food.domain.entity.Food;
import com.cloudbread.domain.model.BaseEntity;
import com.cloudbread.domain.photo_analyses.domain.enums.PhotoAnalysisStatus;
import com.cloudbread.domain.user.domain.entity.User;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.*;
import java.util.List;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "photo_analysis") // mysql table명
@Getter
public class PhotoAnalysis extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // 업로더

    @Column(length = 512)
    private String imageUrl; // nhn cloud에 업로드될 이미지 url

    private String aiLabel; // ai 추청 음식명

    private Double aiConfidence; // 추정치 신뢰도

    @Column(columnDefinition = "LONGTEXT")
    private String candidatesJson;  // 음식 후보 3개

    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private PhotoAnalysisStatus photoAnalysisStatus; // 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "selected_food_id")
    private Food food; // 유저가 고른 음식 후보 1개

    @Builder
    public PhotoAnalysis(Long id, User user, String imageUrl, String aiLabel, Double aiConfidence, String candidatesJson, PhotoAnalysisStatus photoAnalysisStatus, Food food) {
        this.id = id;
        this.user = user;
        this.imageUrl = imageUrl;
        this.aiLabel = aiLabel;
        this.aiConfidence = aiConfidence;
        this.candidatesJson = candidatesJson;
        this.photoAnalysisStatus = photoAnalysisStatus;
        this.food = food;
    }

    public static PhotoAnalysis createPhotoAnalysisSetStatus(
        String imageUrl, String aiLabel, Double aiConfidence, String candidatesJson, PhotoAnalysisStatus status
    ){
        return PhotoAnalysis.builder()
                .imageUrl(imageUrl)
                .aiLabel(aiLabel)
                .aiConfidence(aiConfidence)
                .candidatesJson(candidatesJson)
                .photoAnalysisStatus(status)
                .build();
    }

    public void setPhotoUser(User user){
        this.user = user;
    }

    public void setImageUrl(String imageUrl){
        this.imageUrl = imageUrl;
    }
}


