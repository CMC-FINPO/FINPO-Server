package kr.finpo.api.repository;

import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.BlockedUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BlockedUserRepository extends JpaRepository<BlockedUser, Long> {

    List<BlockedUser> findByUserIdAndAnonymityOrderByIdDesc(Long userId, Boolean anonymity);

    Optional<BlockedUser> findOneByUserIdAndBlockedUserIdAndAnonymity(Long userId, Long blockedUserId,
        Boolean anonymity);
}

