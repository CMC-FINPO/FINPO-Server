package kr.finpo.api.constant;


public enum UserPurpose {
  A(1L, "정보 이용"),
  B(2L, "취업 역량 강화"),
  C(3L, "창업지원"),
  D(4L, "구직 활동"),
  E(5L, "생활 지원"),
  F(6L, "문화/예술 활동"),
  G(7L, "교육 이수"),
  H(8L, "상담 활동"),
  I(9L, "기타");


  public final Long id;
  public final String name;

  UserPurpose(Long id, String name) {
    this.id = id;
    this.name = name;
  }
}
