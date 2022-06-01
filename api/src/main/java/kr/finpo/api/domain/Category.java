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
public class Category {

  @Id
  @Setter
  @Column(nullable = false)
  private Long id;

  @Setter
  @Column(nullable = false)
  private String name;

  @Setter
  @Column(nullable = false)
  private Long depth;

  protected Category() {
  }

  public Category(Long id, String name, Long depth) {
    this.id = id;
    this.name = name;
    this.depth = depth;
  }

  public static Category of() {
    return new Category();
  }

  public static Category of(Long id, String name, Long depth) {
    return new Category(id, name, depth);
  }

  @Setter
  @OneToOne
  @JoinColumn(name = "parent_id")
  private Category parent;
}