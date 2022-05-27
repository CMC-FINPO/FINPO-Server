package kr.finpo.api.service;

import com.amazonaws.services.ec2.model.PrincipalType;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.KakaoAccount;
import kr.finpo.api.domain.RefreshToken;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.KakaoTokenDto;
import kr.finpo.api.dto.KakaoAccountDto;
import kr.finpo.api.dto.TokenDto;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.jwt.TokenProvider;
import kr.finpo.api.repository.KakaoAccountRepository;
import kr.finpo.api.repository.RefreshTokenRepository;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuthService {

  private final TokenProvider tokenProvider;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final RegionRepository regionRepository;
  private final S3Uploader s3Uploader;


  @Value("${oauth.kakao.rest-api-key}")
  private String kakaoApiKey;
  @Value("${oauth.kakao.redirect-uri}")
  private String kakaoRedirectUri;
  @Value("${upload.url}")
  private String uploadUrl;


  public KakaoTokenDto getKakaoAccessToken(String code) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("grant_type", "authorization_code");
      params.add("client_id", kakaoApiKey);
      params.add("redirect_uri", kakaoRedirectUri);
      params.add("code", code);

      ResponseEntity<KakaoTokenDto> response = new RestTemplate().exchange(
          "https://kauth.kakao.com/oauth/token",
          HttpMethod.POST,
          new HttpEntity<>(params, headers),
          KakaoTokenDto.class
      );

      return response.getBody();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.KAKAO_SERVER_ERROR, e);
    }
  }


  public KakaoAccountDto getKakaoAccount(String accessToken) {
    try {
      if (accessToken.indexOf("Bearer ") != 0) accessToken = "Bearer " + accessToken;

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", accessToken);
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      ResponseEntity<KakaoAccountDto> response = new RestTemplate().exchange(
          "https://kapi.kakao.com/v2/user/me",
          HttpMethod.POST,
          new HttpEntity<>(null, headers),
          KakaoAccountDto.class
      );

      return response.getBody();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.KAKAO_SERVER_ERROR, e);
    }
  }


  public Object loginWithKakaoToken(String kakaoAccessToken) {
    try {
      KakaoAccountDto kakaoAccount = getKakaoAccount(kakaoAccessToken);

      Optional<User> user = userRepository.findByKakaoAccountId(kakaoAccount.id());
      if (user.isEmpty()) return kakaoAccount.toUserDto();

      TokenDto tokenDto = tokenProvider.generateTokenDto(user.get());

      refreshTokenRepository.findByUserId(user.get().getId())
          .ifPresent(refreshToken -> {
            refreshTokenRepository.delete(refreshToken);
          });

      RefreshToken refreshToken = RefreshToken.of(tokenDto.getRefreshToken());
      refreshToken.setUser(user.get());
      refreshTokenRepository.save(refreshToken);

      return tokenDto;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.INTERNAL_ERROR, e);
    }
  }


  public TokenDto registerByKakao(String kakaoAccessToken, UserDto dto) {
    try {
      String kakaoAccountId = getKakaoAccount(kakaoAccessToken).id();

      // kakao id duplication check
      kakaoAccountRepository.findById(kakaoAccountId).ifPresent(s -> {
        throw new GeneralException(ErrorCode.USER_ALREADY_REGISTERED);
      });

      // nickname duplication check
      userRepository.findByNickname(dto.nickname()).ifPresent(e -> {
        throw new GeneralException(ErrorCode.NICKNAME_DUPLICATED);
      });

      String profileImgUrl = null;
      if (dto.profileImgFile() != null)
        profileImgUrl = uploadUrl + s3Uploader.uploadFile("profile", dto.profileImgFile());

      Region defaultRegion = Region.of(dto.region1(), dto.region2(), true);
      defaultRegion = regionRepository.save(defaultRegion);

      User user = dto.toEntity();
      user.setProfileImg(profileImgUrl);
      user.setOAuthType(OAuthType.KAKAO);
      user.setDefaultRegion(defaultRegion);
      user = userRepository.save(user);

      defaultRegion.setUser(user);
      regionRepository.save(defaultRegion);

      KakaoAccount kakaoAccount = kakaoAccountRepository.save(KakaoAccount.of(kakaoAccountId));
      kakaoAccount.setUser(user);

      TokenDto tokenDto = tokenProvider.generateTokenDto(user);
      RefreshToken refreshToken = RefreshToken.of(tokenDto.getRefreshToken());
      refreshToken.setUser(user);
      refreshTokenRepository.save(refreshToken);

      return tokenDto;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public TokenDto reissueTokens(TokenDto tokenDto) {
    try {
      if (!tokenProvider.validateToken(tokenDto.getRefreshToken()))
        throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);

      Authentication authentication = tokenProvider.getAuthentication(tokenDto.getAccessToken());
      Long userId = Long.parseLong(authentication.getName());

      RefreshToken refreshToken = refreshTokenRepository.findByUserId(userId)
          .orElseThrow(() -> new GeneralException(ErrorCode.REFRESH_TOKEN_NOT_FOUND));

      if (!refreshToken.getRefreshToken().equals(tokenDto.getRefreshToken()))
        throw new GeneralException(ErrorCode.INVALID_REFRESH_TOKEN);

      refreshTokenRepository.delete(refreshToken);

      Optional<User> user = userRepository.findById(userId);
      TokenDto newTokenDto = tokenProvider.generateTokenDto(user.get());
      RefreshToken newRefreshToken = RefreshToken.of(newTokenDto.getRefreshToken());
      newRefreshToken.setUser(user.get());
      refreshTokenRepository.save(newRefreshToken);

      return newTokenDto;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}