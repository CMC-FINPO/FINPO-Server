package kr.finpo.api.repository;

import kr.finpo.api.domain.BannedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


@Repository
public interface BannedUserRepository extends JpaRepository<BannedUser, Long> {
  public Page<BannedUser> findByReleaseDateGreaterThan(LocalDate date, Pageable pageable);
  public List<BannedUser> findByReleaseDateLessThanEqual(LocalDate date);

  public List<BannedUser> findByUserId(Long userId);

  public Optional<BannedUser> findByUserIdAndReleaseDateGreaterThan(Long userId, LocalDate date);
}

