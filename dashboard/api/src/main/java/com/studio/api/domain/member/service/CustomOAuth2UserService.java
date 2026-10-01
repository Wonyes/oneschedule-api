package com.studio.api.domain.member.service;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.global.enums.AuthProvider;
import com.studio.core.global.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final NotificationService notificationService;

    @Override
    @Transactional
    public OAuth2User loadUser(OAuth2UserRequest request) {
        Map<String, Object> attr = super.loadUser(request).getAttributes();

        String providerId = (String) attr.get("sub");
        String email = (String) attr.get("email");
        boolean emailVerified = Boolean.TRUE.equals(attr.get("email_verified"));
        String name = (String) attr.get("name");
        String picture = (String) attr.get("picture");

        MemberEntity member = memberRepository
                .findByProviderAndProviderId(AuthProvider.GOOGLE, providerId)
                .orElseGet(() -> linkOrCreate(email, emailVerified, name, picture, providerId));

        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority(member.getRole().name())),
                Map.of("memberNo", member.getMemberNo()),
                "memberNo"
        );
    }

    private MemberEntity linkOrCreate(
            String email,
            boolean emailVerified,
            String name,
            String picture,
            String providerId
    ) {
        return memberRepository.findByEmail(email)
                .map(existing -> {
                    if (!emailVerified) {
                        throw new OAuth2AuthenticationException("EMAIL_NOT_VERIFIED");
                    }
                    existing.linkSocial(AuthProvider.GOOGLE, providerId);
                    return existing;
                })
                .orElseGet(() -> {
                    MemberEntity member = memberRepository.save(
                            MemberEntity.createSocialMember(
                                    email, uniqueNickname(name), name, picture,
                                    AuthProvider.GOOGLE, providerId
                            )
                    );

                    notificationService.send(
                            List.of(member.getMemberNo()),
                            null,
                            NotificationType.WELCOME,
                            NotificationType.WELCOME.message(member.getNickname()),
                            null);

                    return member;
                });
    }

    private String uniqueNickname(String name) {
        String base = (name == null || name.isBlank()) ? "user" : name;
        String candidate = base;

        while (memberRepository.existsByNickname(candidate)) {
            candidate = base + UUID.randomUUID().toString().substring(0, 4);
        }

        return candidate;
    }
}
