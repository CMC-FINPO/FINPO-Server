package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.InterestRegion;
import kr.finpo.api.domain.Region;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterestRegionDto(
    Long id,
    Long regionId,
    Region region,
    Boolean isDefault,
    Boolean subscribe
) {
  public InterestRegionDto {
  }

  public static InterestRegionDto of(Long regionId, Boolean isDefault) {
    return new InterestRegionDto(null, regionId, null, isDefault, null);
  }

  public static InterestRegionDto response(InterestRegion interestRegion) {
    return new InterestRegionDto(interestRegion.getId(), null, interestRegion.getRegion(), interestRegion.getIsDefault(), interestRegion.getSubscribe());
  }
}
