package kr.finpo.api.repository;

import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.Policy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  List<Category> findByParentId(Long parentId);
  List<Category> findByDepth(Long depth);
}

