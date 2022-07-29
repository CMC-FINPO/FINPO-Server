package kr.finpo.api.repository;

import java.util.List;
import kr.finpo.api.domain.UserPurpose;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface UserPurposeRepository extends JpaRepository<UserPurpose, Long> {

    List<UserPurpose> findByUserId(Long userId);

    Long deleteByUserId(Long id);
}

