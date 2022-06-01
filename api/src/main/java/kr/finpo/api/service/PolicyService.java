package kr.finpo.api.service;


import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.PolicyDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.PolicyRepository;
import kr.finpo.api.service.openapi.GgdataService;
import kr.finpo.api.service.openapi.YouthcenterService;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.StreamSupport;

import static org.springframework.util.ObjectUtils.isEmpty;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class PolicyService {

  private final GgdataService ggdataService;
  private final YouthcenterService youthcenterService;
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

}

