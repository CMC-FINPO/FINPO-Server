package kr.finpo.api.repository;

import kr.finpo.api.domain.CommunityReport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface CommunityReportRepository extends JpaRepository<CommunityReport, Long> {

    Page<CommunityReport> findAll(Pageable pageable);
}

