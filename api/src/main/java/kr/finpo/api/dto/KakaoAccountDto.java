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
    record Profile(String nickname, String profile_image_url) {
    }
  }

  public UserDto toUserDto() {
    String nickname = null, profileUrl = null;
    try {
      nickname = kakao_account.profile.nickname;
    } catch (NullPointerException ignored) {
    }
    try {
      profileUrl = kakao_account.profile.profile_image_url;
    } catch (NullPointerException ignored) {
    }

    return new UserDto(null,
        null,
        nickname,
        null,
        "male".equals(kakao_account.gender) ? Gender.MALE : "female".equals(kakao_account.gender) ? Gender.FEMALE : null,
        kakao_account.email,
        null,
        profileUrl,
        OAuthType.KAKAO,
        null,
        null,
        null,
        null,
        null
    );
  }
}

