package kr.finpo.api.repository;

import kr.finpo.api.domain.Region;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {
  public List<Region> findByUserId(Long id);
  Long deleteByUserId(Long id);


  public Optional<Region> findOneByUserIdAndIsDefault(Long id, Boolean isDefault);
}

