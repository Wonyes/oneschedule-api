package com.studio.api.domain.group.service;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.api.global.sse.SseEmitterRegistry;
import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupJoinRequestEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.domain.group.repository.GroupJoinRequestRepository;
import com.studio.core.domain.Schedule.repository.ScheduleParticipantRepository;
import com.studio.core.domain.group.repository.GroupMemberRepository;
import com.studio.core.domain.group.repository.GroupRepository;
import com.studio.core.domain.group.response.GroupMemberResponse;
import com.studio.core.domain.group.response.GroupResponse;
import com.studio.core.domain.group.response.JoinRequestResponse;
import com.studio.core.domain.group.response.MemberPresenceResponse;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.enums.GroupVisibility;
import com.studio.core.global.enums.JoinRequestStatus;
import com.studio.core.global.enums.NotificationType;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RequiredArgsConstructor
@Service
public class GroupMemberService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final ScheduleParticipantRepository scheduleParticipantRepository;
    private final MemberRepository memberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;

    private final GroupPermissionValidator permission;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final NotificationService notificationService;

    @Transactional
    public GroupResponse joinGroup(Long memberNo, String groupCode) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);

        GroupEntity group = groupRepository.getByCodeOrThrow(groupCode);

        if (group.getVisibility() == GroupVisibility.PUBLIC_APPROVAL) {
            throw new CustomException(ErrorCode.GROUP_JOIN_NOT_ALLOWED);
        }

        return addMember(group, member);
    }

    @Transactional(readOnly = true)
    public List<MemberPresenceResponse> getMemberPresence(
            Long groupNo, Long memberNo
    ) {
        permission.validateMember(groupNo, memberNo);

        return groupMemberRepository.findMembersOfGroups(List.of(groupNo))
                .stream()
                .map(GroupMemberEntity::getMember)
                .map(member -> MemberPresenceResponse.of(
                        member.getMemberNo(),
                        sseEmitterRegistry.isOnline(member.getMemberNo()),
                        member.getLastSeenAt()
                ))
                .toList();
    }

    @Transactional
    public GroupResponse joinPublicGroup(Long memberNo, Long groupNo) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);

        GroupEntity group = groupRepository.getOrThrow(groupNo);

        if (group.getVisibility() != GroupVisibility.PUBLIC_OPEN) {
            throw new CustomException(ErrorCode.GROUP_JOIN_NOT_ALLOWED);
        }

        return addMember(group, member);
    }

    @Transactional
    public GroupMemberResponse updateGroupMember(
            Long groupNo,
            Long requestMemberNo,
            Long targetMemberNo,
            GroupRole groupRole,
            String position
    ) {
        // 그룹장 위임은 권한 변경과 규칙이 달라 먼저 분기한다
        if (groupRole == GroupRole.SUPER) {
            return transferOwnership(groupNo, requestMemberNo, targetMemberNo, position);
        }

        permission.validateManager(groupNo, requestMemberNo);

        GroupMemberEntity target = groupMemberRepository.getOrThrow(groupNo, targetMemberNo);

        // 그룹장은 강등할 수 없다 (부관리자가 끌어내리는 것 방지)
        if (target.getGroupRole() == GroupRole.SUPER) {
            throw new CustomException(ErrorCode.UNAUTHORIZED_GROUP_ACCESS);
        }

        String groupName = target.getGroup().getGroupName();
        boolean roleChanged = groupRole != null && groupRole != target.getGroupRole();

        target.update(groupRole, position);

        if (roleChanged) {
            notificationService.send(
                    List.of(targetMemberNo),
                    requestMemberNo,
                    NotificationType.GROUP_ROLE_CHANGED,
                    NotificationType.GROUP_ROLE_CHANGED.message(groupName, roleLabel(groupRole)),
                    groupNo);
        }

        return GroupMemberResponse.from(target);
    }

    /**
     * 그룹장을 넘긴다. 넘긴 사람은 부관리자로 내려가 그룹장이 항상 한 명만 있게 한다.
     * validateManager는 부관리자도 통과시키므로 여기서는 validateOwner를 쓴다.
     */
    private GroupMemberResponse transferOwnership(
            Long groupNo,
            Long requestMemberNo,
            Long targetMemberNo,
            String position
    ) {
        if (requestMemberNo.equals(targetMemberNo)) {
            throw new CustomException(ErrorCode.CANNOT_TRANSFER_TO_SELF);
        }

        GroupMemberEntity owner = permission.validateOwner(groupNo, requestMemberNo);
        GroupMemberEntity target = groupMemberRepository.getOrThrow(groupNo, targetMemberNo);

        target.update(GroupRole.SUPER, position);
        owner.update(GroupRole.SUB, null);

        notificationService.send(
                List.of(targetMemberNo),
                requestMemberNo,
                NotificationType.GROUP_OWNER_TRANSFERRED,
                NotificationType.GROUP_OWNER_TRANSFERRED.message(target.getGroup().getGroupName()),
                groupNo);

        return GroupMemberResponse.from(target);
    }

    private String roleLabel(GroupRole role) {
        if (role == GroupRole.SUPER) return "그룹장";
        return role == GroupRole.SUB ? "관리자" : "멤버";
    }

    @Transactional
    public void leaveGroup(Long memberNo, Long groupNo) {

        GroupMemberEntity groupMember = permission.validateMember(groupNo, memberNo);

        if (groupMember.getGroupRole() == GroupRole.SUPER) {
            throw new CustomException(ErrorCode.GROUP_OWNER_CANNOT_LEAVE);
        }

        String nickname = groupMember.getMember().getNickname();
        String groupName = groupMember.getGroup().getGroupName();

        scheduleParticipantRepository.deleteAllByGroupMember(groupMember); // FK: 참여 기록 먼저
        groupMemberRepository.delete(groupMember);

        notificationService.send(managerNos(groupNo), memberNo, NotificationType.GROUP_MEMBER_LEFT,
                NotificationType.GROUP_MEMBER_LEFT.message(nickname, groupName),
                groupNo);
    }

    @Transactional
    public void deleteMember(Long groupNo, Long requestMemberNo, Long targetMemberNo) {

        permission.validateManager(groupNo, requestMemberNo);
        GroupMemberEntity target = groupMemberRepository.getOrThrow(groupNo, targetMemberNo);

        if (target.getGroupRole() == GroupRole.SUPER) {
            throw new CustomException(ErrorCode.CANNOT_DELETE_OWNER);
        }

        String groupName = target.getGroup().getGroupName();
        scheduleParticipantRepository.deleteAllByGroupMember(target); // FK: 참여 기록 먼저
        groupMemberRepository.delete(target);

        notificationService.send(
                List.of(targetMemberNo),
                requestMemberNo,
                NotificationType.GROUP_MEMBER_REMOVED,
                NotificationType.GROUP_MEMBER_REMOVED.message(groupName),
                null);
    }

    @Transactional
    public void requestJoin(Long groupNo, Long memberNo, String message) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);
        GroupEntity group = groupRepository.getOrThrow(groupNo);

        if (group.getVisibility() != GroupVisibility.PUBLIC_APPROVAL) {
            throw new CustomException(ErrorCode.GROUP_JOIN_NOT_ALLOWED);
        }

        if (groupMemberRepository.existsByGroupAndMember(group, member)) {
            throw new CustomException(ErrorCode.ALREADY_IN_GROUP);
        }

        if (message != null && message.length() > 200 ) {
            throw new CustomException(ErrorCode.INVALID_JOIN_MESSAGE);
        }

        groupJoinRequestRepository.findByGroupAndMember(group, member)
                .ifPresentOrElse(
                        request -> reopenRequest(request, message),
                        () -> groupJoinRequestRepository.save(
                                GroupJoinRequestEntity.builder()
                                        .group(group)
                                        .member(member)
                                        .message(message)
                                        .build()
                        )
                );

        notificationService.send(managerNos(group.getGroupNo()), memberNo, NotificationType.GROUP_JOIN_REQUESTED,
                NotificationType.GROUP_JOIN_REQUESTED.message(member.getNickname(), group.getGroupName()),
                groupNo);
    }

    @Transactional(readOnly = true)
    public Page<JoinRequestResponse> getJoinRequests(
            Long groupNo,
            Long memberNo,
            Pageable pageable
    ) {
        permission.validateManager(groupNo, memberNo);

        return groupJoinRequestRepository
                .findByGroupAndStatus(groupNo, JoinRequestStatus.PENDING, pageable)
                .map(JoinRequestResponse::from);
    }

    @Transactional
    public void processJoinRequest(
            Long groupNo,
            Long memberNo,
            Long requestNo,
            JoinRequestStatus status
    ) {
        permission.validateManager(groupNo, memberNo);

        GroupJoinRequestEntity request = groupJoinRequestRepository.getOrThrow(requestNo);

        if (!request.getGroup().getGroupNo().equals(groupNo)) {
            throw new CustomException(ErrorCode.JOIN_REQUEST_NOT_FOUND);
        }

        if (request.getStatus() != JoinRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.JOIN_REQUEST_ALREADY_PROCESSED);
        }

        GroupEntity group = request.getGroup();
        Long applicantNo = request.getMember().getMemberNo();
        boolean approved = status == JoinRequestStatus.APPROVED;

        if (approved) {
            addMember(group, request.getMember());
            request.approve(memberNo);
        } else {
            request.reject(memberNo);
        }

        notificationService.send(
                List.of(applicantNo), memberNo,
                approved ? NotificationType.GROUP_JOIN_APPROVED : NotificationType.GROUP_JOIN_REJECTED,
                (approved ? NotificationType.GROUP_JOIN_APPROVED : NotificationType.GROUP_JOIN_REJECTED)
                        .message(group.getGroupName()),
                groupNo);

    }

    private void reopenRequest(GroupJoinRequestEntity request, String message) {

        if (request.getStatus() == JoinRequestStatus.PENDING) {
            throw new CustomException(ErrorCode.JOIN_REQUEST_ALREADY_PENDING);
        }

        request.reopen(message);
    }

    private GroupResponse addMember(GroupEntity group, MemberEntity member) {

        if (groupMemberRepository.existsByGroupAndMember(group, member)) {
            throw new CustomException(ErrorCode.ALREADY_IN_GROUP);
        }

        GroupMemberEntity groupMember =
                GroupMemberEntity.create(group, member, GroupRole.MEMBER, null);


        groupMemberRepository.save(groupMember);

        notificationService.send(managerNos(group.getGroupNo()), member.getMemberNo(), NotificationType.GROUP_MEMBER_JOINED,
                NotificationType.GROUP_MEMBER_JOINED.message(member.getNickname(), group.getGroupName()),
                group.getGroupNo());

        return GroupResponse.from(group, groupMember);
    }

    private List<Long> managerNos(Long groupNo) {
        return groupMemberRepository.findMembersOfGroups(List.of(groupNo)).stream()
                .filter(gm -> gm.getGroupRole().isManager())
                .map(gm -> gm.getMember().getMemberNo())
                .toList();
    }
}
