package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.User;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterestCategoryDto(
    Long id,
    Long categoryId,
    Category category
) {
  public InterestCategoryDto {
  }

  public static InterestCategoryDto of(Long id) {
    return new InterestCategoryDto(null, id, null);
  }
  public static InterestCategoryDto response(InterestCategory interestCategory) {
    return new InterestCategoryDto(interestCategory.getId(), null, interestCategory.getCategory());
  }
}
