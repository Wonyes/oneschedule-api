package com.studio.api.domain.member.service;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.domain.group.repository.GroupJoinRequestRepository;
import com.studio.core.domain.group.repository.GroupMemberRepository;
import com.studio.core.domain.group.repository.GroupRepository;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.domain.member.repository.RefreshTokenRepository;
import com.studio.core.domain.notification.repository.NotificationRepository;
import com.studio.core.domain.Schedule.repository.ScheduleParticipantRepository;
import com.studio.core.domain.Schedule.repository.ScheduleRepository;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.enums.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 회원 탈퇴. 개인정보처리방침에 적은 대로
 * "개인 일정은 사라지고, 그룹 일정은 그룹에 남되 작성자 표시만 사라진다".
 *
 * 다른 테이블이 회원을 참조하고 있어 삭제 순서가 중요하다.
 * 참여자 → 그룹원 → 가입신청 → 알림 → 토큰 → 회원.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MemberWithdrawService {

    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository scheduleParticipantRepository;
    private final NotificationRepository notificationRepository;
    private final NotificationService notificationService;

    @Transactional
    public void withdraw(Long memberNo) {
        MemberEntity member = memberRepository.getOrThrow(memberNo);

        // 1. 그룹장으로 있는 그룹은 넘기거나 해체한다. 주인 없는 그룹을 남기지 않는다
        for (GroupMemberEntity myMembership : groupMemberRepository.findAllByMember_MemberNo(memberNo)) {
            if (myMembership.getGroupRole() == GroupRole.SUPER) {
                handOverOrDisband(myMembership, memberNo);
            }
        }

        // 2. 일정 — 그룹 일정은 남기고 작성자만 끊고, 개인 일정은 지운다
        scheduleRepository.detachAuthor(memberNo);
        scheduleRepository.deletePersonalOf(memberNo);

        // 3. 그룹 관련 — 참여자가 그룹원을 참조하므로 참여자부터
        for (GroupMemberEntity myMembership : groupMemberRepository.findAllByMember_MemberNo(memberNo)) {
            scheduleParticipantRepository.deleteAllByGroupMember(myMembership);
        }
        groupMemberRepository.deleteAll(groupMemberRepository.findAllByMember_MemberNo(memberNo));
        groupJoinRequestRepository.deleteAllByMember(memberNo);

        // 4. 알림 — 받은 건 지우고, 보낸 건 남의 알림함에 있으니 보낸이만 끊는다
        notificationRepository.deleteAllByReceiver(memberNo);
        notificationRepository.detachSender(memberNo);

        // 5. 토큰 → 회원
        refreshTokenRepository.deleteAllByMemberNo(memberNo);
        memberRepository.delete(member);

        log.info("[WITHDRAW] memberNo={} 탈퇴 완료", memberNo);
    }

    /** 관리자 우선, 없으면 가장 오래된 멤버에게 넘긴다. 혼자면 그룹을 해체한다 */
    private void handOverOrDisband(GroupMemberEntity myMembership, Long memberNo) {
        GroupEntity group = myMembership.getGroup();
        Long groupNo = group.getGroupNo();

        List<GroupMemberEntity> candidates =
                groupMemberRepository.findSuccessorCandidates(groupNo, memberNo);

        if (candidates.isEmpty()) {
            disband(group);
            return;
        }

        GroupMemberEntity successor = candidates.get(0);
        successor.update(GroupRole.SUPER, null);
        myMembership.update(GroupRole.MEMBER, null);

        notificationService.send(
                List.of(successor.getMember().getMemberNo()),
                memberNo,
                NotificationType.GROUP_OWNER_TRANSFERRED,
                NotificationType.GROUP_OWNER_TRANSFERRED.message(group.getGroupName()),
                groupNo);
    }

    /** GroupService.deleteGroup과 같은 순서. 권한 검사는 이미 끝났으므로 생략한다 */
    private void disband(GroupEntity group) {
        Long groupNo = group.getGroupNo();

        scheduleParticipantRepository.deleteAllByGroupNo(groupNo);
        scheduleRepository.deleteAllByGroupNo(groupNo);
        groupJoinRequestRepository.deleteAllByGroup(group);
        groupMemberRepository.deleteAllByGroup(group);
        groupRepository.delete(group);
    }
}
