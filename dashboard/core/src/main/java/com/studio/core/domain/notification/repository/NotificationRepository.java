package com.studio.core.domain.notification.repository;

import com.studio.core.domain.notification.entity.NotificationEntity;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {
    @Query(value = """
    select n from NotificationEntity n
    left join fetch n.sender
    where n.receiver.memberNo = :memberNo
    order by n.createAt desc, n.notificationNo desc
""", countQuery = """
    select count(n) from NotificationEntity n
    where n.receiver.memberNo = :memberNo
""")
    @EntityGraph(attributePaths = "sender")
    Page<NotificationEntity> findByReceiver(@Param("memberNo") Long memberNo, Pageable pageable);

    long countByReceiver_MemberNoAndReadAtIsNull(Long memberNo);

    @Modifying
    @Query("update NotificationEntity n set n.readAt = :now " +
            "where n.receiver.memberNo = :memberNo and n.readAt is null")
    int markAllRead(@Param("memberNo") Long memberNo, @Param("now") LocalDateTime now);

    default NotificationEntity getOrThrow(Long notificationNo) {
        return findById(notificationNo)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));
    }
    /** 탈퇴: 내가 받은 알림은 지운다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NotificationEntity n where n.receiver.memberNo = :memberNo")
    void deleteAllByReceiver(@Param("memberNo") Long memberNo);

    /** 탈퇴: 내가 보낸 알림은 남의 알림함에 있으므로 보낸이만 지운다 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update NotificationEntity n set n.sender = null where n.sender.memberNo = :memberNo")
    void detachSender(@Param("memberNo") Long memberNo);

    /** 정리용 — 읽은 지 오래된 알림 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NotificationEntity n where n.readAt is not null and n.readAt < :before")
    int deleteReadBefore(@Param("before") LocalDateTime before);

    /** 정리용 — 안 읽었어도 너무 오래된 알림 */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("delete from NotificationEntity n where n.createAt < :before")
    int deleteCreatedBefore(@Param("before") LocalDateTime before);

}
