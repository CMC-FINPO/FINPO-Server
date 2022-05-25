package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.User;
import kr.finpo.api.exception.GeneralException;

import java.lang.reflect.Field;
import java.time.LocalDate;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserDto(
    Long id,
    String name,
    LocalDate birth,
    Gender gender,
    String email,
    OAuthType oAuthType
) {

  public User toEntity() {
    return User.of(name, birth, gender, email, oAuthType);
  }

  public String toUrlParameter() {
    try {
      StringBuilder sb = new StringBuilder();
      for (Field field : this.getClass().getDeclaredFields()) {
        if(field.get(this) == null) continue;
        sb.append(field.getName());
        sb.append("=");
        sb.append(field.get(this));
        sb.append("&");
      }
      sb.deleteCharAt(sb.length() - 1);
      return sb.toString();
    }
    catch(Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public User updateEntity(User user) {
    if (name != null) user.setName(name);
    if (birth != null) user.setBirth(birth);
    if (email != null) user.setEmail(email);
    return user;
  }

  public static UserDto info(User user) {
    return new UserDto(user.getId(), user.getName(), user.getBirth(), user.getGender(), user.getEmail(), user.getOAuthType());
  }
}
