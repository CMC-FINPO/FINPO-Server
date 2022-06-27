package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class InterestCategory {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private Boolean subscribe = true;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected InterestCategory() {
  }

  protected InterestCategory(User user, Category category) {
    this.user = user;
    this.category = category;
  }

  public static InterestCategory of() {
    return new InterestCategory();
  }

  public static InterestCategory of(User user, Category category) {
    return new InterestCategory(user, category);
  }


  @Setter
  @ManyToOne
  @JoinColumn
  private User user;

  @Setter
  @ManyToOne
  @JoinColumn
  private Category category;
}