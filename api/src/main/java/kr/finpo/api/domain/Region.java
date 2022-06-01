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
public class Region {

  @Id
  @Setter
  private Long id;

  @Setter
  @Column(nullable = false)
  private String name;

  @Setter
  @Column(nullable = false)
  private Long depth;

  protected Region() {
  }

  public Region(Long id, String name, Long depth) {
    this.id = id;
    this.name = name;
    this.depth = depth;
  }

  public static Region of() {
    return new Region();
  }

  public static Region of(Long id, String name, Long depth) {
    return new Region(id, name, depth);
  }

  @Setter
  @JoinColumn(name = "parent_id")
  @OneToOne(optional = true)
  private Region parent;
}