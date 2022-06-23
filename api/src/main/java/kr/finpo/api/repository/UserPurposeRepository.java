package kr.finpo.api.repository;

import kr.finpo.api.domain.UserPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {
  Long deleteByUserId(Long id);
}

