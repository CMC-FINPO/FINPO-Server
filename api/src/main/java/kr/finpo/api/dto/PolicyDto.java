package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import kr.finpo.api.constant.OpenApiType;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.Region;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PolicyDto(
    Long id,
    Boolean status,
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
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    Category category,
    Region region,
    Integer countOfInterest,
    Long hits,
    Boolean isInterest
) {

    public Policy updateEntity(Policy policy) {
        Optional.ofNullable(status).ifPresent(policy::setStatus);
        Optional.ofNullable(title).ifPresent(policy::setTitle);
        Optional.ofNullable(content).ifPresent(policy::setContent);
        Optional.ofNullable(institution).ifPresent(policy::setInstitution);
        Optional.ofNullable(supportScale).ifPresent(policy::setSupportScale);
        Optional.ofNullable(support).ifPresent(policy::setSupport);
        Optional.ofNullable(period).ifPresent(policy::setPeriod);
        Optional.ofNullable(startDate).ifPresent(policy::setStartDate);
        Optional.ofNullable(endDate).ifPresent(policy::setEndDate);
        Optional.ofNullable(process).ifPresent(policy::setProcess);
        Optional.ofNullable(announcement).ifPresent(policy::setAnnouncement);
        Optional.ofNullable(detailUrl).ifPresent(policy::setDetailUrl);

        return policy;
    }

    public static PolicyDto response(Policy policy, Boolean isInterest) {
        return new PolicyDto(
            policy.getId(),
            policy.getStatus(),
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
            policy.getCreatedAt(),
            policy.getModifiedAt(),
            policy.getCategory(),
            policy.getRegion(),
            policy.getCountOfInterest(),
            policy.getHits(),
            isInterest
        );
    }

    public static PolicyDto previewResponse(Policy policy, Boolean isInterest) {
        return new PolicyDto(
            policy.getId(),
            policy.getStatus(),
            policy.getTitle(),
            null,
            policy.getInstitution(),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            policy.getCreatedAt(),
            null,
            null,
            policy.getRegion(),
            policy.getCountOfInterest(),
            null,
            isInterest
        );
    }
}
