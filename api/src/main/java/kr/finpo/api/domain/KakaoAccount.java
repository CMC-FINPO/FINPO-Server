package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
public class KakaoAccount {

  @Id
  @Setter
  private String id;


  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected KakaoAccount() {
  }

  protected KakaoAccount(String kakaoId) {
    this.id = kakaoId;
  }

  public static KakaoAccount of(String kakaoId) {
    return new KakaoAccount(kakaoId);
  }

  @OneToOne(mappedBy = "kakaoAccount")
  private User user;
}