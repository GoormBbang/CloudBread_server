package com.cloudbread.domain.user.domain.entity;

import com.cloudbread.domain.model.BaseEntity;
import com.cloudbread.domain.user.domain.enums.OauthProvider;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    private String profileImageUrl;

    private LocalDate birthDate; // 생년월일

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(precision = 5, scale = 2)
    private BigDecimal height;

    @Column(precision = 5, scale = 2)
    private BigDecimal weight;

    @Column(name = "other_health_factors")
    private String otherHealthFactors;

    private boolean activated; // soft-delete 구현, 삭제 구현 시 activated = 0 (false)

    @Builder
    public User(Long id, String email, OauthProvider oauthProvider, String nickname, String profileImageUrl, LocalDate birthDate,
                LocalDate dueDate, BigDecimal height, BigDecimal weight, String otherHealthFactors, boolean activated) {
        this.id = id;
        this.email = email;
        this.oauthProvider = oauthProvider;
        this.nickname = nickname;
        this.profileImageUrl = profileImageUrl;
        this.birthDate = birthDate;
        this.dueDate = dueDate;
        this.height = height;
        this.weight = weight;
        this.otherHealthFactors = otherHealthFactors;
        this.activated = activated;
    }

    // JwtAuthorizationFilter에서 스프링컨텍스트에 넣을 유저 식별 정보 생성 함수
    public static User createUserForSecurityContext(Long userId, String email){
        return User.builder()
                .id(userId)
                .email(email)
                .build();
    }

    // 회원가입 STEP1 -> oauth2 회원가입 (email, oauth_provider, nickname, profile_image_url // activated)
    public static User createUserFirstOAuth(String email, String nickname, String profileImageUrl, OauthProvider oauthProvider) {
        return User.builder()
                .email(email)
                .oauthProvider(oauthProvider)
                .nickname(nickname)
                .profileImageUrl(profileImageUrl)
                .activated(true) // soft-delete를 위한 필드 (삭제 구현 시, activated = 0)
                .build();

    }

    // 회원가입 STEP2
    public void updateDetails(LocalDate birthDate, BigDecimal height, BigDecimal weight, LocalDate dueDate){
        this.birthDate = birthDate;
        this.height = height;
        this.weight = weight;
        this.dueDate = dueDate;
    }

    // 기타 건강상태 업데이트
    public void updateOtherHealthFactors(String otherHealthFactors){
        this.otherHealthFactors = otherHealthFactors;
    }

    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    //생년월일 업데이트
    public void updateBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }
}
