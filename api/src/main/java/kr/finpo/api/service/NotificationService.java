package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.*;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
  private final InterestCategoryRepository interestCategoryRepository;
  private final UserRepository userRepository;

  public User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  public NotificationDto getMy() {
    try {
      User user = getMe();
      Fcm fcm = fcmRepository.findOneByUserId(user.getId()).orElse(null);
      List<InterestCategory> interestCategories = interestCategoryRepository.findByUserId(user.getId());
      return NotificationDto.response(fcm, interestCategories);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public NotificationDto upsertMy(NotificationDto dto) {
    try {
      User user = getMe();
      Optional<Fcm> fcm = fcmRepository.findOneByUserId(user.getId());

      if (fcm.isEmpty()) { // insert
        if (dto.registrationToken() == null)
          throw new GeneralException(ErrorCode.BAD_REQUEST, "There's no registeration token");
        fcmRepository.deleteByUserId(user.getId());
        insertMy(dto);
      }
      else { // update
        if (dto.subscribe() != null) fcm.get().setSubscribe(dto.subscribe());
        if (dto.registrationToken() != null) fcm.get().setRegistrationToken(dto.registrationToken());
        fcmRepository.save(fcm.get());
      }

      if (dto.interestCategories() != null)
        dto.interestCategories().forEach(interestCategoryDto -> {
          InterestCategory interestCategory = interestCategoryRepository.findById(interestCategoryDto.id()).get();
          interestCategory.setSubscribe(interestCategoryDto.subscribe());
          interestCategoryRepository.save(interestCategory);
        });

      return getMy();
    } catch (Exception e) {
      log.debug(dto.toString());
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public NotificationDto insertMy(NotificationDto dto) {
    try {
      User user = getMe();
      Fcm fcm = Fcm.of(dto.subscribe(), dto.registrationToken());
      fcm.setUser(user);
      return NotificationDto.response(fcmRepository.save(fcm), interestCategoryRepository.findByUserId(user.getId()));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

