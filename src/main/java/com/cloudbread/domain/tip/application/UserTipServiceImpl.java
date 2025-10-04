package com.cloudbread.domain.tip.application;

import com.cloudbread.domain.crawling.domain.entity.TipContent;
import com.cloudbread.domain.crawling.domain.enums.TipCategoryName;
import com.cloudbread.domain.crawling.domain.repository.TipContentRepository;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.dto.UserResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.cloudbread.domain.user.exception.UserNotFoundException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserTipServiceImpl implements UserTipService {

    private final UserRepository userRepository;
    private final TipContentRepository tipContentRepository;

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto.TipResponse getMyTips() {
        // 🔹 로그인 사용자 이메일 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // 주차 계산 (dueDate 기반)
        int weekNumber = calculatePregnancyWeek(user.getDueDate());

        // DB에서 해당 주차의 모든 Tip 조회
        List<TipContent> contents = tipContentRepository.findByTip_WeekNumber(weekNumber);

        List<UserResponseDto.TipDto> tips = contents.stream()
                .map(c -> new UserResponseDto.TipDto(
                        c.getId(),
                        c.getCategory().getName().name(),
                        c.getTitle(),
                        c.getDescription()
                ))
                .collect(Collectors.toList());

        return new UserResponseDto.TipResponse(weekNumber, tips);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto.TipResponse getBabyTips() {
        return getTipsByCategory(TipCategoryName.BABY);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto.TipResponse getMomTips() {
        return getTipsByCategory(TipCategoryName.MOM);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto.TipResponse getNutritionTips() {
        return getTipsByCategory(TipCategoryName.NUTRITION);
    }

    private UserResponseDto.TipResponse getTipsByCategory(TipCategoryName category) {
        // 🔹 로그인 사용자 이메일 가져오기
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email)
                .orElseThrow(UserNotFoundException::new);

        // 주차 계산 (dueDate 기반)
        int weekNumber = calculatePregnancyWeek(user.getDueDate());

        // DB에서 주차 + 카테고리로 필터링
        List<TipContent> contents = tipContentRepository
                .findByTip_WeekNumberAndCategory_Name(weekNumber, category);

        // DTO 변환
        List<UserResponseDto.TipDto> tips = contents.stream()
                .map(c -> new UserResponseDto.TipDto(
                        c.getId(),
                        c.getCategory().getName().name(),
                        c.getTitle(),
                        c.getDescription()
                ))
                .toList();

        return new UserResponseDto.TipResponse(weekNumber, tips);
    }


    private int calculatePregnancyWeek(LocalDate dueDate) {//user-due_date 사용
        if (dueDate == null) return 0;

        // 임신 시작일 = 출산 예정일 - 280일 (40주)
        LocalDate pregnancyStartDate = dueDate.minusDays(280);

        long days = ChronoUnit.DAYS.between(pregnancyStartDate, LocalDate.now());
        return (int) (days / 7) + 1;
    }

}
