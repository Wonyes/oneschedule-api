package com.studio.core.domain.member.repository;

import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.enums.AuthProvider;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface MemberRepository extends JpaRepository<MemberEntity, Long> {
    boolean existsByEmail(String email);
    boolean existsByNickname(String nickname);
    boolean existsByNicknameAndMemberNoNot(
            String nickname,
            Long memberNo
    );
    Optional<MemberEntity> findByProviderAndProviderId(AuthProvider provider, String ProviderId);
    Optional<MemberEntity> findByEmail(String email);

    default MemberEntity getOrThrow(Long memberNo) {
        return findById(memberNo)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND));
    }
    default MemberEntity getByEmailOrThrow(String email) {
        return findByEmail(email)
                .orElseThrow(() ->  new CustomException(ErrorCode.LOGIN_FAIL));
    }

    @Modifying
    @Query("update MemberEntity m set m.lastSeenAt = :now where m.memberNo = :memberNo")
    void touchLastSeen(
            @Param("memberNo") Long memberNo,
            @Param("now") LocalDateTime now
    );
}
