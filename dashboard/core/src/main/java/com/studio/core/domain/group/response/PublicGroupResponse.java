package com.studio.core.domain.group.response;

import com.studio.core.global.enums.GroupVisibility;
import lombok.Getter;
import lombok.Setter;

@Getter
public class PublicGroupResponse {
    private final Long groupNo;
    private final String groupName;
    private final String description;
    private final GroupVisibility visibility;
    private final Long memberCount;
    private String profileImageUrl;

    @Setter
    private boolean joined;

    @Setter
    private boolean pending;

    public PublicGroupResponse (
            Long groupNo,
            String groupName,
            String description,
            GroupVisibility visibility,
            Long memberCount,
            String profileImageUrl
    ) {
        this.groupNo = groupNo;
        this.groupName = groupName;
        this.description = description;
        this.visibility = visibility;
        this.memberCount = memberCount;
        this.profileImageUrl = profileImageUrl;
    }
}
