package kr.finpo.api.repository;

import java.util.Optional;
import kr.finpo.api.domain.KakaoAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface KakaoAccountRepository extends JpaRepository<KakaoAccount, String> {

    Long deleteByUserId(Long id);

    Optional<KakaoAccount> findByUserId(Long userId);

}

