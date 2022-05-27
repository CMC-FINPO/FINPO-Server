package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Region;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record RegionDto(
    Long id,
    String region1,
    String region2,
    Boolean isDefault,
    Long userId
    ) {
  public RegionDto {
  }

  public Region toEntity() {
    return Region.of(region1, region2, false);
  }

  public static RegionDto result(Region region) {
    return new RegionDto(region.getId(), region.getRegion1(), region.getRegion2(), region.getIsDefault(), null);
  }
}
