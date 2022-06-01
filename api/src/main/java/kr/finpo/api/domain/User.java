package kr.finpo.api.domain;

import com.sun.istack.NotNull;
import kr.finpo.api.constant.*;
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
public class User{

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String name;

  @Setter
  @Column(nullable = false)
  private String nickname;

  @Setter
  @Column(nullable = false)
  private LocalDate birth;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Gender gender;

  @Setter
  @Column(nullable = false)
  private String email;

  @Setter
  @Column(nullable = true)
  private String status;

  @Setter
  @Column(nullable = true)
  private String profileImg;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OAuthType oAuthType;

  @Setter
  @Column(nullable = false)
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

  protected User(String name, String nickname, LocalDate birth, Gender gender, String email, String status, String profileImg, OAuthType oAuthType) {
    this.name = name;
    this.nickname = nickname;
    this.birth = birth;
    this.gender = gender;
    this.email = email;
    this.status = status;
    this.profileImg = profileImg;
    this.oAuthType = oAuthType;
  }

  public static User of(String name, String nickname, LocalDate birth, Gender gender, String email, String status, String profileImg, OAuthType oAuthType) {
    return new User(name, nickname, birth, gender, email, status, profileImg, oAuthType);
  }


  @OneToOne(mappedBy = "user")
  private RefreshToken refreshToken;

  @OneToOne(mappedBy = "user")
  private KakaoAccount kakaoAccount;

  @OneToOne(mappedBy = "user")
  private GoogleAccount googleAccount;

  @Setter
  @OneToOne
  @NotNull
  @JoinColumn(name = "default_region_id")
  private InterestRegion defaultRegion;
}