package com.studio.core.domain.member.repository;

import com.studio.core.domain.member.entity.EmailVerificationEntity;
import com.studio.core.global.enums.VerificationPurpose;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailVerificationRepository extends JpaRepository<EmailVerificationEntity, Long> {

    Optional<EmailVerificationEntity> findByEmailAndPurpose(String email, VerificationPurpose purpose);
    void deleteByEmailAndPurpose(String email, VerificationPurpose purpose);
    long deleteAllByExpiresAtBefore(LocalDateTime now);
    /** 정리용 — 만료된 지 하루 지난 인증 행 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from EmailVerificationEntity v where v.expiresAt < :before")
    int deleteExpiredBefore(@Param("before") LocalDateTime before);

}
