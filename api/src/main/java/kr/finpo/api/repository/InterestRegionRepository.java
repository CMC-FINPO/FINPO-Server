package kr.finpo.api.repository;

import kr.finpo.api.domain.InterestRegion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InterestRegionRepository extends JpaRepository<InterestRegion, Long> {
  public List<InterestRegion> findByUserId(Long id);
  public List<InterestRegion> findByRegionId(Long id);
  public List<InterestRegion> findByRegionIdAndSubscribe(Long id, Boolean subscribe);
  public Long deleteByUserId(Long id);
  public void deleteByUserIdAndIsDefault(Long id, Boolean isDefault);

  public Optional<InterestRegion> findOneByUserIdAndIsDefault(Long userId, Boolean isDefault);
  public Optional<InterestRegion> findOneByUserIdAndRegionId(Long userId, Long regionId);
  public Optional<InterestRegion> findOneByUserIdAndRegionIdAndIsDefault(Long userId, Long regionId, Boolean isDefault);
}

