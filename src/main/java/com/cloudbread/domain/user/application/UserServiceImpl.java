package com.cloudbread.domain.user.application;

import com.cloudbread.domain.user.converter.UserConverter;
import com.cloudbread.domain.user.domain.entity.Allergy;
import com.cloudbread.domain.user.domain.entity.DietType;
import com.cloudbread.domain.user.domain.entity.HealthType;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.AllergyRepository;
import com.cloudbread.domain.user.domain.repository.DietTypeRepository;
import com.cloudbread.domain.user.domain.repository.HealthTypeRepository;
import com.cloudbread.domain.user.domain.repository.UserRepository;
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
    public UserResponseDto.Example exampleMethod(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException());

        return UserConverter.toExample(user);
    }

}
