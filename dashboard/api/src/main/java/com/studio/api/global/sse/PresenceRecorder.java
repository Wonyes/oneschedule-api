package com.studio.api.global.sse;

import com.studio.core.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class PresenceRecorder {

    private final MemberRepository memberRepository;

    @Transactional
    public void touch(Long memberNo) {
        memberRepository.touchLastSeen(memberNo, LocalDateTime.now());
    }
}
