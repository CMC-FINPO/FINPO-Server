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
public class JoinedPolicy {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(length = 100)
  private String memo;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  protected JoinedPolicy() {
  }

  protected JoinedPolicy(User user, Policy policy, String memo) {
    this.user = user;
    this.policy = policy;
    this.memo = memo;
  }

  public static JoinedPolicy of() {
    return new JoinedPolicy();
  }

  public static JoinedPolicy of(User user, Policy policy, String memo) {
    return new JoinedPolicy(user, policy, memo);
  }


  @Setter
  @ManyToOne
  @JoinColumn
  private User user;

  @Setter
  @ManyToOne
  @JoinColumn
  private Policy policy;
}