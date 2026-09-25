package com.studio.api.global.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VerificationMailSender {

    private final JavaMailSender javaMailSender;   // starter-mail이 yml 보고 자동 생성

    @Value("${spring.mail.username}")
    private String from;

    /** 인증 코드 메일. 요청 스레드를 붙잡지 않도록 비동기로 보낸다. */
    @Async
    public void sendVerificationCode(String to, String code, String purposeLabel, long ttlMinutes) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("OneSchedule <" + from + ">");
        message.setTo(to);
        message.setSubject("[OneSchedule] " + purposeLabel + " 인증 코드");
        message.setText("""
                인증 코드: %s

                %d분 안에 입력해 주세요.
                본인이 요청하지 않았다면 이 메일은 무시하셔도 됩니다.
                """.formatted(code, ttlMinutes));

        try {
            javaMailSender.send(message);
        } catch (MailException e) {
            log.error("[MAIL] 발송 실패 to={}", to, e);   // 사용자 잘못 아니므로 예외를 올리지 않음
        }
    }
}