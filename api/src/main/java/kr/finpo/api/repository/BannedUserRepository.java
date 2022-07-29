package kr.finpo.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.BannedUser;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BannedUserRepository extends JpaRepository<BannedUser, Long> {

    Page<BannedUser> findByReleaseDateGreaterThan(LocalDate date, Pageable pageable);

    List<BannedUser> findByReleaseDate(LocalDate date);

    List<BannedUser> findByUserIdOrderByIdDesc(Long userId);

    Optional<BannedUser> findByUserIdAndReleaseDateGreaterThan(Long userId, LocalDate date);
}

