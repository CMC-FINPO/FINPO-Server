package kr.finpo.api.service;


import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.GoogleTokenDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.dto.WithdrawDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class UserService {

  private final RegionService regionService;
  private final UserRepository userRepository;
  private final UserPurposeRepository userPurposeRepository;
  private final InterestRegionRepository interestRegionRepository;
  private final InterestCategoryRepository interestCategoryRepository;
  private final RegionRepository regionRepository;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final GoogleAccountRepository googleAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final S3Uploader s3Uploader;

  @Value("${upload.url}")
  private String uploadUrl;

  @Value("${oauth.kakao.admin-key}")
  private String kakaoAdminKey;

  public List<UserDto> getAll() {
    try {
      return StreamSupport.stream(userRepository.findAll().spliterator(), false).map(UserDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<UserDto> getById(Long id) {
    try {
      return userRepository.findById(id).map(UserDto::response);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<UserDto> getMyInfo() {
    try {
      return userRepository.findById(SecurityUtil.getCurrentUserId()).map(UserDto::response);
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
      if (StringUtils.hasText(dto.email()) && isEmailDuplicated(dto.email()))
        throw new GeneralException(ErrorCode.VALIDATION_ERROR, "email duplicated");

      User user = dto.updateEntity(userRepository.findById(id).get());

      if (dto.regionId() != null)
        regionService.updateMyDefault(InterestRegionDto.of(dto.regionId(), true));

      log.debug("purposeId: : " + dto.purposeIds());
      if (dto.purposeIds() != null) {
        userPurposeRepository.deleteByUserId(user.getId());

        dto.purposeIds().forEach(purposeId -> {
          UserPurpose userPurpose = UserPurpose.of(purposeId);
          userPurpose.setUser(user);
          userPurposeRepository.save(userPurpose);
        });
      }
      return UserDto.response(userRepository.save(user));
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

      return UserDto.response(user);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMe(WithdrawDto body) {
    return delete(SecurityUtil.getCurrentUserId(), body);
  }

  public Boolean delete(Long id, WithdrawDto dto) {
    try {
      User user = userRepository.findById(id).get();

      interestRegionRepository.deleteByUserId(id);
      interestCategoryRepository.deleteByUserId(id);

      try {
        HttpHeaders headers = new HttpHeaders();
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        if (user.getOAuthType().equals(OAuthType.KAKAO)) {
          KakaoAccount kakaoAccount = kakaoAccountRepository.findByUserId(id).get();

          headers.set("Authorization", "KakaoAK " + kakaoAdminKey);
          headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

          params.add("target_id_type", "user_id");
          params.add("target_id", kakaoAccount.getId());

          new RestTemplate().exchange(
              "https://kapi.kakao.com/v1/user/unlink",
              HttpMethod.POST,
              new HttpEntity<>(params, headers),
              String.class
          );
        }
        else if (user.getOAuthType().equals(OAuthType.GOOGLE)) {
          headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
          params.add("token", dto.access_token());

          new RestTemplate().exchange(
              "https://accounts.google.com/o/oauth2/revoke",
              HttpMethod.POST,
              new HttpEntity<>(params, headers),
              String.class
          );

        }

      } catch (HttpClientErrorException | NoSuchElementException e) {
        return false;
      } finally {
        kakaoAccountRepository.deleteByUserId(id);
        googleAccountRepository.deleteByUserId(id);
        refreshTokenRepository.deleteByUserId(id);
        userRepository.deleteById(id);
      }
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

