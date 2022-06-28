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
public class Fcm {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String registrationToken;

  @Setter
  @Column(nullable = false)
  private Boolean subscribe = false;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected Fcm() {
  }

  protected Fcm(Boolean subscribe, String registrationToken) {
    this.subscribe = subscribe;
    this.registrationToken = registrationToken;
  }

  public static Fcm of(Boolean subscribe, String registrationToken) {
    return new Fcm(subscribe, registrationToken);
  }

  @Setter
  @OneToOne
  @JoinColumn
  private User user;
}