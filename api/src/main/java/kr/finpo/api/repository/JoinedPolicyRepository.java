package kr.finpo.api.repository;

import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.JoinedPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface JoinedPolicyRepository extends JpaRepository<JoinedPolicy, Long> {

    List<JoinedPolicy> findByUserId(Long id);

    Optional<JoinedPolicy> findOneByUserIdAndPolicyId(Long userId, Long policyId);

    Long deleteByUserId(Long id);

    Long deleteByPolicyId(Long policyId);
}

