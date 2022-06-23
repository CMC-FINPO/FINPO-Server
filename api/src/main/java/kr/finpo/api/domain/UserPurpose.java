package kr.finpo.api.domain;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
public class UserPurpose {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private Long userPurposeId;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  protected UserPurpose() {
  }

  protected UserPurpose(Long userPurposeId) {
    this.userPurposeId = userPurposeId;
  }

  public static UserPurpose of(Long userPurposeId) {
    return new UserPurpose(userPurposeId);
  }


  @Setter
  @ManyToOne
  @NotNull
  @JoinColumn
  private User user;
}