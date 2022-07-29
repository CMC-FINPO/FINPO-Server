package kr.finpo.api.constant;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Predicate;
import kr.finpo.api.exception.GeneralException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    OK(0, HttpStatus.OK, "Ok"),

    BAD_REQUEST(10000, HttpStatus.BAD_REQUEST, "Bad request"),
    SPRING_BAD_REQUEST(10001, HttpStatus.BAD_REQUEST, "Spring-detected bad request"),
    VALIDATION_ERROR(10002, HttpStatus.BAD_REQUEST, "Validation error"),
    NOT_FOUND(10003, HttpStatus.NOT_FOUND, "Requested resource is not found"),
    USER_ALREADY_REGISTERED(10004, HttpStatus.BAD_REQUEST, "You're already registered"),
    USER_NOT_EQUAL(10005, HttpStatus.UNAUTHORIZED, "You're not the owner"),
    USER_UNAUTHORIZED(10006, HttpStatus.UNAUTHORIZED, "User not found"),


    INTERNAL_ERROR(20000, HttpStatus.INTERNAL_SERVER_ERROR, "Internal error"),
    SPRING_INTERNAL_ERROR(20001, HttpStatus.INTERNAL_SERVER_ERROR, "Spring-detected internal error"),
    DATA_ACCESS_ERROR(20002, HttpStatus.INTERNAL_SERVER_ERROR, "Data access error"),
    IMAGE_UPLOAD_ERROR(20003, HttpStatus.INTERNAL_SERVER_ERROR, "Image upload error"),


    PROVIDER_TOKEN_NOT_FOUND(30002, HttpStatus.BAD_REQUEST, "Auth provider token not found"),
    KAKAO_SERVER_ERROR(30001, HttpStatus.INTERNAL_SERVER_ERROR, "Kakao authorization server may have problem"),
    GOOGLE_ACCESS_TOKEN_ERROR(30003, HttpStatus.INTERNAL_SERVER_ERROR, "Invalid google access token"),
    APPlE_IDENTITY_TOKEN_ERROR(30004, HttpStatus.INTERNAL_SERVER_ERROR, "Invalid apple identity token"),


    ACCESS_TOKEN_NOT_FOUND(40000, HttpStatus.UNAUTHORIZED, "Access token not found"),
    ACCESS_TOKEN_EXPIRATION(40001, HttpStatus.UNAUTHORIZED, "Expired access token. Send me refresh token"),
    INVALID_REFRESH_TOKEN(40002, HttpStatus.BAD_REQUEST, "Your refresh token isn't valid"),
    REFRESH_TOKEN_NOT_FOUND(40003, HttpStatus.UNAUTHORIZED, "No refresh token exist. It seemed you've already logout");


    private final Integer code;
    private final HttpStatus httpStatus;
    private final String message;


    public static ErrorCode valueOf(HttpStatus httpStatus) {
        if (httpStatus == null) {
            throw new GeneralException("HttpStatus is null.");
        }

        return Arrays.stream(values())
            .filter(errorCode -> errorCode.getHttpStatus() == httpStatus)
            .findFirst()
            .orElseGet(() -> {
                if (httpStatus.is4xxClientError()) {
                    return ErrorCode.BAD_REQUEST;
                } else if (httpStatus.is5xxServerError()) {
                    return ErrorCode.INTERNAL_ERROR;
                } else {
                    return ErrorCode.OK;
                }
            });
    }

    public String getMessage(Throwable e) {
        return this.getMessage(this.getMessage() + " - " + e.getMessage());
    }

    public String getMessage(String message) {
        return Optional.ofNullable(message)
            .filter(Predicate.not(String::isBlank))
            .orElse(this.getMessage());
    }

    @Override
    public String toString() {
        return String.format("%s (%d)", this.name(), this.getCode());
    }
}
