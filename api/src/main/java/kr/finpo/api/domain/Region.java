package kr.finpo.api.domain;

import com.sun.istack.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Region {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @NotBlank(message = "region1 must not be blank")
  @Column(nullable = false)
  private String region1;

  @Setter
  @NotBlank(message = "region2 must not be blank")
  @Column(nullable = false)
  private String region2;

  @Setter
  @Column(nullable = false)
  private Boolean isDefault = false;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected Region() {
  }

  protected Region(String region1, String region2) {
    this.region1 = region1;
    this.region2 = region2;
  }
  protected Region(String region1, String region2, Boolean isDefault) {
    this.region1 = region1;
    this.region2 = region2;
    this.isDefault = isDefault;
  }

  public static Region of() {
    return new Region();
  }
  public static Region of(String region1, String region2) {
    return new Region(region1, region2);
  }
  public static Region of(String region1, String region2, Boolean isDefault) {
    return new Region(region1, region2, isDefault);
  }

  @Setter
  @ManyToOne
  @JoinColumn
  private User user;
}