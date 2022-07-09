package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Fcm;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.InterestRegion;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FcmDto(
    Long id,
    String registrationToken,
    Boolean subscribe,
    List<InterestCategoryDto> interestCategories,
    List<InterestRegionDto> interestRegions
) {

  public FcmDto {
  }

  public static FcmDto response(Fcm fcm, List<InterestCategory> interestCategories, List<InterestRegion> interestRegions) {
    return new FcmDto(null, null, fcm.getSubscribe(), interestCategories.stream().map(InterestCategoryDto::response).toList(), interestRegions.stream().map(InterestRegionDto::response).toList());
  }
}
