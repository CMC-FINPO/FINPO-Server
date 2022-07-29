package kr.finpo.api.repository;

import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

    Optional<Region> findById(Long id);

    Optional<Region> findOneByName(String name);

    List<Region> findByParentId(Long parentId);

    List<Region> findByParentIdOrderByNameAsc(Long parentId);

    List<Region> findByDepth(Long depth);
}

