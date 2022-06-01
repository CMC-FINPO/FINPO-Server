package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.OpenApiType;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.Region;

import java.time.LocalDate;
import java.time.LocalDateTime;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PolicyDto(
    Long id,
    String title,
    String content,
    String institution,
    String supportScale,
    String support,
    String period,
    LocalDate startDate,
    LocalDate endDate,
    String process,
    String announcement,
    String detailUrl,
    OpenApiType openApiType,
    LocalDateTime modifiedAt,
    Category category,
    Region region
) {
  public PolicyDto {
  }


  public static PolicyDto response(Policy policy) {
    return new PolicyDto(
        policy.getId(),
        policy.getTitle(),
        policy.getContent(),
        policy.getInstitution(),
        policy.getSupportScale(),
        policy.getSupport(),
        policy.getPeriod(),
        policy.getStartDate(),
        policy.getEndDate(),
        policy.getProcess(),
        policy.getAnnouncement(),
        policy.getDetailUrl(),
        policy.getOpenApiType(),
        policy.getModifiedAt(),
        policy.getCategory(),
        policy.getRegion()
    );
  }
}
