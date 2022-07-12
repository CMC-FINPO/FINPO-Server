package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Report {

  @Id
  @Setter
  @Column(nullable = false)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String reason;

  protected Report() {
  }

  public Report(Long id, String reason) {
    this.id = id;
    this.reason = reason;
  }

  public static Report of() {
    return new Report();
  }

  public static Report of(Long id, String name) {
    return new Report(id, name);
  }
}