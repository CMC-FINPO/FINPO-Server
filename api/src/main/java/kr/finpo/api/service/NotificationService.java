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

      if (dto.subscribe() != null) {
        if (dto.subscribe().equals(true)) { // 알림 구독 설정
          if (fcm.isPresent() && dto.registrationToken() != null) {
            fcm.get().setRegistrationToken(dto.registrationToken());
            fcmRepository.save(fcm.get());
          }
          else if (fcm.isEmpty()) // insert
            insertMy(dto);
        }
        else  // 알림 구독 해제
          fcmRepository.deleteByUserId(user.getId());
      }
      if (dto.interestCategories() != null)
        dto.interestCategories().forEach(interestCategoryDto -> {
          InterestCategory interestCategory = interestCategoryRepository.findById(interestCategoryDto.id()).get();
          interestCategory.setSubscribe(interestCategoryDto.subscribe());
          interestCategoryRepository.save(interestCategory);
        });

      return getMy();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public NotificationDto insertMy(NotificationDto dto) {
    try {
      User user = getMe();
      Fcm fcm = Fcm.of(dto.registrationToken());
      fcm.setUser(user);
      return NotificationDto.response(fcmRepository.save(fcm), interestCategoryRepository.findByUserId(user.getId()));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

