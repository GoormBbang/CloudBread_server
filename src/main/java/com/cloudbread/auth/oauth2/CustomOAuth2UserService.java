package com.cloudbread.auth.oauth2;

import com.cloudbread.auth.oauth2.dto.KaKaoResponse;
import com.cloudbread.auth.oauth2.dto.OAuth2Response;
import com.cloudbread.domain.user.domain.entity.User;
import com.cloudbread.domain.user.domain.enums.OauthProvider;
import com.cloudbread.domain.user.domain.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

// OAuth2 제공자(카카오,구글..)로부터 제공받은 사용자 정보를, 우리 서비스에 맞게 가공, 변환
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        log.info("CustomOAuth2UserService는 왔음 ??");


        OAuth2User oAuth2User = super.loadUser(userRequest);

        log.info("CustomOAuth2UserService :: {}", oAuth2User);
        log.info("oAuthUser.getAttributes :: {}", oAuth2User.getAttributes());

        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        log.info("registrationId :: {}", registrationId);

        OAuth2Response oAuth2Response = null;

        switch (registrationId) {
            case "kakao":
                oAuth2Response = new KaKaoResponse(oAuth2User.getAttributes());
                break;
//            case "google":
//                oAuth2Response = new GoogleResponse(oAuth2User.getAttributes());
//                break;
//            case "naver":
//                oAuth2Response = new NaverResponse(oAuth2User.getAttributes());
//                break;
            default:
                throw new OAuth2AuthenticationException("지원하지 않는 OAuth Provider입니다.");
        }

        // OAuth2User로부터 가져온 정보를 바탕으로, 회원가입 or 로그인

        // DB에 해당 유저가 있는지 판단
        Optional<User> foundUser = userRepository.findByEmail(oAuth2Response.getEmail());

        // DB에 유저 없으면 - 회원가입
        if (foundUser.isEmpty()){
            log.info("user가 없어서 회원가입했어요.");
            User user = User.createUserFirstOAuth(
                    oAuth2Response.getEmail(),
                    oAuth2Response.getNickName(),
                    oAuth2Response.getProfileImage(),
                    OauthProvider.fromRegistrationId(registrationId)
            );

            userRepository.save(user);

            return new CustomOAuth2User(user);
        } else {
            log.info("이미 해당 유저가 회원가입에 존재해서, CustomOAuth2User만 채워둡니다.");
            // DB에 유저 존재하면 - 로그인 진행 (이때 로그인 처리는 안하고, OAuth2LoginSuccessHandler에서 담당함)
            User user = foundUser.get();

            return new CustomOAuth2User(user);
        }

    }
}
