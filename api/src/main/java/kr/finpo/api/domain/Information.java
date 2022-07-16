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
public class Information {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String type;

  @Setter
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String content;

  @Setter
  @Column(nullable = true)
  private String url;

  @Setter
  @Column(nullable = true)
  private Boolean status;

  @Setter
  @Column(nullable = false)
  private Boolean hidden = false;

  protected Information() {
  }

  public Information(String type, String content, String url, Boolean status) {
    this.type = type;
    this.content = content;
    this.url = url;
    this.status = status;
  }

  public static Information of() {
    return new Information();
  }

  public static Information of(String type, String content, String url, Boolean status) {
    return new Information(type, content, url, status);
  }
}