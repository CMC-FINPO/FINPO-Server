package kr.finpo.api.repository;

import java.util.Optional;
import kr.finpo.api.domain.Fcm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Long> {

    Optional<Fcm> findFirst1ByUserId(Long id);

    Optional<Fcm> findFirst1ByUserIdAndSubscribe(Long id, Boolean subscribe);

    Optional<Fcm> findFirst1ByUserIdAndSubscribeAndCommunitySubscribe(Long id, Boolean subscribe,
        Boolean communitySubscribe);

    Long deleteByUserId(Long id);
}

