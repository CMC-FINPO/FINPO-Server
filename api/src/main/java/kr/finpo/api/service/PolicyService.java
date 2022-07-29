package kr.finpo.api.service;

import static org.springframework.util.ObjectUtils.isEmpty;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import kr.finpo.api.constant.Constraint;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.InterestPolicy;
import kr.finpo.api.domain.JoinedPolicy;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestPolicyDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.JoinedPolicyDto;
import kr.finpo.api.dto.PolicyDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.InterestPolicyRepository;
import kr.finpo.api.repository.JoinedPolicyRepository;
import kr.finpo.api.repository.NotificationRepository;
import kr.finpo.api.repository.PolicyRepository;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final NotificationRepository notificationRepository;
    private final FcmService fcmService;

    private User getMe() {
        return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
            () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
        );
    }

    private void authorizeMe(Long id) {
        if (!id.equals(SecurityUtil.getCurrentUserId())) {
            throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
        }
    }

    private Boolean isInterest(Long id) {
        return interestPolicyRepository.findOneByUserIdAndPolicyId(SecurityUtil.getCurrentUserId(), id).isPresent();
    }

    public Boolean insertCustom(List<PolicyDto> policyDtos) {
        try {
            policyDtos.forEach(dto -> {
                Policy policy = Policy.of(dto.title(), Integer.toString(dto.title().hashCode()), dto.institution(),
                    dto.content(), null, null, null, null, null, null, null, null, null, null);
                policy.setRegion(regionRepository.findById(dto.region().getId()).get());
                policy.setCategory(categoryRepository.findById(dto.category().getId()).get());
                policy.setStatus(false);
                policyRepository.save(policy);
            });
            return true;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Boolean deleteCustom(Long id) {
        try {
            notificationRepository.deleteByPolicyId(id);
            interestPolicyRepository.deleteByPolicyId(id);
            joinedPolicyRepository.deleteByPolicyId(id);
            policyRepository.deleteById(id);
            return true;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public PolicyDto get(Long id) {
        try {
            policyRepository.increaseHits(id);
            return PolicyDto.response(policyRepository.findById(id).get(), isInterest(id));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Page<PolicyDto> getMy(Pageable pageable) {
        try {
            List<InterestCategoryDto> myCategoryDtos = categoryService.getMyInterests();
            List<InterestRegionDto> myRegionDtos = regionService.getMyInterests();

            return policyRepository.querydslFindMy(myCategoryDtos, myRegionDtos, pageable)
                .map(e -> PolicyDto.previewResponse(e, isInterest(e.getId())));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Page<PolicyDto> search(String title, LocalDate startDate, LocalDate endDate, List<Long> regionIds,
        List<Long> categoryIds, Boolean status, Pageable pageable) {
        try {
            return policyRepository.querydslFindbyTitle(title, startDate, endDate, categoryIds, regionIds, status,
                pageable).map(e -> PolicyDto.previewResponse(e, isInterest(e.getId())));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public String update(Long id, PolicyDto dto, Boolean sendNotification) {
        try {
            List<List<Long>> ret = new ArrayList<>();
            Policy policy = dto.updateEntity(policyRepository.findById(id).get());
            if (!isEmpty(dto.region())) {
                policy.setRegion(regionRepository.findById(dto.region().getId()).get());
            }
            if (!isEmpty(dto.category())) {
                policy.setCategory(categoryRepository.findById(dto.category().getId()).get());
            }
            policy = policyRepository.save(policy);
            if (sendNotification) {
                ret.add(fcmService.sendPolicyPush(policy));
            }
            return ret.toString();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public List<InterestPolicyDto> getMyInterests() {
        try {
            return interestPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream()
                .map(e -> InterestPolicyDto.response(e, isInterest(e.getPolicy().getId()))).toList();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public List<JoinedPolicyDto> getMyJoins() {
        try {
            return joinedPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream()
                .map(e -> JoinedPolicyDto.response(e, isInterest(e.getPolicy().getId()))).toList();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public InterestPolicyDto insertMyInterest(InterestPolicyDto dto) {
        try {
            if (interestPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).size()
                >= Constraint.INTEREST_POLICY_MAX_CNT) {
                throw new GeneralException(ErrorCode.BAD_REQUEST,
                    "Interest policy must equal or less than " + Constraint.INTEREST_POLICY_MAX_CNT);
            }

            User user = getMe();
            Policy policy = policyRepository.findById(dto.policyId()).get();
            if (interestPolicyRepository.findOneByUserIdAndPolicyId(user.getId(), policy.getId()).isPresent()) {
                return null;
            }
            InterestPolicy interestPolicy = InterestPolicy.of(user, policy);
            return InterestPolicyDto.response(interestPolicyRepository.save(interestPolicy), true);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public JoinedPolicyDto insertMyJoined(JoinedPolicyDto dto) {
        try {
            if (joinedPolicyRepository.findByUserId(SecurityUtil.getCurrentUserId()).size()
                >= Constraint.JOINED_POLICY_MAX_CNT) {
                throw new GeneralException(ErrorCode.BAD_REQUEST,
                    "Joined policy must equal or less than " + Constraint.JOINED_POLICY_MAX_CNT);
            }

            User user = getMe();
            Policy policy = policyRepository.findById(dto.policyId()).get();
            JoinedPolicy joinedPolicy = JoinedPolicy.of(user, policy, dto.memo());
            if (joinedPolicyRepository.findOneByUserIdAndPolicyId(user.getId(), policy.getId()).isPresent()) {
                return null;
            }
            return JoinedPolicyDto.response(joinedPolicyRepository.save(joinedPolicy), isInterest(policy.getId()));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public JoinedPolicyDto updateMyJoined(Long id, JoinedPolicyDto dto) {
        try {
            JoinedPolicy joinedPolicy = joinedPolicyRepository.findById(id).get();
            authorizeMe(joinedPolicy.getUser().getId());
            joinedPolicy.setMemo(dto.memo());
            return JoinedPolicyDto.response(joinedPolicyRepository.save(joinedPolicy),
                isInterest(joinedPolicy.getPolicy().getId()));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Boolean deleteMyInterestByPolicyId(Long policyId) {
        try {
            InterestPolicy interestPolicy = interestPolicyRepository.findOneByUserIdAndPolicyId(
                SecurityUtil.getCurrentUserId(), policyId).get();
            interestPolicyRepository.delete(interestPolicy);
            return true;
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Boolean deleteMyJoinedByPolicyId(Long policyId) {
        try {
            JoinedPolicy joinedPolicy = joinedPolicyRepository.findOneByUserIdAndPolicyId(
                SecurityUtil.getCurrentUserId(), policyId).get();
            joinedPolicyRepository.delete(joinedPolicy);
            return true;
        } catch (GeneralException e) {
            throw e;
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
        } catch (GeneralException e) {
            throw e;
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
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }
}

