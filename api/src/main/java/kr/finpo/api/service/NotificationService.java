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

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class NotificationService {

  private final FcmRepository fcmRepository;
  private final NotificationRepository notificationRepository;
  private final InterestCategoryRepository interestCategoryRepository;
  private final InterestRegionRepository interestRegionRepository;
  private final UserRepository userRepository;

  public User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  private void authorizeMe(Long id) {
    if (!id.equals(SecurityUtil.getCurrentUserId()))
      throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
  }

  public FcmDto getMy() {
    try {
      User user = getMe();
      Fcm fcm = fcmRepository.findFirst1ByUserId(user.getId()).orElse(null);
      List<InterestCategory> interestCategories = interestCategoryRepository.findByUserId(user.getId());
      List<InterestRegion> interestRegions = interestRegionRepository.findByUserId(user.getId());
      return FcmDto.response(fcm, interestCategories, interestRegions);
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public FcmDto upsertMy(FcmDto dto) {
    try {
      User user = getMe();
      Optional<Fcm> fcm = fcmRepository.findFirst1ByUserId(user.getId());

      if (fcm.isEmpty()) { // insert
        if (dto.registrationToken() == null)
          throw new GeneralException(ErrorCode.BAD_REQUEST, "There's no registration token");
        fcmRepository.deleteByUserId(user.getId());
        insertMy(dto);
      }
      else { // update
        fcmRepository.save(dto.updateEntity(fcm.get()));
      }

      if (dto.interestCategories() != null)
        dto.interestCategories().forEach(interestCategoryDto -> {
          InterestCategory interestCategory = interestCategoryRepository.findById(interestCategoryDto.id()).get();
          interestCategory.setSubscribe(interestCategoryDto.subscribe());
          interestCategoryRepository.save(interestCategory);
        });

      if (dto.interestRegions() != null)
        dto.interestRegions().forEach(interestRegionDto -> {
          InterestRegion interestRegion = interestRegionRepository.findById(interestRegionDto.id()).get();
          interestRegion.setSubscribe(interestRegionDto.subscribe());
          interestRegionRepository.save(interestRegion);
        });

      return getMy();
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public void insertMy(FcmDto dto) {
    try {
      User user = getMe();
      Fcm fcm = Fcm.of(dto.subscribe(), dto.registrationToken());
      fcm.setUser(user);
      fcmRepository.save(fcm);
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<NotificationDto> getMyHistories(Long lastId, Pageable pageable) {
    try {
      if (Optional.ofNullable(lastId).isPresent())
        return notificationRepository.findByUserId(SecurityUtil.getCurrentUserId(), lastId, pageable).map(NotificationDto::response);
      return notificationRepository.findByUserId(SecurityUtil.getCurrentUserId(), pageable).map(NotificationDto::response);
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyHistory(Long id) {
    try {
      authorizeMe(notificationRepository.findById(id).get().getUser().getId());
      notificationRepository.deleteById(id);
      return true;
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

