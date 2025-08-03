package com.cloudbread.domain.user.domain.entity;

import com.cloudbread.domain.model.BaseEntity;
import com.cloudbread.domain.user.domain.enums.OauthProvider;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

// 만약 ManyToOne 으로 관계설정할 경우, FetchType = LAZY 로 설정해주세요 (oneToMany 는 디폴티로 FetchType이 LAZY 라서 안해줘도 됨)
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users") // mysql table명
@Getter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false) // 사용자 식별 - unique 제약
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth_provider", nullable = false)
    private OauthProvider oauthProvider;

    private String nickname;

    private String profile_image_url;

    private int age;

    @Column(name = "due_date", nullable = false)
    private LocalDate dueDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "other_health_factors")
    private String otherHealthFactors;

    private boolean activated; // soft-delete 구현, 삭제 구현 시 activated = 0 (false)

    @Builder
    public User(Long id, String email, OauthProvider oauthProvider, String nickname, String profile_image_url, int age,
                LocalDate dueDate, BigDecimal height, BigDecimal weight, String otherHealthFactors, boolean activated) {
        this.id = id;
        this.email = email;
        this.oauthProvider = oauthProvider;
        this.nickname = nickname;
        this.profile_image_url = profile_image_url;
        this.age = age;
        this.dueDate = dueDate;
        this.height = height;
        this.weight = weight;
        this.otherHealthFactors = otherHealthFactors;
        this.activated = activated;
    }

    // User 도메인 관련 비즈니스 로직
    public boolean isAdult(){ // 테스트
        return this.age >= 20;
    }

}
