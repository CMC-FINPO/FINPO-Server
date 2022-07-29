package kr.finpo.api.repository;

import java.util.List;
import kr.finpo.api.domain.Information;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface InformationRepository extends JpaRepository<Information, Long> {

    List<Information> findByTypeAndHidden(String type, Boolean hidden);
}

