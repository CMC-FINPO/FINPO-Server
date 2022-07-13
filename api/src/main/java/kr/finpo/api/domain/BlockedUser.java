package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class BlockedUser {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  protected BlockedUser() {
  }

  public BlockedUser(User user, User blockedUser) {
    this.user = user;
    this.blockedUser = blockedUser;
  }

  public static BlockedUser of() {
    return new BlockedUser();
  }

  public static BlockedUser of(User user, User blockedUser) {
    return new BlockedUser(user, blockedUser);
  }

  @Setter
  @ManyToOne
  @JoinColumn
  private User user;

  @Setter
  @ManyToOne
  @JoinColumn
  private User blockedUser;
}