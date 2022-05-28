package kr.finpo.api.repository;

import kr.finpo.api.domain.GoogleAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GoogleAccountRepository extends JpaRepository<GoogleAccount, String> {
  Long deleteByUserId(Long id);
}

