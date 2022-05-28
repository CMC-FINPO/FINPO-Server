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
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class GoogleAccount {

  @Id
  @Setter
  private String id;


  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected GoogleAccount() {
  }

  protected GoogleAccount(String googleId) {
    this.id = googleId;
  }

  public static GoogleAccount of(String googleId) {
    return new GoogleAccount(googleId);
  }

  @Setter
  @OneToOne
  @JoinColumn
  private User user;
}