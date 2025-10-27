package com.cloudbread.auth.oauth2;

import com.cloudbread.domain.user.domain.entity.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;

// OAuth2User 인터페이스를 구현하면, Spring Security사 이 객체를 SecurityContext에 넣어줌
// 여기서 오버라이딩한 메서드들은, 스프링 시큐리티가 내부적으로 사용자 정보를 꺼낼 때 호출한다
@RequiredArgsConstructor
public class CustomOAuth2User implements OAuth2User {
    private User user;

    public CustomOAuth2User(User user){
        this.user = user;
    }

    // oauth2 공급자(google, kakao..)로부터 받은 원본 사용자 데이터를 담은 map (email,..)
    @Override
    public Map<String, Object> getAttributes() {
        return null;
    }

    // 위 getAttributes()에 있는 값들 중, 특정 키만 꺼내고 싶을 때
    // ex) getAttribute("email"), getAttributes가 null이면 무용지물
    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    // 현재 로그인한 사용자가 가진 권한(role) 목록 반환
    // ex: ROLE_USER, ROLE_ADMIN
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"));
    }

    // 사용자의 고유 식별자 반환, 보통 email나 id ..
    @Override
    public String getName() {
        return user.getEmail();
    }

    public User getUser(){
        return user;
    }

    public Long getUserId(){
        return user.getId();
    }

    public String getEmail(){
        return user.getEmail();
    }
}

