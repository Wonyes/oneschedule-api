package com.studio.core.domain.member.entity;

import com.studio.core.domain.member.dto.member.request.MemberPatchRequest;
import com.studio.core.global.enums.AuthProvider;
import com.studio.core.global.enums.AuthRole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tb_member",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_member_provider",
                columnNames = {"provider", "provider_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberNo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AuthProvider provider;

    @Column(length = 100)
    private String providerId;

    @Column(nullable = false, unique = true)
    private String email;

    @Comment("소셜 가입자는 비밀번호가 없다")
    @Column
    private String password;

    @Column(nullable = false, unique = true)
    private String nickname;

    @Comment("소셜 가입자는 가입 시점에 번호를 받지 못한다")
    @Column
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AuthRole role;

    @Comment("이름")
    private String name;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column
    private String profileImageUrl;

    @Column
    private LocalDateTime lastSeenAt;

    @PrePersist
    public void prePersist() {
        createdAt = LocalDateTime.now();
    }


    public void changePassword(String password) {
        this.password = password;
    }

    @Builder
    public MemberEntity(
        String email,
        String password,
        String nickname,
        String phoneNumber,
        String name,
        String profileImageUrl,
        AuthProvider provider,
        String providerId
    ) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
        this.phoneNumber = phoneNumber;
        this.name = name;
        this.profileImageUrl = profileImageUrl;

        this.provider = provider != null ? provider : AuthProvider.LOCAL;
        this.providerId = providerId;
        this.role = AuthRole.ROLE_USER;
        this.createdAt = LocalDateTime.now();
    }

    public static MemberEntity create(
            String email,
            String password,
            String nickname,
            String phoneNumber,
            String name
    ) {
        return MemberEntity.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .phoneNumber(phoneNumber)
                .name(name)
                .build();
    }

    public static MemberEntity createSocialMember(
            String email, String nickname, String name,
            String profileImageUrl, AuthProvider provider, String providerId
    ) {
        return MemberEntity.builder()
                .email(email)
                .nickname(nickname)
                .name(name)
                .profileImageUrl(profileImageUrl)
                .provider(provider)
                .providerId(providerId)
                .build();
    }

    public void update(MemberPatchRequest request) {
        if(request.nickname() != null) {
            this.nickname = request.nickname();
        }

        if(request.name() != null && !request.name().isBlank()) {
            this.name = request.name().trim().replaceAll("\\s+", " ");
        }

        if(request.phoneNumber() != null){
            this.phoneNumber = request.phoneNumber();
        }
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }
    public void linkSocial(AuthProvider provider, String providerId) {
        this.provider = provider;
        this.providerId =providerId;
    }

}
