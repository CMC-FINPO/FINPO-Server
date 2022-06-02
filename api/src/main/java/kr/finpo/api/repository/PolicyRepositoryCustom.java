package kr.finpo.api.repository;

import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.Region;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.PolicyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;


public interface PolicyRepositoryCustom {
  Page<PolicyDto> querydslFindMy(List<InterestCategoryDto> myCategoryDtos, List<InterestRegionDto> myRegionDtos, Pageable pageable);

  Page<PolicyDto> querydslFindbyTitle(String title, LocalDate startDate, LocalDate endDate, List<Long> categoryIds, List<Long> regionIds, Pageable pageable);
}

