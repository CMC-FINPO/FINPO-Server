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
public class BookmarkPost {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  protected BookmarkPost() {
  }

  protected BookmarkPost(User user, Post post) {
    this.user = user;
    this.post = post;
  }

  public static BookmarkPost of() {
    return new BookmarkPost();
  }

  public static BookmarkPost of(User user, Post post) {
    return new BookmarkPost(user, post);
  }


  @Setter
  @ManyToOne
  @JoinColumn
  private User user;

  @Setter
  @ManyToOne
  @JoinColumn
  private Post post;
}