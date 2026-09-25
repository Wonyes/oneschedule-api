package com.studio.api.domain.Schedule.service;

import com.studio.api.domain.group.service.GroupPermissionValidator;
import com.studio.api.domain.notification.service.NotificationService;
import com.studio.core.domain.Schedule.dto.ScheduleViewType;
import com.studio.core.domain.Schedule.dto.request.ScheduleRequest;
import com.studio.core.domain.Schedule.dto.response.ScheduleAuthorResponse;
import com.studio.core.domain.Schedule.dto.response.ScheduleResponse;
import com.studio.core.domain.Schedule.entity.ScheduleEntity;
import com.studio.core.domain.Schedule.entity.ScheduleParticipantEntity;
import com.studio.core.domain.Schedule.repository.ScheduleParticipantRepository;
import com.studio.core.domain.Schedule.repository.ScheduleRepository;
import com.studio.core.domain.group.response.GroupMemberResponse;
import com.studio.core.domain.group.entity.GroupEntity;
import com.studio.core.domain.group.entity.GroupMemberEntity;
import com.studio.core.domain.group.repository.GroupMemberRepository;
import com.studio.core.domain.member.entity.MemberEntity;
import com.studio.core.domain.member.repository.MemberRepository;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.enums.NotificationType;
import com.studio.core.global.exception.CustomException;
import com.studio.core.global.exception.ErrorCode;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import static java.util.stream.Collectors.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RequiredArgsConstructor
@Service
public class ScheduleService {

    private static final int UPCOMING_RANGE_DAYS = 7;

    private final MemberRepository memberRepository;
    private final ScheduleRepository scheduleRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupPermissionValidator permission;
    
    private final NotificationService notificationService;
    private final ScheduleParticipantRepository scheduleParticipantRepository;

    @Transactional
    public ScheduleResponse createSchedule(
            Long memberNo,
            ScheduleRequest request
    ) {
        validatePeriod(request);

        MemberEntity member = memberRepository.getOrThrow(memberNo);
        ScheduleEntity schedule =
                ScheduleEntity.createPersonal(
                        request,
                        member
                );

        scheduleRepository.save(schedule);

        return toResponse(schedule);
    }

