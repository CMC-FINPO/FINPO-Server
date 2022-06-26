package kr.finpo.api.repository;

import kr.finpo.api.domain.Policy;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface PolicyRepository extends JpaRepository<Policy, Long>, PolicyRepositoryCustom{
  Optional<Policy> findOneByPolicyKey(String policyKey);

  @Modifying
  @Query("update Policy p set p.hits = p.hits + 1 where p.id = :id")
  int increaseHits(Long id);
}

