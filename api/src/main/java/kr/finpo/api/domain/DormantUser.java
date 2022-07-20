package kr.finpo.api.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
public class DormantUser {

  @Id
  @Setter
  private Long id;

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
  @Column(nullable = true)
  private LocalDate lastRefreshedDate;

  @Setter
  @Column(nullable = false)
  @Enumerated(EnumType.STRING)
  private OAuthType oAuthType;

  @Setter
  @OneToOne
  @JoinColumn(name = "default_region_id")
  private InterestRegion defaultRegion;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  protected DormantUser() {
  }

  protected DormantUser(Long userId, String name, String nickname, LocalDate birth, Gender gender, String email, Long statusId, String profileImg, LocalDate lastRefreshedDate, OAuthType oAuthType, InterestRegion defaultRegion) {
    this.setId(userId);
    this.setName(name);
    this.setNickname(nickname);
    this.setBirth(birth);
    this.setGender(gender);
    this.setEmail(email);
    this.setStatusId(statusId);
    this.setProfileImg(profileImg);
    this.setLastRefreshedDate(lastRefreshedDate);
    this.setOAuthType(oAuthType);
    this.setDefaultRegion(defaultRegion);
  }

  public static DormantUser of(User user) {
    return new DormantUser(user.getId(), user.getName(), user.getNickname(), user.getBirth(), user.getGender(), user.getEmail(), user.getStatusId(), user.getProfileImg(), user.getLastRefreshedDate(), user.getOAuthType(), user.getDefaultRegion());
  }
}