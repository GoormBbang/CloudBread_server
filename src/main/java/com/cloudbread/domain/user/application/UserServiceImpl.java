package com.cloudbread.domain.user.application;

import com.cloudbread.auth.token.domain.Token;
import com.cloudbread.auth.token.domain.TokenRepository;
import com.cloudbread.auth.token.exception.RefreshTokenNotFoundException;
import com.cloudbread.domain.user.converter.UserConverter;
import com.cloudbread.domain.user.domain.entity.*;
import com.cloudbread.domain.user.domain.repository.*;
import com.cloudbread.domain.user.dto.UserRequestDto;
import com.cloudbread.domain.user.dto.UserResponseDto;
import com.cloudbread.domain.user.exception.UserNotFoundException;
import com.cloudbread.global.common.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final DietTypeRepository dietTypeRepository;
    private final HealthTypeRepository healthTypeRepository;
    private final AllergyRepository allergyRepository;

    private final UserDietRepository userDietRepository;
    private final UserHealthRepository userHealthRepository;
    private final UserAllergyRepository userAllergyRepository;

    private final TokenRepository tokenRepository;

    @Override
    public UserResponseDto.UpdateResponse updateDetails(Long userId, UserRequestDto.UpdateDetailsRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        user.updateDetails(
                request.getBirthDate(), request.getHeight(), request.getWeight(), request.getDueDate()
        ); // 변경감지로 update

        return UserConverter.toUpdateResponse(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto.MetadataResponse getMetaData() {
        List<DietType> dietTypes = dietTypeRepository.findAllOrderById();
        List<HealthType> healthTypes = healthTypeRepository.findAllOrderById();
        List<Allergy> allergies = allergyRepository.findAllOrderById();

        List<UserResponseDto.MetadataItemDto> dietDtoList = UserConverter.toDietTypeDtoList(dietTypes);
        List<UserResponseDto.MetadataItemDto> healthDtoList = UserConverter.toHealthTypeDtoList(healthTypes);
        List<UserResponseDto.MetadataItemDto> allergyDtoList = UserConverter.toAllergyDtoList(allergies);

        return UserResponseDto.MetadataResponse.builder()
                .dietTypes(dietDtoList)
                .healthTypes(healthDtoList)
                .allergies(allergyDtoList)
                .build();

    }

    @Override
    public UserResponseDto.UpdateResponse updateHealthInfo(Long userId, UserRequestDto.UpdateHealthInfoRequest request) {
        User user = userRepository.getReferenceById(userId); // User의 엔디티가 아닌 프록시 객체를 가져온다 (성능 최적화, 불필요 쿼리 줄이기)

        // 유효성: 존재하는 id 인지 검증
        dietTypeRepository.assertAllExist(request.getDietTypeIds());
        healthTypeRepository.assertAllExist(request.getHealthTypeIds());
        allergyRepository.assertAllExist(request.getAllergyIds());

        // 기타 건강상태(자유입력) 업데이트
        user.updateOtherHealthFactors(request.getOtherHealthFactors());

        // 기존 관계 제거 -> 새로 삽입
        userDietRepository.deleteByUserId(userId);
        userHealthRepository.deleteByUserId(userId);
        userAllergyRepository.deleteByUserId(userId);

        // of() 메서드에 엔티티 프록시를 직접 전달
        if (!request.getDietTypeIds().isEmpty()) {
            request.getDietTypeIds().forEach(did -> {
                DietType dietTypeRef = dietTypeRepository.getReferenceById(did);
                userDietRepository.save(UserDiet.of(user, dietTypeRef));
            });
        }
        if (!request.getHealthTypeIds().isEmpty()) {
            request.getHealthTypeIds().forEach(hid -> {
                HealthType healthTypeRef = healthTypeRepository.getReferenceById(hid);
                userHealthRepository.save(UserHealth.of(user, healthTypeRef));
            });
        }
        if (!request.getAllergyIds().isEmpty()) {
            request.getAllergyIds().forEach(aid -> {
                Allergy allergyRef = allergyRepository.getReferenceById(aid);
                userAllergyRepository.save(UserAllergy.of(user, allergyRef));
            });
        }

        return UserConverter.toUpdateResponse(user);
    }

    @Override
    public UserResponseDto.Example exampleMethod(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        return UserConverter.toExample(user);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto.MyInfoResponse getInfo2(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // userId 기반으로 직접 조회
        List<UserDiet> userDiets = userDietRepository.findByUserId(userId);
        List<UserHealth> userHealths = userHealthRepository.findByUserId(userId);
        List<UserAllergy> userAllergies = userAllergyRepository.findByUserId(userId);

        List<String> dietTypes = userDiets.stream()
                .map(ud -> ud.getDietType().getName().name()) // DietTypeEnum → String
                .toList();

        List<String> healthTypes = userHealths.stream()
                .map(uh -> uh.getHealthType().getName().name()) // HealthTypeEnum → String
                .toList();

        List<String> allergies = userAllergies.stream()
                .map(ua -> ua.getAllergy().getName()) // String 그대로
                .toList();

        return UserConverter.toMyInfoResponse(user, dietTypes, healthTypes, allergies);
    }

    @Override
    public void logout(Long userId) {
        Token token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RefreshTokenNotFoundException());
        tokenRepository.delete(token);
    }
}
