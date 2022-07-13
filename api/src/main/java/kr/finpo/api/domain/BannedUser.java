package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class BannedUser {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private LocalDate releaseDate;

  @Setter
  @Column(length = 200, nullable = true)
  private String detail;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected BannedUser() {
  }

  protected BannedUser(LocalDate releaseDate, String detail, User user, Report report) {
    this.releaseDate = releaseDate;
    this.detail = detail;
    this.user = user;
    this.report = report;
  }

  public static BannedUser of(LocalDate releaseDate, String detail, User user, Report report) {
    return new BannedUser(releaseDate, detail, user, report);
  }

  @Setter
  @OneToOne
  @JoinColumn
  private User user;

  @Setter
  @OneToOne
  @JoinColumn
  private Report report;
}