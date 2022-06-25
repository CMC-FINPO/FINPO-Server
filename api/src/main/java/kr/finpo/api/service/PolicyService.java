package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.*;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
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
  private final InterestPolicyRepository interestPolicyRepository;
  private final JoinedPolicyRepository joinedPolicyRepository;
  private final UserRepository userRepository;

  public PolicyDto get(Long id) {
    try {
      return PolicyDto.response(policyRepository.findById(id).get());
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

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


  public List<InterestPolicyDto> getMyInterests() {
    try {
      return interestPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(InterestPolicyDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<JoinedPolicyDto> getMyJoins() {
    try {
      return joinedPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(JoinedPolicyDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public InterestPolicyDto insertMyInterest(InterestPolicyDto dto) {
    try {
      log.debug(dto.toString());
      User user = userRepository.findById(SecurityUtil.getCurrentUserId()).get();
      Policy policy = policyRepository.findById(dto.policyId()).get();
      InterestPolicy interestPolicy = InterestPolicy.of(user, policy);
      log.debug(interestPolicy.getPolicy().toString());
      return InterestPolicyDto.response(interestPolicyRepository.save(interestPolicy));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public JoinedPolicyDto insertMyJoined(JoinedPolicyDto dto) {
    try {
      User user = userRepository.findById(SecurityUtil.getCurrentUserId()).get();
      Policy policy = policyRepository.findById(dto.policyId()).get();
      JoinedPolicy joinedPolicy = JoinedPolicy.of(user, policy, dto.memo());
      return JoinedPolicyDto.response(joinedPolicyRepository.save(joinedPolicy));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public JoinedPolicyDto updateMyJoined(JoinedPolicyDto dto) {
    try {
      JoinedPolicy joinedPolicy = joinedPolicyRepository.findById(dto.id()).get();
      if (!joinedPolicy.getUser().getId().equals(SecurityUtil.getCurrentUserId()))
        throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
      joinedPolicy.setMemo(dto.memo());
      return JoinedPolicyDto.response(joinedPolicyRepository.save(joinedPolicy));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyInterest(Long id) {
    try {
      InterestPolicy interestPolicy = interestPolicyRepository.findById(id).get();
      if (!interestPolicy.getUser().getId().equals(SecurityUtil.getCurrentUserId()))
        throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
      interestPolicyRepository.delete(interestPolicy);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyJoined(Long id) {
    try {
      JoinedPolicy joinedPolicy = joinedPolicyRepository.findById(id).get();
      if (!joinedPolicy.getUser().getId().equals(SecurityUtil.getCurrentUserId()))
        throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
      joinedPolicyRepository.delete(joinedPolicy);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

}

