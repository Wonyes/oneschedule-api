package com.studio.api.domain.group.controller;

import com.studio.api.domain.group.service.GroupMemberService;
import com.studio.api.domain.group.service.GroupService;
import com.studio.api.global.auth.LoginMember;
import com.studio.core.domain.group.response.GroupMemberResponse;
import com.studio.core.domain.group.response.GroupResponse;
import com.studio.core.domain.group.response.JoinRequestResponse;
import com.studio.core.domain.group.response.MemberPresenceResponse;
import com.studio.core.domain.group.response.PublicGroupResponse;
import com.studio.core.domain.member.dto.member.response.ProfileImageResponse;
import com.studio.core.global.enums.GroupVisibility;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import com.studio.core.global.enums.GroupRole;
import com.studio.core.global.response.PageResponse;
import com.studio.core.global.enums.JoinRequestStatus;

import com.studio.core.global.response.SuccessResponse;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


@RestController
@RequestMapping("/v1/api/group")
@RequiredArgsConstructor
public class GroupController {

    private final GroupService groupService;
    private final GroupMemberService groupMemberService;

    @Operation(summary = "그룹 생성하기")
    @PostMapping("/create")
    public SuccessResponse<GroupResponse> createGroup(
            @RequestParam String groupName,
            @RequestParam String position,
            @LoginMember Long memberNo
    ) {

        return SuccessResponse.ok(
                groupService.createGroup(
                        memberNo,
                        groupName,
                        position
                )
        );
    }

    @Operation(summary = "그룹 가입")
    @PostMapping("/join")
    public SuccessResponse<GroupResponse> joinGroup(
            @LoginMember Long memberNo,
            @RequestParam String groupCode
    ){
        return SuccessResponse.ok(
                groupMemberService.joinGroup(
                        memberNo,
                        groupCode
                )
        );
    }

    @Operation(summary = "공개 그룹 즉시 가입")
    @PostMapping("/{groupNo}/join")
    public SuccessResponse<GroupResponse> joinPublicGroup(
            @LoginMember Long memberNo,
            @PathVariable Long groupNo
    ) {
        return SuccessResponse.ok(
                groupMemberService.joinPublicGroup(memberNo, groupNo)
        );
    }

    @Operation(summary = "그룹명 수정")
    @PutMapping("/group-name/{groupNo}")
    public SuccessResponse<GroupResponse> updateGroup(
            @PathVariable Long groupNo,
            @RequestParam String groupName,
            @LoginMember Long memberNo
    ) {

        return SuccessResponse.ok(
                groupService.updateGroupName(
                        groupNo,
                        memberNo,
                        groupName
                )
        );
    }

    @Operation(summary = "그룹원 수정")
    @PatchMapping("/{groupNo}/member/{memberNo}")
    public SuccessResponse<GroupMemberResponse> updateGroupMember(
            @PathVariable Long groupNo,
            @PathVariable Long memberNo,
            @LoginMember Long requestMemberNo,
            @RequestParam GroupRole groupRole,
            @RequestParam String position
    ) {

        return SuccessResponse.ok(
                groupMemberService.updateGroupMember(
                        groupNo,
                        requestMemberNo,
                        memberNo,
                        groupRole,
                        position
                )
        );
    }

    @Operation(summary = "내 그룹 목록")
    @GetMapping("/my/groups")
    public SuccessResponse<PageResponse<GroupResponse>> getMyGroups(
            @LoginMember Long memberNo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return SuccessResponse.ok(
                PageResponse.from(
                        groupService.getMyGroups(memberNo, pageable)
                )
        );
    }

    @Operation(summary = "그룹 해체")
    @DeleteMapping("/{groupNo}/disband")
    public SuccessResponse<Void> disbandGroup(
            @PathVariable Long groupNo,
            @LoginMember Long memberNo

    ) {

        groupService.deleteGroup(
                groupNo,
                memberNo
        );


        return SuccessResponse.ok();
    }

