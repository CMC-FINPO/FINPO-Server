package kr.finpo.api.repository;

import kr.finpo.api.domain.JoinedPolicy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JoinedPolicyRepository extends JpaRepository<JoinedPolicy, Long> {
  public Page<JoinedPolicy> findByUserId(Long id, Pageable pageable);

  public Optional<JoinedPolicy> findOneByUserIdAndPolicyId(Long userId, Long policyId);

  public Long deleteByUserId(Long id);
}

