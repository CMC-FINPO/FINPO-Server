package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.InterestCategory;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterestCategoryDto(
    Long id,
    Long categoryId,
    Category category,
    Boolean subscribe
) {

    public InterestCategoryDto {
    }

    public static InterestCategoryDto of(Long id) {
        return new InterestCategoryDto(null, id, null, null);
    }

    public static InterestCategoryDto response(InterestCategory interestCategory) {
        return new InterestCategoryDto(interestCategory.getId(), null, interestCategory.getCategory(),
            interestCategory.getSubscribe());
    }
}
