package kr.finpo.api.repository;

import java.util.List;
import java.util.Optional;
import kr.finpo.api.domain.InterestRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InterestRegionRepository extends JpaRepository<InterestRegion, Long> {

    List<InterestRegion> findByUserId(Long id);

    List<InterestRegion> findByRegionId(Long id);

    List<InterestRegion> findByRegionIdAndSubscribe(Long id, Boolean subscribe);

    Long deleteByUserId(Long id);

    void deleteByUserIdAndIsDefault(Long id, Boolean isDefault);

    Optional<InterestRegion> findOneByUserIdAndIsDefault(Long userId, Boolean isDefault);

    Optional<InterestRegion> findOneByUserIdAndRegionId(Long userId, Long regionId);

    Optional<InterestRegion> findOneByUserIdAndRegionIdAndIsDefault(Long userId, Long regionId,
        Boolean isDefault);
}

