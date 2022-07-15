package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.BlockedUser;

import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BlockedUserDto(
    Long id,
    UserDto blockedUser,
    LocalDateTime createdAt
) {
  public static BlockedUserDto response(BlockedUser blockedUser) {
    return new BlockedUserDto(blockedUser.getId(), UserDto.communityResponse(blockedUser.getBlockedUser()), blockedUser.getCreatedAt());
  }
}
