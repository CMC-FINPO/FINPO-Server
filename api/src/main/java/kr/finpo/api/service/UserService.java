package kr.finpo.api.service;


import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.GoogleAccount;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class UserService {

  private final UserRepository userRepository;
  private final RegionRepository regionRepository;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final GoogleAccountRepository googleAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final S3Uploader s3Uploader;

  @Value("${upload.url}")
  private String uploadUrl;

  public List<UserDto> getAll() {
    try {
      return StreamSupport.stream(userRepository.findAll().spliterator(), false).map(UserDto::info).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<UserDto> getById(Long id) {
    try {
      return userRepository.findById(id).map(UserDto::info);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<UserDto> getMyInfo() {
    try {
      return userRepository.findById(SecurityUtil.getCurrentUserId()).map(UserDto::info);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public UserDto updateMe(UserDto dto) {
    return update(SecurityUtil.getCurrentUserId(), dto);
  }


  public UserDto update(Long id, UserDto dto) {
    try {
      if (isNicknameDuplicated(dto.nickname()))
        throw new GeneralException(ErrorCode.VALIDATION_ERROR, "nickname duplicated");
      if (isEmailDuplicated(dto.email()))
        throw new GeneralException(ErrorCode.VALIDATION_ERROR, "email duplicated");

      User user = dto.updateEntity(userRepository.findById(id).get());

      Region region = regionRepository.findOneByUserIdAndIsDefault(user.getId(), true).get();
      region.update(dto.region1(), dto.region2());
      regionRepository.save(region);

      return UserDto.info(userRepository.save(user));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public UserDto updateMyProfileImg(UserDto dto) {
    try {
      String profileImgUrl = uploadUrl + s3Uploader.uploadFile("profile", dto.profileImgFile());

      User user = userRepository.findById(SecurityUtil.getCurrentUserId()).get();
      user.setProfileImg(profileImgUrl);
      userRepository.save(user);

      return UserDto.info(user);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMe() {
    return delete(SecurityUtil.getCurrentUserId());
  }

  public Boolean delete(Long id) {
    try {
      regionRepository.deleteByUserId(id);
      kakaoAccountRepository.deleteByUserId(id);
      googleAccountRepository.deleteByUserId(id);
      refreshTokenRepository.deleteByUserId(id);
      userRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Boolean isNicknameDuplicated(String nickname) {
    try {
      if (!userRepository.findByNickname(nickname).isPresent())
        return false;

      if (SecurityUtil.isUserLogin())
        if (nickname.equals(userRepository.findById(SecurityUtil.getCurrentUserId()).get().getNickname()))
          return false;

      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean isEmailDuplicated(String email) {
    try {
      if (!userRepository.findByEmail(email).isPresent())
        return false;

      if (SecurityUtil.isUserLogin())
        if (email.equals(userRepository.findById(SecurityUtil.getCurrentUserId()).get().getEmail()))
          return false;

      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

