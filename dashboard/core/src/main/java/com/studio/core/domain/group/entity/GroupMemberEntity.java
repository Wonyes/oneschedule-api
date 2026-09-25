package com.studio.core.domain.group.entity;

import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.global.enums.GroupRole;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "tb_group_member",
        uniqueConstraints = {
                @UniqueConstraint(
                        columnNames = {"group_no", "member_no"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GroupMemberEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long groupMemberNo;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_no", nullable = false)
    private GroupEntity group;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no", nullable = false)
    private MemberEntity member;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GroupRole groupRole;

    private String position;


    @Builder
    public GroupMemberEntity(
            GroupEntity group,
            MemberEntity member,
            GroupRole groupRole,
            String position
    ) {
        this.group = group;
        this.member = member;
        this.groupRole = groupRole;
        this.position = position;
    }


    public static GroupMemberEntity create(
            GroupEntity group,
            MemberEntity member,
            GroupRole groupRole,
            String position
    ) {
        return GroupMemberEntity.builder()
                .group(group)
                .member(member)
                .groupRole(groupRole)
                .position(position)
                .build();
    }

    public void update(
            GroupRole groupRole,
            String position
    ) {
        if(groupRole != null){
            this.groupRole = groupRole;
        }

        if(position != null){
            this.position = position;
        }
    }

}