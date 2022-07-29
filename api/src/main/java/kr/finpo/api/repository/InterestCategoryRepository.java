package kr.finpo.api.repository;

import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.InterestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InterestCategoryRepository extends JpaRepository<InterestCategory, Long> {

    List<InterestCategory> findByUserId(Long id);

    List<InterestCategory> findByCategoryIdAndSubscribe(Long categoryId, Boolean subscribe);

    Optional<InterestCategory> findByUserIdAndCategoryId(Long id, Long categoryId);

    Long deleteByUserId(Long id);
}

