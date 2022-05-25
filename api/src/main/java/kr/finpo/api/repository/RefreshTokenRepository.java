package kr.finpo.api.repository;

import kr.finpo.api.domain.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
  public Optional<RefreshToken> findByUserId(Long id);
}

