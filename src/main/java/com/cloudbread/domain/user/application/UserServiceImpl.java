package com.cloudbread.domain.user.application;

import com.cloudbread.auth.token.domain.Token;
import com.cloudbread.auth.token.domain.TokenRepository;
import com.cloudbread.auth.token.exception.RefreshTokenNotFoundException;
import com.cloudbread.domain.crawling.domain.entity.TipContent;
import com.cloudbread.domain.crawling.domain.repository.TipContentRepository;
import com.cloudbread.domain.user.converter.UserConverter;
import com.cloudbread.domain.user.domain.entity.*;
import com.cloudbread.domain.user.domain.enums.DietTypeEnum;
import com.cloudbread.domain.user.domain.enums.HealthTypeEnum;
import com.cloudbread.domain.user.domain.repository.*;
import com.cloudbread.domain.user.dto.UserRequestDto;
import com.cloudbread.domain.user.dto.UserResponseDto;
import com.cloudbread.domain.user.exception.UserAlreadyDeactivatedException;
import com.cloudbread.domain.user.exception.UserNotFoundException;
import com.cloudbread.global.common.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

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

        // dietType → id + name
        List<UserResponseDto.MetadataItemDto> dietTypes = userDiets.stream()
                .map(ud -> UserResponseDto.MetadataItemDto.builder()
                        .id(ud.getDietType().getId())
                        .name(ud.getDietType().getName().name()) // Enum → String
                        .build())
                .toList();

        // healthType → id + name
        List<UserResponseDto.MetadataItemDto> healthTypes = userHealths.stream()
                .map(uh -> UserResponseDto.MetadataItemDto.builder()
                        .id(uh.getHealthType().getId())
                        .name(uh.getHealthType().getName().name())
                        .build())
                .toList();

        // allergy → id + name
        List<UserResponseDto.MetadataItemDto> allergies = userAllergies.stream()
                .map(ua -> UserResponseDto.MetadataItemDto.builder()
                        .id(ua.getAllergy().getId())
                        .name(ua.getAllergy().getName()) // String 그대로
                        .build())
                .toList();

        return UserConverter.toMyInfoResponse(user, dietTypes, healthTypes, allergies);
    }



    @Override
    @Transactional
    public UserResponseDto.UpdateResponse updateMyInfo(Long userId, UserRequestDto.UpdateMyInfoRequest request) {
        // 유저 조회
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 기본 정보 업데이트
        user.updateDetails(
                request.getHeight(),
                request.getWeight(),
                request.getDueDate()
        );

        if (request.getNickname() != null) {
            user.updateNickname(request.getNickname());
        }

        user.updateOtherHealthFactors(request.getOtherHealthFactors());

        // 기존 관계 삭제
        userDietRepository.deleteByUserId(userId);
        userHealthRepository.deleteByUserId(userId);
        userAllergyRepository.deleteByUserId(userId);

        // dietType 업데이트 (id 기반)
        if (request.getDietTypeIds() != null && !request.getDietTypeIds().isEmpty()) {
            request.getDietTypeIds().forEach(dietTypeId -> {
                DietType dietType = dietTypeRepository.findById(dietTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid dietTypeId: " + dietTypeId));
                userDietRepository.save(UserDiet.of(user, dietType));
            });
        }

        // healthType 업데이트 (id 기반)
        if (request.getHealthTypeIds() != null && !request.getHealthTypeIds().isEmpty()) {
            request.getHealthTypeIds().forEach(healthTypeId -> {
                HealthType healthType = healthTypeRepository.findById(healthTypeId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid healthTypeId: " + healthTypeId));
                userHealthRepository.save(UserHealth.of(user, healthType));
            });
        }

        // allergy 업데이트 (id 기반)
        if (request.getAllergyIds() != null && !request.getAllergyIds().isEmpty()) {
            request.getAllergyIds().forEach(allergyId -> {
                Allergy allergy = allergyRepository.findById(allergyId)
                        .orElseThrow(() -> new IllegalArgumentException("Invalid allergyId: " + allergyId));
                userAllergyRepository.save(UserAllergy.of(user, allergy));
            });
        }

        return UserConverter.toUpdateResponse(user);
    }

    @Override
    public UserResponseDto.UserSummaryResponse getUserSummary(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        return UserConverter.toUserSummaryResponse(user);
    }

    @Override
    @Transactional
    public UserResponseDto.UpdateUserSummaryResponse updateUserSummary(
            Long userId,
            UserRequestDto.UpdateUserSummaryRequest request
    ) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        // 닉네임 수정
        if (request.getNickname() != null) {
            user.updateNickname(request.getNickname());
        }

        // 생년월일 수정
        if (request.getBirthDate() != null) {
            user.updateBirthDate(request.getBirthDate());
        }

        return UserConverter.toUpdateUserSummaryResponse(user);
    }

    @Override
    public UserResponseDto.ProfileResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 사용자입니다. userId=" + userId));

        return UserResponseDto.ProfileResponse.builder()
                .nickname(user.getNickname())
                .birthDate(user.getBirthDate())
                .build();
    }

    @Override
    public void logout(Long userId) {
        Token token = tokenRepository.findByUserId(userId)
                .orElseThrow(() -> new RefreshTokenNotFoundException());
        tokenRepository.delete(token);
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(UserNotFoundException::new);

        if (!user.isActivated()) {
            throw new UserAlreadyDeactivatedException();
        }

        user.deactivate(); // activated = false
    }


}
