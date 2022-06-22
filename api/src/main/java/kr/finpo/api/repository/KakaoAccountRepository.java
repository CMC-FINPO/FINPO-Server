package kr.finpo.api.repository;

import kr.finpo.api.domain.KakaoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface KakaoAccountRepository extends JpaRepository<KakaoAccount, String> {
  Long deleteByUserId(Long id);
  Optional<KakaoAccount> findByUserId(Long userId);

}

