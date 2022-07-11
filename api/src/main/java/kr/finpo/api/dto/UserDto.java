package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;
import kr.finpo.api.exception.GeneralException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
    Long id,
    String name,
    String nickname,
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    LocalDate birth,
    Gender gender,
    String email,
    Long statusId,
    String profileImg,
    OAuthType oAuthType,
    MultipartFile profileImgFile,
    Long regionId,
    Region defaultRegion,
    String categories,
    List<Long> purposeIds
) {
  public UserDto {
  }

  public User toEntity() {
    return User.of(name, nickname, birth, gender, email, statusId, profileImg, oAuthType);
  }

  public String toUrlParameter() {
    try {
      StringBuilder sb = new StringBuilder();
      for (Field field : this.getClass().getDeclaredFields()) {
        if (field.get(this) == null) continue;
        sb.append(field.getName());
        sb.append("=");
        sb.append(field.get(this));
        sb.append("&");
      }
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString().replaceAll(" ", "%20");
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public User updateEntity(User user) {
    if (name != null) user.setName(name);
    if (nickname != null) user.setNickname(nickname);
    if (birth != null) user.setBirth(birth);
    if (gender != null) user.setGender(gender);
    if (email != null) user.setEmail(email);
    if (statusId != null) user.setStatusId(statusId);
    if (profileImg != null) user.setProfileImg(profileImg);

    return user;
  }

  public static UserDto response(User user) {
    return new UserDto(user.getId(), user.getName(), user.getNickname(), user.getBirth(), user.getGender(), user.getEmail(), user.getStatusId(), user.getProfileImg(), user.getOAuthType(), null, null, user.getDefaultRegion().getRegion(), null, null);
  }

  public static UserDto communityResponse(User user) {
    return new UserDto(user.getId(), null, user.getNickname(), null, user.getGender(), null, null, user.getProfileImg(), null, null, null, null, null, null);
  }

  public static UserDto appleUserDto() {
    return new UserDto(null,null,null,null,null,null,null,null,OAuthType.APPLE,null,null,null,null,null);
  }
}
