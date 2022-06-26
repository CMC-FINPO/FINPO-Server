package kr.finpo.api.repository;

import kr.finpo.api.domain.InterestPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InterestPolicyRepository extends JpaRepository<InterestPolicy, Long> {
  public List<InterestPolicy> findByUserId(Long id);
  public Optional<InterestPolicy> findOneByUserIdAndPolicyId(Long userId, Long policyId);
  public Long countByPolicyId(Long id);
  public Long deleteByUserId(Long id);
}

