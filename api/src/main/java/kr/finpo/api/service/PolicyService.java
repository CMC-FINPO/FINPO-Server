package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.PolicyDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.PolicyRepository;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.service.openapi.GgdataService;
import kr.finpo.api.service.openapi.YouthcenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class PolicyService {

  private final CategoryService categoryService;
  private final RegionService regionService;
  private final PolicyRepository policyRepository;


  public Page<PolicyDto> getMy(Pageable pageable) {
    try {
      List<InterestCategoryDto> myCategoryDtos = categoryService.getMyInterests();
      List<InterestRegionDto> myRegionDtos = regionService.getMyInterests();

      return policyRepository.querydslFindMy(myCategoryDtos, myRegionDtos, pageable);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PolicyDto> search(String title, LocalDate startDate, LocalDate endDate, List<Long> regionIds, List<Long> categoryIds, Pageable pageable) {
    try {
      return policyRepository.querydslFindbyTitle(title, startDate, endDate, categoryIds, regionIds, pageable);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

}

