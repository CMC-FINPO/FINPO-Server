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
public class RefreshToken {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String refreshToken;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected RefreshToken() {
  }

  protected RefreshToken(String refreshToken) {
    this.refreshToken = refreshToken;
  }

  public static RefreshToken of(String refreshToken) {
    return new RefreshToken(refreshToken);
  }

  @Setter
  @OneToOne
  @JoinColumn
  private User user;
}