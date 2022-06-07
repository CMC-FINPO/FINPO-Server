package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.InterestRegion;
import kr.finpo.api.domain.Region;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterestRegionDto(
    Long id,
    Long regionId,
    Region region,
    Boolean isDefault
) {
  public InterestRegionDto {
  }

  public static InterestRegionDto response(InterestRegion interestRegion) {
    return new InterestRegionDto(interestRegion.getId(), null, interestRegion.getRegion(), interestRegion.getIsDefault());
  }
}