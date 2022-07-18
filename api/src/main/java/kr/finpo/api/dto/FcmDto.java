package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Comment;
import kr.finpo.api.domain.Fcm;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.InterestRegion;

import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record FcmDto(
    Long id,
    String registrationToken,
    Boolean subscribe,
    Boolean communitySubscribe,
    Boolean adSubscribe,
    List<InterestCategoryDto> interestCategories,
    List<InterestRegionDto> interestRegions
) {

  public FcmDto {
  }

  public Fcm updateEntity(Fcm fcm) {
    Optional.ofNullable(registrationToken).ifPresent(fcm::setRegistrationToken);
    Optional.ofNullable(subscribe).ifPresent(fcm::setSubscribe);
    Optional.ofNullable(communitySubscribe).ifPresent(fcm::setCommunitySubscribe);
    Optional.ofNullable(adSubscribe).ifPresent(fcm::setAdSubscribe);
    return fcm;
  }

  public static FcmDto response(Fcm fcm, List<InterestCategory> interestCategories, List<InterestRegion> interestRegions) {
    if(isEmpty(fcm)) return new FcmDto(null, null, false, null, null, null, null);
    return new FcmDto(null, null, fcm.getSubscribe(), fcm.getCommunitySubscribe(), fcm.getAdSubscribe(), interestCategories.stream().map(InterestCategoryDto::response).toList(), interestRegions.stream().map(InterestRegionDto::response).toList());
  }
}
