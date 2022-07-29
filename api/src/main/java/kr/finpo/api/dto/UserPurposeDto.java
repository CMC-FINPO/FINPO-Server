package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.UserPurpose;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserPurposeDto(
    Long id,
    String name
) {

    public UserPurposeDto {
    }

    public static UserPurposeDto response(UserPurpose userPurpose) {
        return new UserPurposeDto(userPurpose.id, userPurpose.name);
    }
}
