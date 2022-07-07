package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.InterestPolicy;
import kr.finpo.api.domain.JoinedPolicy;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record JoinedPolicyDto(
    Long id,
    Long policyId,
    PolicyDto policy,
    String memo
) {
  public JoinedPolicyDto {
  }

  public static JoinedPolicyDto of(Long policyId, String memo) {
    return new JoinedPolicyDto(null, policyId, null, memo);
  }
  public static JoinedPolicyDto response(JoinedPolicy joinedPolicy, Boolean isInterest) {
    return new JoinedPolicyDto(joinedPolicy.getId(), null, PolicyDto.previewResponse(joinedPolicy.getPolicy(), isInterest), joinedPolicy.getMemo());
  }
}
