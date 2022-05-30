package kr.finpo.api.domain;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.RegionConstant;
import kr.finpo.api.exception.GeneralException;
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
public class Region extends RegionConstant{

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = false)
  private Long regionKey;

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
    this.regionKey = (this.getKey(region1, region2));
  }
  protected Region(String region1, String region2, Boolean isDefault) {
    this(region1, region2);
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

  public void update(String region1, String region2) {
    this.regionKey = (this.getKey(region1, region2));
  }

  public String getRegion1() {
    return this.getRegion1(regionKey);
  }

  public String getRegion2() {
    return this.getRegion2(regionKey);
  }


  @Setter
  @ManyToOne
  @JoinColumn
  private User user;
}