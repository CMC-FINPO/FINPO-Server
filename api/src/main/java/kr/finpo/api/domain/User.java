package kr.finpo.api.domain;

import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.constant.Role;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = true)
  private String name;

  @Setter
  @Column(nullable = true)
  private LocalDate birth;

  @Setter
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Setter
  @Column(nullable = true)
  private String email;

  @Setter
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private OAuthType oAuthType;

  @Setter
  @Column(nullable = true)
  @Enumerated(EnumType.STRING)
  private Role role = Role.ROLE_USER;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected User() {
  }

  protected User(String name) {
    this.name = name;
  }

  protected User(String name, LocalDate birth, Gender gender, String email, OAuthType oAuthType) {
    this.name = name;
    this.birth = birth;
    this.gender = gender;
    this.email = email;
    this.oAuthType = oAuthType;
  }

  public static User of(String name) {
    return new User(name);
  }

  public static User of(String name, LocalDate birth, Gender gender, String email, OAuthType oauthType) {
    return new User(name, birth, gender, email, oauthType);
  }

  @Setter
  @OneToOne(cascade = CascadeType.PERSIST)
  @JoinColumn
  private RefreshToken refreshToken;


  @OneToOne
  @Setter
  @JoinColumn
  private KakaoAccount kakaoAccount;
}