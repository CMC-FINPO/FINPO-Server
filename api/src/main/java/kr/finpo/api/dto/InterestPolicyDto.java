package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.InterestPolicy;

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

    public static InterestPolicyDto response(InterestPolicy interestPolicy, Boolean isInterest) {
        return new InterestPolicyDto(interestPolicy.getId(), null,
            PolicyDto.previewResponse(interestPolicy.getPolicy(), isInterest));
    }
}