    @Operation(summary = "그룹 탈퇴")
    @DeleteMapping("/leave")
    public SuccessResponse<Void> leaveGroup(
            @LoginMember Long memberNo,
            @RequestParam Long groupNo
    ) {

        groupMemberService.leaveGroup(memberNo, groupNo);


        return SuccessResponse.ok();
    }

    @Operation(summary = "그룹원 삭제")
    @DeleteMapping("/{groupNo}/member/{memberNo}")
    public SuccessResponse<Void> deleteMember(
            @PathVariable Long groupNo,
            @PathVariable Long memberNo,
            @LoginMember Long requestMemberNo
    ) {

        groupMemberService.deleteMember(
                groupNo,
                requestMemberNo,
                memberNo
        );


        return SuccessResponse.ok();
    }

    @Operation(summary = "공개 그룹 목록")
    @GetMapping("/public")
    public SuccessResponse<PageResponse<PublicGroupResponse>> getPublicGroups(
            @LoginMember Long memberNo,
            @RequestParam(required = false) String keyword,
            @PageableDefault(
                    size = 20,
                    sort = "groupNo",
                    direction = Sort.Direction.DESC
            )
            Pageable pageable
    ) {
        return SuccessResponse.ok(
                PageResponse.from(
                        groupService.getPublicGroups
                                (memberNo, keyword, pageable)
                )
        );
    }

    @Operation(summary = "그룹 공개 설정")
    @PatchMapping("/{groupNo}/setting")
    public SuccessResponse<GroupResponse> updateGroupSetting(
            @PathVariable Long groupNo,
            @RequestParam(required = false) GroupVisibility visibility,
            @RequestParam(required = false) String description,
            @LoginMember Long memberNo
    ) {
        return SuccessResponse.ok(
                groupService.updateGroupSetting(
                        groupNo, memberNo,
                        visibility, description
                )
        );
    }
    @Operation(summary = "가입 신청")
    @PostMapping("/{groupNo}/join-request")
    public SuccessResponse<Void> requestJoin(
            @PathVariable Long groupNo,
            @LoginMember Long memberNo,
            @RequestParam(required = false) String message
    ) {
        groupMemberService.requestJoin(groupNo, memberNo, message);

        return SuccessResponse.ok();
    }

    @Operation(summary = "가입 신청 목록")
    @GetMapping("/{groupNo}/join-requests")
    public SuccessResponse<PageResponse<JoinRequestResponse>> getJoinRequests(
            @PathVariable Long groupNo,
            @LoginMember Long memberNo,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return SuccessResponse.ok(
                PageResponse.from(
                        groupMemberService.getJoinRequests(groupNo, memberNo, pageable)
                )
        );
    }

    @Operation(summary = "가입 신청 승인/거절")
    @PatchMapping("/{groupNo}/join-request/{requestNo}")
    public SuccessResponse<Void> processJoinRequest(
            @PathVariable Long groupNo,
            @PathVariable Long requestNo,
            @RequestParam JoinRequestStatus status,
            @LoginMember Long memberNo
    ) {
        groupMemberService.processJoinRequest(groupNo, memberNo, requestNo, status);

        return SuccessResponse.ok();
    }

    @Operation(summary = "그룹원 접속 상태")
    @GetMapping("/{groupNo}/online")
    public SuccessResponse<List<MemberPresenceResponse>> getMemberPresence(
            @LoginMember Long memberNo,
            @PathVariable Long groupNo
    ) {
        return SuccessResponse.ok(
                groupMemberService.getMemberPresence(groupNo, memberNo)
        );
    }

    @Operation(summary = "그룹 프로필 이미지")
    @PostMapping("/{groupNo}/profile-image")
    public SuccessResponse<ProfileImageResponse> groupProfileImageUpload(
            @LoginMember Long memberNo,
            @PathVariable Long groupNo,
            @RequestParam("file")MultipartFile file
    ) {
        String imageUrl = groupService.updateGroupProfileImage(memberNo, groupNo, file);

        return SuccessResponse.ok(new ProfileImageResponse(imageUrl));
    }
}
