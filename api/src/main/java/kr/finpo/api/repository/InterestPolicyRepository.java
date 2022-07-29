package kr.finpo.api.repository;

import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.InterestPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InterestPolicyRepository extends JpaRepository<InterestPolicy, Long> {

    List<InterestPolicy> findByUserId(Long id);

    Optional<InterestPolicy> findOneByUserIdAndPolicyId(Long userId, Long policyId);

    Long deleteByUserId(Long id);

    Long deleteByPolicyId(Long policyId);
}

