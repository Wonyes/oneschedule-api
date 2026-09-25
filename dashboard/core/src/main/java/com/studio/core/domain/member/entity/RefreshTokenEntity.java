package com.studio.core.domain.member.entity;

import com.studio.core.global.repository.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "REFRESH_TOKEN",
        indexes = @Index(name = "idx_refresh_token", columnList = "refreshToken", unique = true))
@NoArgsConstructor
@Getter
public class RefreshTokenEntity extends TimeBaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberNo;

    @Column(nullable = false, length = 512)
    private String refreshToken;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Builder
    public RefreshTokenEntity(
            Long memberNo,
            String refreshToken,
            LocalDateTime expiresAt
    ) {
        this.memberNo = memberNo;
        this.refreshToken = refreshToken;
        this.expiresAt = expiresAt;
    }

    public void updateToken(String refreshToken, LocalDateTime expiresAt) {
        this.refreshToken = refreshToken; this.expiresAt = expiresAt;
    }
}
