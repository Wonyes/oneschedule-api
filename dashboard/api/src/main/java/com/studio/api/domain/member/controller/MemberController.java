package com.studio.api.domain.member.controller;

import com.studio.api.domain.member.service.EmailVerificationService;
import com.studio.api.global.auth.LoginMember;
import com.studio.api.global.config.CookieProperties;
import com.studio.core.domain.member.dto.member.request.MemberLoginRequest;
import com.studio.core.domain.member.dto.member.request.MemberPatchRequest;
import com.studio.core.domain.member.dto.member.request.MemberSignupRequest;
import com.studio.api.domain.member.service.MemberService;
import com.studio.api.domain.member.service.MemberWithdrawService;
import com.studio.core.domain.member.dto.member.request.PasswordResetRequest;
import com.studio.core.domain.member.dto.member.request.PasswordUpdateRequest;
import com.studio.core.domain.member.dto.member.response.ProfileImageResponse;
import com.studio.core.domain.member.dto.member.response.TokenResponse;
import com.studio.core.global.enums.VerificationPurpose;
import com.studio.core.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/v1/api/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final MemberWithdrawService memberWithdrawService;
    private final EmailVerificationService emailVerificationService;

    private final CookieProperties cookieProperties;

    @Operation(summary = "회원 가입")
    @PostMapping("/signup")
    public SuccessResponse<Long> signup(
           @Valid @RequestBody MemberSignupRequest req
    ) {
        return SuccessResponse.ok(
                memberService.createSignup(
                        req.email(),
                        req.password(),
                        req.nickname(),
                        req.phoneNumber(),
                        req.name()
                )
        );
    }

    @GetMapping("/email-check")
    public SuccessResponse<?> checkEmail(
            @RequestParam("email") String email
    ) {
        return SuccessResponse.ok(memberService.checkEmail(email));
    }

    @GetMapping("/nickname-check")
    public SuccessResponse<?> checkNickname(
            @RequestParam("nickname") String nickname
    ) {
        memberService.checkNickname(nickname);

        return SuccessResponse.ok(true);
    }

    @PostMapping("/email-verification/request")
    public SuccessResponse<?> checkEmailRequest(
            @RequestParam("email") String email,
            @RequestParam("purpose") VerificationPurpose purpose
    ) {
        emailVerificationService.request(email, purpose);

        return SuccessResponse.ok(true);
    }

    @PostMapping("/email-verification/verify")
    public SuccessResponse<?> requestVerify(
            @RequestParam("email") String email,
            @RequestParam("purpose") VerificationPurpose purpose,
            @RequestParam("code") String code
            ) {
        emailVerificationService.verify(email, purpose, code);

        return SuccessResponse.ok(true);
    }

    @Operation(summary = "로그인")
    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<Void>> login(@Valid @RequestBody MemberLoginRequest req) {

        TokenResponse token = memberService.login(req);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, cookieProperties.access(token.accessToken()).toString())
                .header(HttpHeaders.SET_COOKIE, cookieProperties.refresh(token.refreshToken()).toString())
                .body(SuccessResponse.ok());
    }

    @GetMapping("/info")
    public SuccessResponse<?> getMyProfile(
            @LoginMember Long memberNo
    ) {

        return SuccessResponse.ok(
                memberService.getMyProfile(
                        memberNo
                )
        );
    }

    @Operation(summary = "회원정보수정")
    @PatchMapping("/info")
    public SuccessResponse<Long> updateMember(
            @LoginMember Long memberNo,
            @Valid @RequestBody MemberPatchRequest req
            ){
        return SuccessResponse.ok(memberService.updateProfile(memberNo, req));
    }

    @Operation(summary = "비밀번호 변경")
    @PutMapping("/password")
    public SuccessResponse<Void> updatePassword(
            @LoginMember Long memberNo,
            @Valid @RequestBody PasswordUpdateRequest req
            ) {
        memberService.updatePassword(memberNo, req);
        return SuccessResponse.ok();
    }

    @Operation(summary = "비밀번호 재설정 (이메일 인증 후)")
    @PostMapping("/password-reset")
    public SuccessResponse<Void> resetPassword(
            @Valid @RequestBody PasswordResetRequest req
    ) {
        memberService.resetPassword(req);
        return SuccessResponse.ok();
    }

    @Operation(summary = "프로필 사진 업로드")
    @PostMapping("/profile-image")
    public SuccessResponse<ProfileImageResponse> profileImageUpload(
            @LoginMember Long memberNo,
            @RequestParam("file") MultipartFile file
    ) {
        String imageUrl = memberService.updateProfileImage(memberNo, file);

        return SuccessResponse.ok(new ProfileImageResponse(imageUrl));
    }


    @PostMapping("/logout")
    public SuccessResponse<Void> logout(
            @CookieValue(value = CookieProperties.REFRESH, required = false) String refreshToken,
            HttpServletResponse response
    ) {
        memberService.logout(refreshToken);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieProperties.expire(CookieProperties.ACCESS).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieProperties.expire(CookieProperties.REFRESH).toString());

        return SuccessResponse.ok();
    }

    @Operation(summary = "회원 탈퇴")
    @DeleteMapping
    public SuccessResponse<Void> withdraw(
            @LoginMember Long memberNo,
            HttpServletResponse response
    ) {
        memberWithdrawService.withdraw(memberNo);

        response.addHeader(HttpHeaders.SET_COOKIE, cookieProperties.expire(CookieProperties.ACCESS).toString());
        response.addHeader(HttpHeaders.SET_COOKIE, cookieProperties.expire(CookieProperties.REFRESH).toString());

        return SuccessResponse.ok();
    }
}
