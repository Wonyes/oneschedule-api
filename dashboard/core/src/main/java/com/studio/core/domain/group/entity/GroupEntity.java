package com.studio.core.domain.group.entity;

import com.studio.core.global.enums.GroupVisibility;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Comment;

@Entity
@Table(name = "tb_group")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupNo;

    @Column(nullable = false, unique = true)
    private String groupName;

    @Column(nullable = false, unique = true)
    private String groupCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private GroupVisibility visibility;

    @Comment("그룹 소개")
    @Column(length = 200, columnDefinition = "VARCHAR2(200 CHAR)")
    private String description;

    @Column
    private String profileImageUrl;
    
    @Builder
    public GroupEntity(
            String groupName,
            String groupCode,
            GroupVisibility visibility,
            String profileImageUrl
    ) {
        this.groupName = groupName;
        this.groupCode = groupCode;
        this.visibility = visibility != null ? visibility : GroupVisibility.PRIVATE;
        this.profileImageUrl = profileImageUrl;
    }

    public static GroupEntity create(
            String groupName,
            String groupCode
    ) {
        return GroupEntity.builder()
                .groupName(groupName)
                .groupCode(groupCode)
                .build();
    }

    public void updateGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void updateVisibility(GroupVisibility visibility) {
        this.visibility = visibility;
    }

    public void updateDescription (String description) {
        this.description = description;
    }

    public void updateProfileImage(String profileImageUrl) {
        this.profileImageUrl = profileImageUrl;
    }

}