package com.cloudbread.domain.chat.nutrients.application.user_profile;

import com.cloudbread.domain.user.domain.entity.UserAllergy;
import com.cloudbread.domain.user.domain.entity.UserDiet;
import com.cloudbread.domain.user.domain.entity.UserHealth;
import com.cloudbread.domain.user.domain.repository.UserAllergyRepository;
import com.cloudbread.domain.user.domain.repository.UserDietRepository;
import com.cloudbread.domain.user.domain.repository.UserHealthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class UserProfileServiceImpl implements UserProfileService {
    private final UserAllergyRepository userAllergyRepository;
    private final UserDietRepository userDietRepository;
    private final UserHealthRepository userHealthRepository;
    @Override
    @Transactional(readOnly = true)
    public UserProfile getProfile(Long userId) {
        // TODO: 실제로 user_healths, user_diets, user_allergies 조합해서 리턴

        // 알레르기 (문자열)
        List<UserAllergy> uaList = userAllergyRepository.findWithAllergyByUserId(userId);
        List<String> allergies = uaList.stream()
                .map(ua -> ua.getAllergy() != null ? ua.getAllergy().getName() : null)
                .filter(this::notBlank)
                .distinct()
                .toList();

        // 식이 (Enum → 문자열)
        List<UserDiet> udList = userDietRepository.findWithDietTypeByUserId(userId);
        List<String> diets = udList.stream()
                .map(ud -> ud.getDietType() != null ? ud.getDietType().getName() : null) // DietTypeEnum
                .map(this::enumToLabel)
                .filter(this::notBlank)
                .distinct()
                .toList();


        // 건강 상태 (Enum → 문자열)
        List<UserHealth> uhList = userHealthRepository.findWithHealthTypeByUserId(userId);
        List<String> healths = uhList.stream()
                .map(uh -> uh.getHealthType() != null ? uh.getHealthType().getName() : null) // HealthTypeEnum
                .map(this::enumToLabel)
                .filter(this::notBlank)
                .distinct()
                .toList();

        return new UserProfile(healths, diets, allergies);

    }

    // ===== helpers =====

    private boolean notBlank(String s) {
        return s != null && !s.isBlank();
    }

    /** Enum → 표시 문자열. */
    private String enumToLabel(Enum<?> e) {
        if (e == null) return null;
        return e.name();
    }
}
