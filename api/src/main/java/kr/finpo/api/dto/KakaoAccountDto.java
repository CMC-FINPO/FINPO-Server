package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record KakaoAccountDto(
    String id,
    KakaoAccount kakao_account
) {
  record KakaoAccount(
      Profile profile,
      String email,
      String gender
  ) {
    record Profile(String nickname) {
    }
  }

  public UserDto toUserDto() {
    return new UserDto(null,
        kakao_account.profile.nickname,
        kakao_account.profile.nickname,
        null,
        "male".equals(kakao_account.gender) ? Gender.MALE : "female".equals(kakao_account.gender) ? Gender.FEMALE : null,
        kakao_account.email,
        null,
        null,
        null,
        OAuthType.KAKAO,
        null
    );
  }
}

