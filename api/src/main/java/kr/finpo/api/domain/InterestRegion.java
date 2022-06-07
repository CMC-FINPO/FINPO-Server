package kr.finpo.api.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class InterestRegion {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private Boolean isDefault = false;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected InterestRegion() {
  }

  protected InterestRegion(User user, Region region, Boolean isDefault) {
    this.user = user;
    this.region = region;
    this.isDefault = isDefault;
  }

  protected InterestRegion(User user, Region region) {
    this.user = user;
    this.region = region;
  }

  public static InterestRegion of() {
    return new InterestRegion();
  }

  public static InterestRegion of(User user, Region region) {
    return new InterestRegion(user, region);
  }

  public static InterestRegion of(User user, Region region, Boolean isDefault) {
    return new InterestRegion(user, region, isDefault);
  }

  public void updateDefault(Region region) {
    this.region = region;
  }


  @Setter
  @ManyToOne
  @JoinColumn
  private User user;

  @Setter
  @ManyToOne
  @JoinColumn
  private Region region;
}