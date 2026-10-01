package com.studio.core.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;


@AllArgsConstructor
@Getter
public enum ErrorCode {

    // SERVER ERROR
    SERVER_UNTRACKED_ERROR(-100, "미등록 서버 에러입니다.", 500),
    NOT_FOUND_PATH(-101, "존재하지 않는 경로입니다.", 404),
    DATA_INTEGRITY_ERROR(-110, "데이터 제약 조건을 위반했습니다.", 500),
    DATA_CONFLICT(-111, "이미 존재하는 값입니다.", 409),
    INVALID_PARAMETER(-103, "잘못된 파라미터입니다. : ", 422),
    PARAMETER_VALIDATION_ERROR(-104, "파라미터 검증 에러입니다.", 422),
    PARAMETER_GRAMMAR_ERROR(-105, "파라미터 문법 에러입니다.", 422),
    INVALID_TYPE_PARAMETER(-107, "잘못된 타입 파라미터입니다. : ", 422),
    NO_REQUIRED_VALUE(-108, "필수값 없음 : ", 400),

    // 인증
    UNAUTHORIZED(-200, "인증 자격이 없습니다.", 401),
    FORBIDDEN(-201, "권한이 없습니다.", 403),
    JWT_ERROR_TOKEN(-202, "잘못된 토큰입니다.", 401),
    JWT_EXPIRE_TOKEN(-203, "만료된 토큰입니다.", 401),
    PASSWORD_NOT_MATCH(-211, "현재 비밀번호가 일치하지 않습니다.", 400),
    LOGIN_FAIL( -212, "이메일 또는 비밀번호가 올바르지 않습니다.", 401),
    MEMBER_EMAIL_DUPLICATE( -300,"이미 사용 중인 이메일입니다.",409),
    MEMBER_NICKNAME_DUPLICATE(-301, "이미 사용 중인 닉네임입니다.", 409),
    INVALID_PHONE_NUMBER(-305, "사용가능한 핸드폰 번호를 입력해주세요.", 422),
    INVALID_NAME(-307, "사용할 수 없는 이름입니다.", 422),
    INVALID_NICKNAME( -302, "사용할 수 없는 닉네임입니다.",422),
    INVALID_PASSWORD(-303, "비밀번호 형식이 올바르지 않습니다.", 422),
    MEMBER_NOT_FOUND(-304,"회원을 찾을 수 없습니다.", 404),
    INVALID_EMAIL(-306, "올바른 이메일 형식이 아닙니다.", 422),
    SOCIAL_MEMBER_NO_PASSWORD(-308, "소셜 로그인 계정은 비밀번호를 사용하지 않습니다.", 409),

    // GROUP
    GROUP_NAME_DUPLICATED(-400, "이미 존재하는 그룹명입니다.", 409),
    ALREADY_IN_GROUP(-401, "이미 가입한 그룹입니다.", 409),
    GROUP_NOT_FOUND(-402, "해당 그룹을 찾을 수 없습니다.", 404),
    UNAUTHORIZED_GROUP_ACCESS(-403, "그룹을 관리할 권한이 없습니다.", 403),
    CANNOT_DELETE_OWNER(-405, "그룹장을 삭제할 수 없습니다.", 409),
    GROUP_OWNER_CANNOT_LEAVE(-406, "그룹장은 탈퇴할 수 없습니다.", 409),
    GROUP_MEMBER_NOT_FOUND(-404,  "그룹원을 찾을 수 없습니다.",404),
    GROUP_JOIN_NOT_ALLOWED(-407, "이 그룹은 해당 방식으로 가입할 수 없습니다.", 403),
    INVALID_GROUP_DESCRIPTION(-408, "그룹 소개는 200자까지 입력 가능합니다.", 422),
    JOIN_REQUEST_NOT_FOUND(-409, "가입 신청을 찾을 수 없습니다.", 404),
    JOIN_REQUEST_ALREADY_PENDING(-410, "이미 신청한 그룹입니다.", 409),
    JOIN_REQUEST_ALREADY_PROCESSED(-411, "이미 처리된 신청입니다.", 409),
    INVALID_GROUP_NAME(-412, "그룹 이름은 1~20자여야 합니다.", 422),
    INVALID_JOIN_MESSAGE(-413, "가입 메시지는 200자까지 입력 가능합니다.", 422),
    NOT_GROUP_OWNER(-414, "그룹장만 넘길 수 있어요", 403),
    CANNOT_TRANSFER_TO_SELF(-415, "자기 자신에게는 넘길 수 없어요.", 400),
    // SCGEDULE
    SCHEDULE_NOT_FOUND(-500,  "일정을 찾을 수 없습니다.",404),
    SCHEDULE_ACCESS_DENIED(-501, "일정에 접근할 권한이 없습니다.", 403),
    INVALID_SCHEDULE_PERIOD(-502, "종료 시간이 시작 시간보다 빠를 수 없습니다.", 422),

    // EMAUL 인증
    EMAIL_VERIFICATION_NOT_FOUND(-310, "인증 요청을 먼저 해주세요.", 404),
    EMAIL_VERIFICATION_EXPIRED(-311, "인증 코드가 만료됐어요. 다시 요청해 주세요.", 410),
    EMAIL_VERIFICATION_MISMATCH(-312, "인증 코드가 올바르지 않아요.", 400),
    EMAIL_NOT_VERIFIED(-313, "이메일 인증이 필요해요.", 403),
    EMAIL_VERIFICATION_TOO_OFTEN(-314, "잠시 후 다시 요청해 주세요.", 429),
    EMAIL_VERIFICATION_TOO_MANY_FAILS(-315, "인증 시도를 너무 많이 했어요. 코드를 다시 받아 주세요.", 429),

    // NAVER FILE
    FILE_CREATE_ERROR(-600, "파일 처리 중 오류가 발생했습니다.", 500),
    NAVER_REST_SEND_ERROR(-601, "네이버 rest api로 요청 발송 중 에러가 발생했습니다.", 502),
    INVALID_IMAGE_FILE(-602, "이미지 파일이 올바르지 않습니다.", 422),
    IMAGE_UPLOAD_FAILED(-603, "이미지 업로드에 실패했습니다.", 502),
    EXTERNAL_API_BAD_RESPONSE(-604, "외부 API 응답 형식이 올바르지 않습니다.", 502),

    // NOTIFICATION
    NOTIFICATION_NOT_FOUND(-700, "알림을 찾을 수 없습니다.", 404);

    private final int errorCode;
    private final String message;
    private final int httpCode;


}
