package com.studio.api.domain.member.validator;

import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.stereotype.Component;

@Component
public class MemberValidator {

    public void validateNickname(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            throw new CustomException(
                    ErrorCode.INVALID_NICKNAME
            );
        }

        int score = 0;

        for (char c : nickname.toCharArray()) {
            if (Character.toString(c).matches("[가-힣]")) {
                score += 2;
            } else if (Character.toString(c).matches("[a-zA-Z0-9]")) {
                score += 1;
            } else {
                throw new CustomException(
                        ErrorCode.INVALID_NICKNAME
                );
            }
        }

        if (score < 4 || score > 10) {
            throw new CustomException(
                    ErrorCode.INVALID_NICKNAME
            );

        }
    }

    public void validatePhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()
                || !phoneNumber.matches("01[016789]\\d{8}")) {
            throw new CustomException(
                    ErrorCode.INVALID_PHONE_NUMBER
            );
        }
    }

    public void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_NAME);
        }

        String trimmed = name.trim();

        if (trimmed.length() < 2 || trimmed.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_NAME);
        }

        if (!trimmed.matches("[\\p{L}][\\p{L} .'-]*")) {
            throw new CustomException(ErrorCode.INVALID_NAME);
        }
    }

    public void validatePassword(String password) {
        if (password == null || password.isBlank()
                || password.length() < 8
                || password.length() > 20
                || !password.matches(".*[A-Za-z].*")
                || !password.matches(".*\\d.*")
                || !password.matches(".*[!@#$%^&*].*")) {
            throw new CustomException(
                    ErrorCode.INVALID_PASSWORD
            );
        }
    }
}
