package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Objects;

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

  @Setter
  @Column(nullable = true)
  private String img;

  protected Category() {
  }

  public Category(Long id, String name, Long depth) {
    this(id, name, depth, null);
  }

  public Category(Long id, String name, Long depth, String img) {
    this.id = id;
    this.name = name;
    this.depth = depth;
    this.img = img;
  }

  public static Category of() {
    return new Category();
  }

  public static Category of(Long id, String name, Long depth) {
    return new Category(id, name, depth);
  }

  public static Category of(Long id, String name, Long depth, String img) {
    return new Category(id, name, depth, img);
  }

  @Setter
  @JoinColumn(name = "parent_id")
  @ManyToOne(optional = true)
  private Category parent;

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Category that = (Category) o;
    return Objects.equals(getId(), that.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getId());
  }
}