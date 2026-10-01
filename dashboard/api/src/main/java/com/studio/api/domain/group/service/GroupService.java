package com.studio.api.domain.group.service;

import com.studio.api.domain.notification.service.NotificationService;
import com.studio.api.global.image.ImageFileValidator;
import com.studio.api.global.image.NaverImageClient;
import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.domain.Schedule.repository.ScheduleParticipantRepository;
import com.studio.core.domain.Schedule.repository.ScheduleRepository;
import com.studio.core.domain.group.repository.GroupJoinRequestRepository;
import com.studio.core.domain.group.repository.GroupMemberRepository;
import com.studio.core.domain.group.repository.GroupRepository;
import com.studio.core.domain.group.response.GroupMemberResponse;
import com.studio.core.domain.group.response.GroupResponse;
import com.studio.core.domain.group.response.PublicGroupResponse;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.enums.GroupVisibility;
import com.studio.core.global.enums.JoinRequestStatus;
import com.studio.core.global.enums.NotificationType;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import com.studio.core.global.util.GroupCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final MemberRepository memberRepository;
    private final GroupJoinRequestRepository groupJoinRequestRepository;
    private final ScheduleRepository scheduleRepository;
    private final ScheduleParticipantRepository scheduleParticipantRepository;

    private final GroupCodeGenerator groupCodeGenerator;
    private final GroupPermissionValidator permission;
    private final ImageFileValidator imageFileValidator;
    private final NaverImageClient naverImageClient;
    private final NotificationService notificationService;

    private String normalizeGroupName(String groupName) {
        String normalized = groupName == null ? "" : groupName.replaceAll("\\s+", "").toLowerCase();
        if (normalized.isEmpty() || normalized.length() > 20) {
            throw new CustomException(ErrorCode.INVALID_GROUP_NAME);
        }
        return normalized;
    }

    @Transactional
    public GroupResponse createGroup(Long memberNo, String groupName, String position) {

        MemberEntity member = memberRepository.getOrThrow(memberNo);
        String normalizedName = normalizeGroupName(groupName);

        if (groupRepository.existsByGroupName(normalizedName)) {
            throw new CustomException(ErrorCode.GROUP_NAME_DUPLICATED);
        }

        GroupEntity group = GroupEntity.create(normalizedName, groupCodeGenerator.generate());

        groupRepository.save(group);

        GroupMemberEntity groupMember =
                GroupMemberEntity.create(group, member, GroupRole.SUPER, position);

        groupMemberRepository.save(groupMember);

        return GroupResponse.from(group, groupMember);
    }

    @Transactional
    public GroupResponse updateGroupName(Long groupNo, Long memberNo, String groupName) {

        GroupMemberEntity owner = permission.validateOwner(groupNo, memberNo);

        String normalizedName = normalizeGroupName(groupName);

        if (groupRepository.existsByGroupNameAndGroupNoNot(normalizedName, groupNo)) {
            throw new CustomException(ErrorCode.GROUP_NAME_DUPLICATED);
        }

        GroupEntity group = owner.getGroup();

        group.updateGroupName(normalizedName);

        return GroupResponse.from(group, owner);
    }

    @Transactional
    public GroupResponse updateGroupSetting(
            Long groupNo,
            Long memberNo,
            GroupVisibility visibility,
            String description
    ) {
        GroupMemberEntity owner = permission.validateOwner(groupNo, memberNo);

        GroupEntity group = owner.getGroup();

        if (visibility != null) {
            group.updateVisibility(visibility);
        }

        if (description != null) {
            if (description.length() > 200) {
                throw new CustomException(ErrorCode.INVALID_GROUP_DESCRIPTION);
            }
            group.updateDescription(description);
        }

        return GroupResponse.from(group, owner);
    }

    @Transactional(readOnly = true)
    public Page<GroupResponse> getMyGroups(Long memberNo, Pageable pageable) {

        Page<GroupMemberEntity> myMemberships =
                groupMemberRepository.findMyGroups(memberNo, pageable);

        Map<Long, List<GroupMemberResponse>> membersByGroup = findMembers(
                myMemberships.getContent()
                        .stream()
                        .map(gm -> gm.getGroup().getGroupNo())
                        .toList()
        );

        return myMemberships.map(gm -> GroupResponse.from(
                gm.getGroup(),
                gm,
                membersByGroup.getOrDefault(gm.getGroup().getGroupNo(), List.of())
        ));
    }

    @Transactional(readOnly = true)
    public Page<PublicGroupResponse> getPublicGroups(
            Long memberNo, String keyword, Pageable pageable
    ) {

        String normalized = (keyword == null || keyword.isBlank()) ? null : keyword.trim();

        Page<PublicGroupResponse> groups = groupRepository.findPublicGroups(normalized, pageable);

        Set<Long> myGroupNos = groupMemberRepository.findGroupNosByMemberNo(memberNo);

        Set<Long> pendingGroupNos = groupJoinRequestRepository
                .findGroupNosByMemberAndStatus(memberNo, JoinRequestStatus.PENDING);

        groups.getContent().forEach(group -> {
            group.setJoined(myGroupNos.contains(group.getGroupNo()));
            group.setPending(pendingGroupNos.contains(group.getGroupNo()));
        });

        return groups;
    }

    @Transactional
    public void deleteGroup(Long groupNo, Long memberNo) {

        GroupMemberEntity owner = permission.validateOwner(groupNo, memberNo);

        GroupEntity group = owner.getGroup();

        List<Long> memberNos = groupMemberRepository.findMembersOfGroups(List.of(groupNo)).stream()
                .map(gm -> gm.getMember().getMemberNo())
                .toList();
        String groupName = group.getGroupName();

        // FK 순서: 일정 참여자 → 그룹 일정 → 가입 신청 → 그룹원 → 그룹
        scheduleParticipantRepository.deleteAllByGroupNo(groupNo);
        scheduleRepository.deleteAllByGroupNo(groupNo);
        groupJoinRequestRepository.deleteAllByGroup(group);
        groupMemberRepository.deleteAllByGroup(group);
        groupRepository.delete(group);

        notificationService.send(memberNos, memberNo, NotificationType.GROUP_DISBANDED,
                NotificationType.GROUP_DISBANDED.message(groupName),
                null);
    }

    @Transactional
    public String updateGroupProfileImage(
        Long memberNo,
        Long groupNo,
        MultipartFile file
    ) {
        GroupMemberEntity owner = permission.validateOwner(groupNo, memberNo);

        imageFileValidator.validateImageFile(file);

        GroupEntity group = owner.getGroup();

        String imageUrl = naverImageClient.uploadImage(file);
        group.updateProfileImage(imageUrl);
        return imageUrl;
    }

    private Map<Long, List<GroupMemberResponse>> findMembers(List<Long> groupNos) {

        if (groupNos.isEmpty()) return Map.of();

        return groupMemberRepository.findMembersOfGroups(groupNos)
                .stream()
                .collect(Collectors.groupingBy(
                        gm -> gm.getGroup().getGroupNo(),
                        LinkedHashMap::new,
                        Collectors.mapping(GroupMemberResponse::from, Collectors.toList())
                ));
    }

}
