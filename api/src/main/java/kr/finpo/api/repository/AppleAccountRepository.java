package kr.finpo.api.repository;

import kr.finpo.api.domain.AppleAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppleAccountRepository extends JpaRepository<AppleAccount, String> {

    Long deleteByUserId(Long id);
}

