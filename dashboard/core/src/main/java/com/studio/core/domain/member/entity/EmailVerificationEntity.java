package com.studio.core.domain.member.entity;

import com.studio.core.global.enums.VerificationPurpose;
import com.studio.core.global.repository.TimeBaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicUpdate;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_email_verification",
        indexes = @Index(name = "idx_email_verification", columnList = "email, purpose"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@DynamicUpdate
public class EmailVerificationEntity extends TimeBaseEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private VerificationPurpose purpose;

    @Column(nullable = false, length = 6)
    private String code;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    private LocalDateTime verifiedAt;

    @Column(name = "fail_count", nullable = false)
    private int failCount = 0;

    @Builder
    public EmailVerificationEntity(
            String email, VerificationPurpose purpose, String code, int failCount,
            LocalDateTime expiresAt
    ) {
        this.email = email;
        this.purpose = purpose;
        this.code = code;
        this.failCount = failCount;
        this.expiresAt = expiresAt;
    }

    public boolean isExpired() { return LocalDateTime.now().isAfter(expiresAt); }
    public boolean isVerified() { return verifiedAt != null; }
    public void verify() { this.verifiedAt =  LocalDateTime.now(); }
    public void renew(String code, LocalDateTime expiresAt) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.verifiedAt = null;
        this.failCount = 0;
    }
    public void increaseFailCount() { this.failCount++; }
    public boolean isFailCountExceeded() { return failCount >= 5; }
}
