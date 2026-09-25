package com.studio.core.domain.member.repository;

import com.studio.core.domain.member.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    Optional<RefreshTokenEntity> findByRefreshToken(String refreshToken);
    void deleteByRefreshToken(String refreshToken);
    long deleteAllByExpiresAtBefore(LocalDateTime now);

    void deleteAllByMemberNo(Long memberNo);

}
