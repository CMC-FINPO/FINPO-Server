package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.InterestPolicy;
import kr.finpo.api.domain.Policy;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record InterestPolicyDto(
    Long id,
    Long policyId,
    PolicyDto policy
) {
  public InterestPolicyDto {
  }

  public static InterestPolicyDto of(Long id) {
    return new InterestPolicyDto(null, id, null);
  }
  public static InterestPolicyDto response(InterestPolicy interestPolicy) {
    return new InterestPolicyDto(interestPolicy.getId(), null, PolicyDto.previewResponse(interestPolicy.getPolicy()));
  }
}