    @Transactional
    public ScheduleResponse createGroupSchedule(
            Long memberNo,
            Long groupNo,
            ScheduleRequest request
    ) {
        validatePeriod(request);

        MemberEntity member = memberRepository.getOrThrow(memberNo);
        GroupEntity group = permission.validateMember(groupNo, memberNo).getGroup();

        ScheduleEntity schedule =
                ScheduleEntity.createGroupSchedule(
                        request,
                        member,
                        group
                );

        scheduleRepository.save(schedule);

        saveParticipants(schedule, group.getGroupNo(), request.participantMemberNos());
        notificationService.send(participantNos(schedule),
                memberNo,
                NotificationType.GROUP_SCHEDULE_CREATED,
                NotificationType.GROUP_SCHEDULE_CREATED.message(schedule.getTitle(), when(schedule)),
                groupNo, schedule.getStartDate());

        return toResponse(schedule);
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getSchedules(
            Long groupNo,
            Long memberNo,
            ScheduleViewType type
    ) {
        return switch (type) {
            case PERSONAL -> getPersonalSchedules(memberNo);
            case GROUP -> getGroupSchedules(groupNo, memberNo);
        };
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getPersonalSchedules(
            Long memberNo
    ) {
        return toResponses(scheduleRepository.findPersonalWithMember(memberNo));
    }

    @Transactional(readOnly = true)
    public List<ScheduleResponse> getGroupSchedules(
            Long groupNo,
            Long memberNo
    ) {
        permission.validateMember(groupNo, memberNo);
        return toResponses(scheduleRepository.findByGroupNoWithMember(groupNo));
    }

    @Transactional
    public ScheduleResponse updateSchedule(Long id, Long memberNo, ScheduleRequest request) {
        validatePeriod(request);
        ScheduleEntity schedule = scheduleRepository.getOrThrow(id);
        validateWriteAccess(schedule, memberNo);

        Set<Long> before = explicitParticipantNos(schedule);
        LocalDate beforeDate = schedule.getStartDate();
        LocalTime beforeTime = schedule.getStartTime();

        schedule.update(request);
        scheduleParticipantRepository.deleteAllBySchedule(schedule);

        if (schedule.getGroup() != null) {
            saveParticipants(schedule, schedule.getGroup().getGroupNo(), request.participantMemberNos());
        }

        boolean whenChanged = !Objects.equals(beforeDate, schedule.getStartDate())
                || !Objects.equals(beforeTime, schedule.getStartTime());

        if (schedule.getGroup() != null) {
            Long groupNo = schedule.getGroup().getGroupNo();
            Set<Long> after = explicitParticipantNos(schedule);

            Set<Long> added = new HashSet<>(after);  added.removeAll(before);    // 새로 들어온 사람
            Set<Long> removed = new HashSet<>(before); removed.removeAll(after); // 빠진 사람
            Set<Long> kept = new HashSet<>(after);   kept.retainAll(before);     // 계속 있는 사람

            if (!added.isEmpty())
                notificationService.send(added, memberNo, NotificationType.SCHEDULE_PARTICIPANT_ADDED,
                        NotificationType.SCHEDULE_PARTICIPANT_ADDED.message(schedule.getTitle(), when(schedule)),
                        groupNo, schedule.getStartDate());

            if (!removed.isEmpty())
                notificationService.send(removed, memberNo, NotificationType.SCHEDULE_PARTICIPANT_REMOVED,
                        NotificationType.SCHEDULE_PARTICIPANT_REMOVED.message(schedule.getTitle()), groupNo);

            if (whenChanged && !kept.isEmpty())        // 시간 변경은 "계속 있는 사람"에게만 (추가된 사람은 위에서 이미 시간 받음)
                notificationService.send(kept, memberNo, NotificationType.GROUP_SCHEDULE_UPDATED,
                        NotificationType.GROUP_SCHEDULE_UPDATED.message(schedule.getTitle(), when(schedule)),
                        groupNo, schedule.getStartDate());
        }

        return toResponse(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id, Long memberNo) {
        ScheduleEntity schedule = scheduleRepository.getOrThrow(id);
        validateWriteAccess(schedule, memberNo);

        List<Long> receivers = schedule.getGroup() != null ? participantNos(schedule) : List.of();
        String content = NotificationType.GROUP_SCHEDULE_DELETED.message(schedule.getTitle(), when(schedule));
        Long groupNo = schedule.getGroup() != null ? schedule.getGroup().getGroupNo() : null;

        scheduleParticipantRepository.deleteAllBySchedule(schedule);
        scheduleRepository.delete(schedule);

        if (!receivers.isEmpty()) {
            notificationService.send(receivers, memberNo, NotificationType.GROUP_SCHEDULE_DELETED, content, groupNo);
        }
    }

    private void validateWriteAccess(ScheduleEntity schedule, Long memberNo) {
        // 작성자가 탈퇴하면 null이 된다. 그때는 아래 그룹 관리자 규칙으로만 판단한다
        if (schedule.getMember() != null && schedule.getMember().getMemberNo().equals(memberNo)) {
            return;
        }

        if (schedule.getGroup() != null) {
            Optional<GroupRole> groupRole =
                    groupMemberRepository.findByGroup_GroupNoAndMember_MemberNo(
                            schedule.getGroup().getGroupNo(), memberNo
                    ).map(GroupMemberEntity::getGroupRole);

                if (groupRole.isPresent() && groupRole.get().isManager()) {
                    return;
                }
        }

        throw new CustomException(ErrorCode.SCHEDULE_ACCESS_DENIED);
    }

    private void saveParticipants(
            ScheduleEntity schedule,
            Long groupNo,
            List<Long> participantMemberNos
    ) {
        if (participantMemberNos == null || participantMemberNos.isEmpty()) return;

        Set<Long> memberNos = new LinkedHashSet<>(participantMemberNos);

        List<GroupMemberEntity> groupMembers =
                groupMemberRepository.findByGroupNoAndMemberNos(groupNo, memberNos);

        if (groupMembers.size() != memberNos.size()) {
            throw new CustomException(ErrorCode.GROUP_MEMBER_NOT_FOUND);
        }

        List<ScheduleParticipantEntity> participants =
                groupMembers.stream()
                        .map(gm -> ScheduleParticipantEntity.builder()
                                .schedule(schedule)
                                .groupMember(gm)
                                .build())
                        .toList();

        scheduleParticipantRepository.saveAll(participants);
    }

    private ScheduleResponse toResponse(ScheduleEntity schedule) {
        return toResponses(List.of(schedule)).get(0);
    }

    private List<ScheduleResponse> toResponses(List<ScheduleEntity> schedules) {
        if (schedules.isEmpty()) return List.of();

        Map<Long, List<GroupMemberEntity>> participantsBySchedule =
                scheduleParticipantRepository.findBySchedules(schedules).stream()
                        .collect(groupingBy(
                                p -> p.getSchedule().getId(),
                                mapping(ScheduleParticipantEntity::getGroupMember, toList())
                        ));

        Set<Long> groupNos = schedules.stream()
                .map(ScheduleEntity::getGroup)
                .filter(Objects::nonNull)
                .map(GroupEntity::getGroupNo)
                .collect(toSet());

        Map<Long, Map<Long, GroupMemberEntity>> membersByGroup = groupNos.isEmpty()
                ? Map.of()
                : groupMemberRepository.findMembersOfGroups(groupNos).stream()
                        .collect(groupingBy(
                                gm -> gm.getGroup().getGroupNo(),
                                toMap(gm -> gm.getMember().getMemberNo(), gm -> gm, (a, b) -> a, LinkedHashMap::new)
                        ));

        return schedules.stream().map(schedule -> {
            Long groupNo = schedule.getGroup() != null ? schedule.getGroup().getGroupNo() : null;
            Map<Long, GroupMemberEntity> members =
                    groupNo == null ? Map.of() : membersByGroup.getOrDefault(groupNo, Map.of());

            List<GroupMemberEntity> participants =
                    participantsBySchedule.getOrDefault(schedule.getId(), List.of());

            // 작성자가 탈퇴하면 member가 null이 된다 (그룹 일정은 남기고 표시만 지움)
            GroupMemberEntity author =
                    (groupNo == null || schedule.getMember() == null)
                            ? null
                            : members.get(schedule.getMember().getMemberNo());

            return ScheduleResponse.from(
                    schedule,
                    author != null ? ScheduleAuthorResponse.from(author) : null,
                    participants.stream().map(GroupMemberResponse::from).toList()
            );
        }).toList();
    }

    /**
     * 여러 일정의 알림 대상(참여자 memberNo)을 한 번에 구한다. 일정 수와 무관하게 쿼리는 최대 2번.
     * 참여자를 지정하지 않은 그룹 일정은 그룹 전원이 대상이다.
     */
    public Map<Long, List<Long>> participantNosBySchedule(List<ScheduleEntity> schedules) {
        if (schedules.isEmpty()) return Map.of();

        Map<Long, List<Long>> explicit =
                scheduleParticipantRepository.findBySchedules(schedules).stream()
                        .collect(groupingBy(
                                p -> p.getSchedule().getId(),
                                mapping(p -> p.getGroupMember().getMember().getMemberNo(), toList())
                        ));

        Set<Long> fallbackGroupNos = schedules.stream()
                .filter(s -> s.getGroup() != null && !explicit.containsKey(s.getId()))
                .map(s -> s.getGroup().getGroupNo())
                .collect(toSet());

        Map<Long, List<Long>> membersByGroup = fallbackGroupNos.isEmpty()
                ? Map.of()
                : groupMemberRepository.findMembersOfGroups(fallbackGroupNos).stream()
                        .collect(groupingBy(
                                gm -> gm.getGroup().getGroupNo(),
                                mapping(gm -> gm.getMember().getMemberNo(), toList())
                        ));

        Map<Long, List<Long>> result = new HashMap<>();
        for (ScheduleEntity s : schedules) {
            List<Long> nos = explicit.get(s.getId());
            if (nos == null && s.getGroup() != null) {
                nos = membersByGroup.getOrDefault(s.getGroup().getGroupNo(), List.of());
            }
            result.put(s.getId(), nos == null ? List.of() : nos);
        }
        return result;
    }

    /** 명시적으로 지정된 참여자만 (미지정 → 빈 집합). 추가/제외 diff 계산용. */
    private Set<Long> explicitParticipantNos(ScheduleEntity schedule) {
        return scheduleParticipantRepository.findBySchedules(List.of(schedule)).stream()
                .map(p -> p.getGroupMember().getMember().getMemberNo())
                .collect(toSet());
    }

    public List<Long> participantNos(ScheduleEntity schedule) {
        return participantNosBySchedule(List.of(schedule))
                .getOrDefault(schedule.getId(), List.of());
    }

    /** 알림 본문용 "M/d HH:mm". 종일 일정은 날짜만. */
    public static String when(ScheduleEntity s) {
        String date = s.getStartDate().format(DateTimeFormatter.ofPattern("M/d"));
        return s.getStartTime() == null
                ? date
                : date + " " + s.getStartTime().format(DateTimeFormatter.ofPattern("HH:mm"));
    }

    /** 일정 시작,종료 역전 허용 */
    public void validatePeriod (ScheduleRequest req) {
        LocalDate endDate = req.endDate() != null ? req.endDate() : req.startDate();

        if (endDate.isBefore(req.startDate())) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_PERIOD);
        }

        if (endDate.isEqual(req.startDate())
                && req.startTime() != null && req.endTime() != null
                && req.endTime().isBefore(req.startTime())) {
            throw new CustomException(ErrorCode.INVALID_SCHEDULE_PERIOD);
        }
    }
}
