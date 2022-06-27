package kr.finpo.api.repository;

import kr.finpo.api.domain.Fcm;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FcmRepository extends JpaRepository<Fcm, Long> {
  public Optional<Fcm> findOneByUserId(Long id);
  Long deleteByUserId(Long id);
}

