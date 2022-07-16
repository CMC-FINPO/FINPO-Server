package kr.finpo.api.repository;

import kr.finpo.api.domain.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface InformationRepository extends JpaRepository<Information, Long> {
  public List<Information> findByTypeAndHidden(String type, Boolean hidden);
}

