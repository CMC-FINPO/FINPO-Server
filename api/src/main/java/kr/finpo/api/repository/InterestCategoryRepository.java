package kr.finpo.api.repository;

import kr.finpo.api.domain.InterestCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InterestCategoryRepository extends JpaRepository<InterestCategory, Long> {
  public List<InterestCategory> findByUserId(Long id);
  public List<InterestCategory> findByCategoryIdAndSubscribe(Long categoryId, Boolean subscribe);
  public Optional<InterestCategory> findByUserIdAndCategoryId(Long id, Long categoryId);
  public Long deleteByUserId(Long id);
}

