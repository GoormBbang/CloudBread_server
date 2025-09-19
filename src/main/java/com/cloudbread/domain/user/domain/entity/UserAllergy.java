package com.cloudbread.domain.user.domain.entity;

import com.cloudbread.domain.model.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user_allergies") // mysql table명
@Getter
public class UserAllergy extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "allergy_id")
    private Allergy allergy;

    @Builder
    public UserAllergy(Long id, User user, Allergy allergy) {
        this.id = id;
        this.user = user;
        this.allergy = allergy;
    }

    public static UserAllergy of(User user, Allergy allergy){
        return UserAllergy.builder()
                .user(user)
                .allergy(allergy)
                .build();
    }
}