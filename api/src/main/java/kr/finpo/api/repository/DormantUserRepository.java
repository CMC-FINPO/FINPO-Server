package kr.finpo.api.repository;

import java.util.Optional;
import kr.finpo.api.domain.DormantUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DormantUserRepository extends JpaRepository<DormantUser, Long> {

    Optional<DormantUser> findOneById(Long id);

    void deleteById(Long id);
}

