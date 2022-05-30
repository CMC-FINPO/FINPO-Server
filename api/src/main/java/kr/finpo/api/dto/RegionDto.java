package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.RegionConstant;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;

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

  public Region updateEntity(Region region) {
    if (!region1.isEmpty() && !region2.isEmpty())
      region.setRegionKey(Region.getKey(region1, region2));

    return region;
  }

  public static RegionDto result(Region region) {
    Long regionKey = RegionConstant.getKey(region.getRegion1(), region.getRegion2());
    return new RegionDto(region.getId(), RegionConstant.getRegion1(regionKey), RegionConstant.getRegion2(regionKey), region.getIsDefault(), null);
  }
}
