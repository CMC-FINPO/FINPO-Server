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
import java.util.ArrayList;
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
  private final RegionRepository regionRepository;
  private final CategoryRepository categoryRepository;
  private final FcmService fcmService;

  private User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  private void authorizeMe(Long id) {
    if (!id.equals(SecurityUtil.getCurrentUserId()))
      throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
  }

  private Boolean isInterest(Long id) {
    return interestPolicyRepository.findOneByUserIdAndPolicyId(SecurityUtil.getCurrentUserId(), id).isPresent();
  }

  public String insertCustom(List<PolicyDto> policyDtos) {
    try {
      List<List<Long>> ret = new ArrayList<>();
      policyDtos.forEach(dto -> {
        Policy policy = Policy.of(dto.title(), Integer.toString(dto.title().hashCode()), dto.institution(), dto.content(), null, null, null, null, null, null, null, null, null, null);
        policy.setRegion(regionRepository.findById(dto.region().getId()).get());
        policy.setCategory(categoryRepository.findById(dto.category().getId()).get());
        policyRepository.save(policy);
        ret.add(fcmService.sendPolicyPush(policy));
      });
      return ret.toString();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteCustom(Long id) {
    try {
      policyRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PolicyDto get(Long id) {
    try {
      policyRepository.increaseHits(id);
      return PolicyDto.response(policyRepository.findById(id).get(), isInterest(id));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PolicyDto> getMy(Pageable pageable) {
    try {
      List<InterestCategoryDto> myCategoryDtos = categoryService.getMyInterests();
      List<InterestRegionDto> myRegionDtos = regionService.getMyInterests();

      return policyRepository.querydslFindMy(myCategoryDtos, myRegionDtos, pageable).map(e -> PolicyDto.previewResponse(e, isInterest(e.getId())));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PolicyDto> search(String title, LocalDate startDate, LocalDate endDate, List<Long> regionIds, List<Long> categoryIds, Pageable pageable) {
    try {
      return policyRepository.querydslFindbyTitle(title, startDate, endDate, categoryIds, regionIds, pageable).map(e -> PolicyDto.previewResponse(e, isInterest(e.getId())));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public List<InterestPolicyDto> getMyInterests() {
    try {
      return interestPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(e->InterestPolicyDto.response(e, isInterest(e.getPolicy().getId()))).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<JoinedPolicyDto> getMyJoins() {
    try {
      return joinedPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(e->JoinedPolicyDto.response(e, isInterest(e.getPolicy().getId()))).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public InterestPolicyDto insertMyInterest(InterestPolicyDto dto) {
    try {
      User user = getMe();
      Policy policy = policyRepository.findById(dto.policyId()).get();
      if (interestPolicyRepository.findOneByUserIdAndPolicyId(user.getId(), policy.getId()).isPresent())
        return null;
      InterestPolicy interestPolicy = InterestPolicy.of(user, policy);
      return InterestPolicyDto.response(interestPolicyRepository.save(interestPolicy), true);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public JoinedPolicyDto insertMyJoined(JoinedPolicyDto dto) {
    try {
      User user = getMe();
      Policy policy = policyRepository.findById(dto.policyId()).get();
      JoinedPolicy joinedPolicy = JoinedPolicy.of(user, policy, dto.memo());
      if (joinedPolicyRepository.findOneByUserIdAndPolicyId(user.getId(), policy.getId()).isPresent())
        return null;
      return JoinedPolicyDto.response(joinedPolicyRepository.save(joinedPolicy),isInterest(policy.getId()));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public JoinedPolicyDto updateMyJoined(Long id, JoinedPolicyDto dto) {
    try {
      JoinedPolicy joinedPolicy = joinedPolicyRepository.findById(id).get();
      authorizeMe(joinedPolicy.getUser().getId());
      joinedPolicy.setMemo(dto.memo());
      return JoinedPolicyDto.response(joinedPolicyRepository.save(joinedPolicy), isInterest(joinedPolicy.getPolicy().getId()));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyInterestByPolicyId(Long policyId) {
    try {
      InterestPolicy interestPolicy = interestPolicyRepository.findOneByUserIdAndPolicyId(SecurityUtil.getCurrentUserId(), policyId).get();
      interestPolicyRepository.delete(interestPolicy);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyJoinedByPolicyId(Long policyId) {
    try {
      JoinedPolicy joinedPolicy = joinedPolicyRepository.findOneByUserIdAndPolicyId(SecurityUtil.getCurrentUserId(), policyId).get();
      joinedPolicyRepository.delete(joinedPolicy);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyInterest(Long id) {
    try {
      InterestPolicy interestPolicy = interestPolicyRepository.findById(id).get();
      authorizeMe(interestPolicy.getUser().getId());
      interestPolicyRepository.delete(interestPolicy);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyJoined(Long id) {
    try {
      JoinedPolicy joinedPolicy = joinedPolicyRepository.findById(id).get();
      authorizeMe(joinedPolicy.getUser().getId());
      joinedPolicyRepository.delete(joinedPolicy);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

}

