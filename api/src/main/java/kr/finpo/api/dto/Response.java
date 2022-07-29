package kr.finpo.api.dto;

import kr.finpo.api.constant.ErrorCode;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class Response {

    private final Boolean success;
    private final Integer errorCode;
    private final String message;

    public static Response of(Boolean success, Integer errorCode, String message) {
        return new Response(success, errorCode, message);
    }

    public static Response of(Boolean success, ErrorCode errorCode) {
        return new Response(success, errorCode.getCode(), errorCode.getMessage());
    }

    public static Response of(Boolean success, ErrorCode errorCode, Exception e) {
        return new Response(success, errorCode.getCode(), errorCode.getMessage(e));
    }

    public static Response of(Boolean success, ErrorCode errorCode, String message) {
        return new Response(success, errorCode.getCode(), errorCode.getMessage(message));
    }
}
