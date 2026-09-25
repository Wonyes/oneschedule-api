package com.studio.core.domain.member.repository;

import com.studio.core.domain.member.entity.EmailVerificationEntity;
import com.studio.core.global.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

    Optional<EmailVerificationEntity> findByEmailAndPurpose(String email, VerificationPurpose purpose);
    void deleteByEmailAndPurpose(String email, VerificationPurpose purpose);
    long deleteAllByExpiresAtBefore(LocalDateTime now);
}
