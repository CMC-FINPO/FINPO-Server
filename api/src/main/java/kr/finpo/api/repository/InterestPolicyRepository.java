package kr.finpo.api.repository;

import kr.finpo.api.domain.InterestPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InterestPolicyRepository extends JpaRepository<InterestPolicy, Long> {
  public Page<InterestPolicy> findByUserId(Long id, Pageable pageable);

  public Optional<InterestPolicy> findOneByUserIdAndPolicyId(Long userId, Long policyId);

  public Long deleteByUserId(Long id);
}

