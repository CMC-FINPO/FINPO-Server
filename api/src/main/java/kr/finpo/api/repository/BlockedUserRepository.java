package kr.finpo.api.repository;

import kr.finpo.api.domain.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {
  public List<BlockedUser> findByUserId(Long userId);

  public Optional<BlockedUser> findOneByUserIdAndBlockedUserId(Long userId, Long blockedUserId);
}

