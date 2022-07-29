package kr.finpo.api.repository;

import java.util.List;
import kr.finpo.api.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    List<Category> findByParentId(Long parentId);

    List<Category> findByDepth(Long depth);
}

