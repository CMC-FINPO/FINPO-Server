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
  private Boolean status = true;

  @Setter
  @Column(nullable = true)
  private String name;

  @Setter
  @Column(nullable = true, length = 13)
  private String nickname;

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
  private Long statusId;

  @Setter
  @Column(nullable = true)
  private String profileImg;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OAuthType oAuthType;

  @Setter
  @Column(nullable = false)
  private Boolean isDormant = false;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private Role role = Role.ROLE_USER;

  @Setter
  @Column(nullable = true)
  private LocalDate lastRefreshedDate;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected User() {
  }

  protected User(String name, String nickname, LocalDate birth, Gender gender, String email, Long statusId, String profileImg, OAuthType oAuthType) {
    this.name = name;
    this.nickname = nickname;
    this.birth = birth;
    this.gender = gender;
    this.email = email;
    this.statusId = statusId;
    this.profileImg = profileImg;
    this.oAuthType = oAuthType;
  }

  public static User of(String name, String nickname, LocalDate birth, Gender gender, String email, Long statusId, String profileImg, OAuthType oAuthType) {
    return new User(name, nickname, birth, gender, email, statusId, profileImg, oAuthType);
  }

  public void withdraw() {
    status = false;
    name = nickname = email  = profileImg = null;
    birth = null;
    gender = null;
    defaultRegion = null;
  }

  public DormantUser changeToDormant() {
    DormantUser dormantUser = DormantUser.of(this);
    name = nickname = email  = profileImg = null;
    birth = null;
    gender = null;
    isDormant = true;
    statusId = null;
    lastRefreshedDate = null;
    defaultRegion = null;
    return dormantUser;
  }

  public void changeToNormal(DormantUser dormantUser) {
    name = dormantUser.getName();
    nickname = dormantUser.getNickname();
    email  = dormantUser.getEmail();
    profileImg = dormantUser.getProfileImg();
    birth = dormantUser.getBirth();
    gender = dormantUser.getGender();
    statusId = dormantUser.getStatusId();
    defaultRegion = dormantUser.getDefaultRegion();
    isDormant = false;
  }

  @OneToOne(mappedBy = "user")
  private RefreshToken refreshToken;

  @OneToOne(mappedBy = "user")
  private KakaoAccount kakaoAccount;

  @OneToOne(mappedBy = "user")
  private GoogleAccount googleAccount;

  @OneToOne(mappedBy = "user")
  private AppleAccount appleAccount;

  @Setter
  @OneToOne
  @JoinColumn(name = "default_region_id")
  private InterestRegion defaultRegion;
}