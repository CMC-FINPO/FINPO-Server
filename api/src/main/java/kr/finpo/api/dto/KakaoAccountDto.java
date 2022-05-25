package kr.finpo.api.dto;

public record KakaoAccountDto(
    String id,
    KakaoAccount kakao_account,
    String name,
    String email,
    String gender
) {
  public KakaoAccountDto {
    name = kakao_account.profile().nickname();
    email = kakao_account.email();
    gender = kakao_account.gender();
  }

  record KakaoAccount(
      Profile profile,
      String email,
      String gender
  ) {
    record Profile(String nickname) {
    }
  }
}

