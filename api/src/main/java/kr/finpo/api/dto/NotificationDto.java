package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Fcm;
import kr.finpo.api.domain.InterestCategory;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationDto(
    Long id,
    String registrationToken,
    Boolean subscribe,
    List<InterestCategoryDto> interestCategories
) {

  public NotificationDto {
  }

  public static NotificationDto response(Fcm fcm, List<InterestCategory> interestCategories) {
    return new NotificationDto(null, null, fcm.getSubscribe(), interestCategories.stream().map(InterestCategoryDto::response).toList());
  }
}
