package kr.finpo.api.repository;

import java.util.Optional;
import kr.finpo.api.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findOneByUserId(Long id);

    Long deleteByUserId(Long id);
}

