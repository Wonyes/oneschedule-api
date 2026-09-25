package com.studio.api.domain.member.service;

import com.studio.api.global.mail.VerificationMailSender;
import com.studio.core.domain.member.entity.EmailVerificationEntity;
import com.studio.core.domain.member.repository.EmailVerificationRepository;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.global.enums.VerificationPurpose;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final Duration CODE_TTL = Duration.ofMinutes(3);
    private static final Duration RESEND_INTERVAL  = Duration.ofSeconds(60);
    private static final Duration VERIFIED_TTL  = Duration.ofMinutes(30);
    private static final int MAX_FAIL_COUNT = 5;

    private final EmailVerificationRepository repository;
    private final MemberRepository memberRepository;
    private final VerificationMailSender mailSender;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public void request(String email, VerificationPurpose purpose) {
        boolean exists = memberRepository.existsByEmail(email);

        if (purpose == VerificationPurpose.SIGNUP && exists) {
            throw new CustomException(ErrorCode.MEMBER_EMAIL_DUPLICATE);
        }
        if (purpose == VerificationPurpose.PASSWORD_RESET && !exists) return;

        String code = String.format("%06d", random.nextInt(1_000_000));
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiresAt = now.plus(CODE_TTL);

        repository.findByEmailAndPurpose(email, purpose).ifPresentOrElse(
                v -> {
                    LocalDateTime last = v.getUpdateAt() != null ? v.getUpdateAt() : v.getCreateAt();

                    if (now.isBefore(last.plus(RESEND_INTERVAL))) {
                        throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOO_OFTEN);
                    }
                    v.renew(code, expiresAt);
                },
                () -> repository.save(
                        EmailVerificationEntity.builder()
                                .email(email)
                                .code(code)
                                .purpose(purpose)
                                .expiresAt(expiresAt)
                                .build()
                )
        );

        mailSender.sendVerificationCode(email, code,
                purpose == VerificationPurpose.SIGNUP ? "회원가입" : "비밀번호 재설정",
                CODE_TTL.toMinutes());

    }

    @Transactional(noRollbackFor = CustomException.class)
    public void verify(String email, VerificationPurpose purpose, String code) {

        EmailVerificationEntity v = repository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_VERIFICATION_NOT_FOUND));

        if (v.isExpired()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_EXPIRED);
        }

        if (v.isFailCountExceeded()) {
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_TOO_MANY_FAILS);
        }

        if (!v.getCode().equals(code)) {
            v.increaseFailCount();
            throw new CustomException(ErrorCode.EMAIL_VERIFICATION_MISMATCH);
        }

        v.verify();
    }

    /** 가입/재설정 직전에 호출. 검증됐고 30분 안이면 통과시키고 행을 지운다(1회용). */
    @Transactional
    public void consumeVerified(String email, VerificationPurpose purpose) {
        LocalDateTime now = LocalDateTime.now();

        EmailVerificationEntity v = repository.findByEmailAndPurpose(email, purpose)
                .orElseThrow(() -> new CustomException(ErrorCode.EMAIL_NOT_VERIFIED));

        if (!v.isVerified() || v.getVerifiedAt().plus(VERIFIED_TTL).isBefore(now)) {
            throw new CustomException(ErrorCode.EMAIL_NOT_VERIFIED);
        }

        repository.deleteByEmailAndPurpose(email, purpose);
    }

}
