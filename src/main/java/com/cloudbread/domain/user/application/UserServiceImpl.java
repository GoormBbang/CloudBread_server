package com.cloudbread.domain.user.application;

import com.cloudbread.domain.user.converter.UserConverter;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import com.cloudbread.domain.user.dto.UserResponseDto;
import com.cloudbread.domain.user.dto.UserResponseDto.Example;
import com.cloudbread.domain.user.exception.UserException;
import com.cloudbread.global.common.code.status.ErrorStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserResponseDto.Example exampleMethod(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserException(ErrorStatus.NO_SUCH_USER));

        // 도메인 비즈니스 로직 활용 -> 서비스단 부담 완화 / DDD 구조
        if (!user.isAdult()) {
            log.info("미성년자 회원입니다.");
        }
        return UserConverter.toExample(user);
    }

}
