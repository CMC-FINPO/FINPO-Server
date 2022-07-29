package kr.finpo.api.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByKakaoAccountId(String id);

    Optional<User> findByGoogleAccountId(String id);

    Optional<User> findByAppleAccountId(String id);

    Optional<User> findByNickname(String name);

    Optional<User> findByEmail(String email);

    List<User> findByLastRefreshedDate(LocalDate date);

    Page<User> findAllByStatus(Boolean status, Pageable pageable);
}

