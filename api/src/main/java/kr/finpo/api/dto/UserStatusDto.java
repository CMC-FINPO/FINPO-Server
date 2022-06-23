package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.UserStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserStatusDto(
    Long id,
    String name
) {
  public UserStatusDto {
  }

  public static UserStatusDto response(UserStatus userStatus) {
    return new UserStatusDto(userStatus.id, userStatus.name);
  }
}
