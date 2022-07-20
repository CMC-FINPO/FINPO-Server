package kr.finpo.api.repository;

import java.time.LocalDate;
import java.util.List;
import kr.finpo.api.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  public Optional<User> findByKakaoAccountId(String id);
  public Optional<User> findByGoogleAccountId(String id);
  public Optional<User> findByAppleAccountId(String id);
  public Optional<User> findByNickname(String name);
  public Optional<User> findByEmail(String email);
  public List<User> findByLastRefreshedDate(LocalDate date);
  public Page<User> findAllByStatus(Boolean status, Pageable pageable);
}

