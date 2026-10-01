package com.studio.core.domain.group.entity;

import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.enums.JoinRequestStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tb_group_join_request",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_join_request",
                columnNames = {"group_no", "member_no"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupJoinRequestEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long requestNo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_no", nullable = false)
    private GroupEntity group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private JoinRequestStatus status;

    @Comment("가입 희망 메시지. 선택 입력이라 비어 있을 수 있다.")
    @Column(length = 200, columnDefinition = "VARCHAR2(200 CHAR)")
    private String message;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime processedAt;

    private Long processedBy;

    @Builder
    public GroupJoinRequestEntity(
            GroupEntity group,
            MemberEntity member,
            String message
    ) {
        this.group = group;
        this.member = member;
        this.message = message;
        this.status = JoinRequestStatus.PENDING;
        this.createdAt = LocalDateTime.now();
    }

    public void approve(Long processorNo) {
        this.status = JoinRequestStatus.APPROVED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processorNo;
    }

    public void reject(Long processorNo) {
        this.status = JoinRequestStatus.REJECTED;
        this.processedAt = LocalDateTime.now();
        this.processedBy = processorNo;
    }

    public void reopen(String message) {
        this.message = message;
        this.status = JoinRequestStatus.PENDING;
        this.processedAt = null;
        this.processedBy = null;
    }
}
