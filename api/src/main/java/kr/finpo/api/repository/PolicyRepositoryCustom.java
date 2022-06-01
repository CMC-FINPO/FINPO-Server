package kr.finpo.api.repository;

import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.PolicyDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;


public interface PolicyRepositoryCustom {
  Page<PolicyDto> querydslFindMy(List<InterestCategoryDto> myCategoryDtos, List<InterestRegionDto> myRegionDtos, Pageable pageable);
}

