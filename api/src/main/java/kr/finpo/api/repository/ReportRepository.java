package kr.finpo.api.repository;

import kr.finpo.api.domain.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

}

