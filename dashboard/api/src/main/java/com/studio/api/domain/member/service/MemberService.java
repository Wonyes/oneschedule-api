package com.studio.api.domain.member.service;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.api.domain.member.validator.MemberValidator;
import com.studio.api.global.image.ImageFileValidator;
import com.studio.api.global.image.NaverImageClient;
import com.studio.api.global.auth.AuthToken;
import com.studio.api.global.auth.AuthTokenProvider;
import com.studio.core.domain.member.dto.member.request.MemberLoginRequest;
import com.studio.core.domain.member.dto.member.request.MemberPatchRequest;
import com.studio.core.domain.member.dto.member.request.PasswordResetRequest;
import com.studio.core.domain.member.dto.member.request.PasswordUpdateRequest;
import com.studio.core.domain.member.dto.member.response.MemberResponse;
import com.studio.core.domain.member.dto.member.response.TokenResponse;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.entity.RefreshTokenEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.domain.member.repository.RefreshTokenRepository;
import com.studio.core.global.enums.NotificationType;
import com.studio.core.global.enums.VerificationPurpose;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class MemberService {

    private final PasswordEncoder passwordEncoder;
    private final AuthTokenProvider authTokenProvider;

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    private final NotificationService notificationService;
    private final EmailVerificationService emailVerificationService;

    private final MemberValidator memberValidator;
    private final NaverImageClient naverImageClient;
    private final ImageFileValidator imageFileValidator;

    @Transactional
    public Long createSignup(
            String email,
            String password,
            String nickname,
            String phoneNumber,
            String name
    ) {
        emailVerificationService.consumeVerified(email, VerificationPurpose.SIGNUP);

        if(memberRepository.existsByEmail(email)){
            throw new CustomException(ErrorCode.MEMBER_EMAIL_DUPLICATE);
        }

        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(ErrorCode.MEMBER_NICKNAME_DUPLICATE);
        }

        memberValidator.validateNickname(nickname);
        memberValidator.validatePassword(password);
        memberValidator.validatePhoneNumber(phoneNumber);

        String encodePassword = passwordEncoder.encode(password);
        MemberEntity member = memberRepository.save(
                MemberEntity.create(email, encodePassword, nickname, phoneNumber, name)
        );

        notificationService.send(
                List.of(member.getMemberNo()),
                null,
                NotificationType.WELCOME,
                NotificationType.WELCOME.message(nickname),
                null);

        return member.getMemberNo();
    }

    @Transactional(readOnly = true)
    public boolean checkEmail(String email) {

        if(email == null || email.isBlank()) {
            throw new CustomException(
                    ErrorCode.MEMBER_EMAIL_DUPLICATE
            );
        }

        if(!email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
            throw new CustomException(
                    ErrorCode.INVALID_EMAIL
            );
        }
        return !memberRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        memberValidator.validateNickname(nickname);

        if (memberRepository.existsByNickname(nickname)) {
            throw new CustomException(
                    ErrorCode.MEMBER_NICKNAME_DUPLICATE
            );
        }
    }

    @Transactional(readOnly = true)
    public MemberResponse getMyProfile(
            Long memberNo
    ) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);

        return new MemberResponse(member);
    }

    @Transactional
    public Long updateProfile(
            Long memberNo,
            MemberPatchRequest req
    ) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);

        if (req.nickname() != null
                && !req.nickname().equals(member.getNickname())) {

            memberValidator.validateNickname(req.nickname());

            if (memberRepository.existsByNicknameAndMemberNoNot(
                    req.nickname(),
                    memberNo
            )) {
                throw new CustomException(
                        ErrorCode.MEMBER_NICKNAME_DUPLICATE
                );
            }
        }

        if (req.name() != null && !req.name().equals(member.getName())) {
            memberValidator.validateName(req.name());
        }

        if (req.phoneNumber() != null && !req.phoneNumber().isBlank()) {
            memberValidator.validatePhoneNumber(req.phoneNumber());
        }

        member.update(req);

        return memberNo;
    }

    @Transactional
    public void updatePassword(
            Long memberNo,
            PasswordUpdateRequest req
    ) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);

        if (member.getPassword() == null) {
            throw new CustomException(ErrorCode.SOCIAL_MEMBER_NO_PASSWORD);
        }

        if (!passwordEncoder.matches(
                req.currentPassword(),
                member.getPassword()
        )) {
            throw new CustomException(
                    ErrorCode.PASSWORD_NOT_MATCH
            );
        }

        memberValidator.validatePassword(req.newPassword());

        member.changePassword(
                passwordEncoder.encode(
                        req.newPassword()
                )
        );

        notificationService.send(
                List.of(memberNo), null, NotificationType.PASSWORD_CHANGE,
                NotificationType.PASSWORD_CHANGE.message(), null
        );
    }

    @Transactional
    public void resetPassword(PasswordResetRequest req) {
        emailVerificationService.consumeVerified(req.email(), VerificationPurpose.PASSWORD_RESET);

        MemberEntity member = memberRepository.getByEmailOrThrow(req.email());

        memberValidator.validatePassword(req.newPassword());
        member.changePassword(passwordEncoder.encode(req.newPassword()));

        notificationService.send(
                List.of(member.getMemberNo()), null, NotificationType.PASSWORD_CHANGE,
                NotificationType.PASSWORD_CHANGE.message(), null
        );
    }

    @Transactional
    public TokenResponse login(MemberLoginRequest req) {
        MemberEntity memberEntity = memberRepository.getByEmailOrThrow(req.email());

        if(
                memberEntity.getPassword() == null
                        || !passwordEncoder.matches(req.password(),
                 memberEntity.getPassword())
        ) {
            throw new CustomException(
                    ErrorCode.LOGIN_FAIL
            );
        }

        AuthToken accessToken =
                authTokenProvider.createAccessToken(
                        memberEntity.getMemberNo(),
                        memberEntity.getEmail(),
                        memberEntity.getRole()
                );

        AuthToken refreshToken =
                authTokenProvider.createRefreshToken(
                        memberEntity.getMemberNo()
                );

        refreshTokenRepository.save(
                new RefreshTokenEntity(
                        memberEntity.getMemberNo(),
                        refreshToken.getToken(),
                        authTokenProvider.refreshTokenExpiresAt()
                )
        );

        return new TokenResponse(
                accessToken.getToken(),
                refreshToken.getToken()
        );
    }

    @Transactional
    public String updateProfileImage (
            Long memberNo,
            MultipartFile file
    ) {
        imageFileValidator.validateImageFile(file);

        MemberEntity member = memberRepository.getOrThrow(memberNo);

        String imageUrl = naverImageClient.uploadImage(file);
        member.updateProfileImage(imageUrl);
        return imageUrl;
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) return;
        refreshTokenRepository.deleteByRefreshToken(refreshToken);
    }


}
