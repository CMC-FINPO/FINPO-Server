package kr.finpo.api.repository;

import kr.finpo.api.domain.InterestPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestPolicyRepository extends JpaRepository<InterestPolicy, Long> {
  public List<InterestPolicy> findByUserId(Long id);
  public Long countByPolicyId(Long id);
  public Long deleteByUserId(Long id);
}

