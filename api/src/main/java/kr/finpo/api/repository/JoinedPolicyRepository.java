package kr.finpo.api.repository;
import kr.finpo.api.domain.InterestPolicy;
import kr.finpo.api.domain.JoinedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinedPolicyRepository extends JpaRepository<JoinedPolicy, Long> {
  public List<JoinedPolicy> findByUserId(Long id);
  public Optional<JoinedPolicy> findOneByUserIdAndPolicyId(Long userId, Long policyId);
  public Long countByPolicyId(Long id);
  public Long deleteByUserId(Long id);
}

