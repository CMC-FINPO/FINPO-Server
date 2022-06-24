package kr.finpo.api.repository;

import kr.finpo.api.domain.JoinedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface JoinedPolicyRepository extends JpaRepository<JoinedPolicy, Long> {
  public List<JoinedPolicy> findByUserId(Long id);
  public Long countByPolicyId(Long id);
  public Long deleteByUserId(Long id);
}

