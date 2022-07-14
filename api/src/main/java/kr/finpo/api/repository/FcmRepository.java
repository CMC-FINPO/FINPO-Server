package kr.finpo.api.repository;

import kr.finpo.api.domain.Fcm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Long> {
  public Optional<Fcm> findFirst1ByUserId(Long id);

  public Optional<Fcm> findFirst1ByUserIdAndSubscribe(Long id, Boolean subscribe);

  public Optional<Fcm> findFirst1ByUserIdAndSubscribeAndCommunitySubscribe(Long id, Boolean subscribe, Boolean communitySubscribe);

  Long deleteByUserId(Long id);
}

