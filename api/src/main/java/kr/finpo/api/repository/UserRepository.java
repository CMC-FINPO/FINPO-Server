package kr.finpo.api.repository;

import kr.finpo.api.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  public Optional<User> findByKakaoAccountId(String id);
  public Optional<User> findByGoogleAccountId(String id);
  public Optional<User> findByNickname(String name);
  public Optional<User> findByEmail(String email);
}

