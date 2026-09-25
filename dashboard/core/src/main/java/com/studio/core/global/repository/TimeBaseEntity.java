package com.studio.core.global.repository;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PreUpdate;
import lombok.Getter;
import org.hibernate.annotations.Comment;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@EntityListeners(AuditingEntityListener.class)
@MappedSuperclass
@Getter
public class TimeBaseEntity {
    @CreationTimestamp
    @Column(updatable = false)
    @Comment("생성시간")
    private LocalDateTime createAt;

    @UpdateTimestamp
    @Column(insertable = false)
    @Comment("수정 시간")
    private LocalDateTime updateAt;

    @PreUpdate
    protected void onUpdate() {
        this.updateAt = LocalDateTime.now();
    }
}