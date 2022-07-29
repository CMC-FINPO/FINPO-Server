package kr.finpo.api.constant;


public enum UserStatus {
    A(1L, "재직중"),
    B(2L, "취업 준비중"),
    C(3L, "창업 준비중"),
    D(4L, "구직중"),
    E(5L, "이직 준비중"),
    F(6L, "재학중"),
    G(7L, "창업중"),
    H(8L, "기타");


    public final Long id;
    public final String name;

    UserStatus(Long id, String name) {
        this.id = id;
        this.name = name;
    }
}
