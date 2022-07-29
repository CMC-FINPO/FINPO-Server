package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.finpo.api.domain.BannedUser;
import kr.finpo.api.domain.Report;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BannedUserDto(
    Long id,
    LocalDate releaseDate,
    String detail,
    Report report,
    UserDto user,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt
) {

    public BannedUserDto {
    }

    public BannedUser updateEntity(BannedUser bannedUser) {
        Optional.ofNullable(releaseDate).ifPresent(bannedUser::setReleaseDate);
        Optional.ofNullable(detail).ifPresent(bannedUser::setDetail);
        return bannedUser;
    }

    public static BannedUserDto response(BannedUser bannedUser) {
        return new BannedUserDto(bannedUser.getId(), bannedUser.getReleaseDate(), bannedUser.getDetail(),
            bannedUser.getReport(), UserDto.communityResponse(bannedUser.getUser()), bannedUser.getCreatedAt(),
            bannedUser.getModifiedAt());
    }
}
