package kr.finpo.api.domain;

import kr.finpo.api.constant.OpenApiType;
import kr.finpo.api.util.EmptyStringToNullConverter;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.Formula;
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
public class Policy {

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  private Long id;

  @Setter
  @Column(nullable = true)
  private String policyKey;

  @Setter
  @Column(nullable = true)
  private String title;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String content;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(nullable = true)
  private String institution;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String supportScale;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String support;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(nullable = true)
  private String age;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String period;

  @Setter
  @Column(nullable = true)
  private LocalDate startDate;

  @Setter
  @Column(nullable = true)
  private LocalDate endDate;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String process;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String announcement;

  @Setter
  @Convert(converter = EmptyStringToNullConverter.class)
  @Column(columnDefinition = "LONGTEXT", nullable = true)
  private String detailUrl;

  @Setter
  @Column(nullable = true)
  private OpenApiType openApiType;

  @Setter
  @Column(nullable = false)
  private Boolean status = true;

  @Column(nullable = false)
  @ColumnDefault("0")
  private Long hits;

  @Formula("(select count(*) from interest_policy ip where ip.policy_id = id)")
  private Integer countOfInterest;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
  @CreatedDate
  private LocalDateTime createdAt;

  @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
  @LastModifiedDate
  private LocalDateTime modifiedAt;

  protected Policy() {
  }

  protected Policy(String title, String policyKey, String institution, String content, String supportScale, String support, String age, String period, LocalDate startDate, LocalDate endDate, String procedure, String announcement, String detailUrl, OpenApiType openApiType) {
    this.policyKey = policyKey;
    this.title = title;
    this.content = content;
    this.institution = institution;
    this.supportScale = supportScale;
    this.support = support;
    this.age = age;
    this.period = period;
    this.startDate = startDate;
    this.endDate = endDate;
    this.process = procedure;
    this.announcement = announcement;
    this.detailUrl = detailUrl;
    this.openApiType = openApiType;
  }

  public static Policy of() {
    return new Policy();
  }

  public static Policy of(String title, String policyKey, String institution, String content, String supportScale, String support, String age, String period, LocalDate startDate, LocalDate endDate, String procedure, String announcement, String detailUrl, OpenApiType openApiType) {
    return new Policy(title, policyKey, institution, content, supportScale, support, age, period, startDate, endDate, procedure, announcement, detailUrl, openApiType);
  }

  @Setter
  @ManyToOne
  @JoinColumn
  private Category category;

  @Setter
  @ManyToOne
  @JoinColumn
  private Region region;
}