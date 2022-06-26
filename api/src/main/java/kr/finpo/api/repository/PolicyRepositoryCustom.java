package kr.finpo.api.repository;

import kr.finpo.api.domain.Policy;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.PolicyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;


public interface PolicyRepositoryCustom {
  Page<Policy> querydslFindMy(List<InterestCategoryDto> myCategoryDtos, List<InterestRegionDto> myRegionDtos, Pageable pageable);

  Page<Policy> querydslFindbyTitle(String title, LocalDate startDate, LocalDate endDate, List<Long> categoryIds, List<Long> regionIds, Pageable pageable);
}

