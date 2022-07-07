package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.Formula;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.annotation.Nullable;
import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Post {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(length = 1000, nullable = false)
  private String content;

  @Setter
  @Column(nullable = false)
  private Boolean anonymity;

  @Setter
  @Column(nullable = false)
  private Boolean status = true;

  @Column(nullable = false)
  private Long hits = 0L;

  @Formula("(select count(*) from like_post lp where lp.post_id = id)")
  private Integer likes = 0;

  @Formula("(select count(*) from comment c where c.post_id = id)")
  private Integer countOfComment = 0;

  @Setter
  @Column(nullable = false)
  private Boolean isModified = false;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected Post() {
  }

  protected Post(String content, Boolean anonymity) {
    this.content = content;
    this.anonymity = anonymity;
  }

  public static Post of() {
    return new Post();
  }

  public static Post of(String content, Boolean anonymity) {
    return new Post(content, anonymity);
  }

  @Setter
  @ManyToOne
  @Nullable
  @JoinColumn
  private User user;
}