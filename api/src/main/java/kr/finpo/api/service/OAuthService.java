package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.*;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.jwt.TokenProvider;
import kr.finpo.api.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Service
public class OAuthService {

  private final TokenProvider tokenProvider;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final GoogleAccountRepository googleAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final RegionRepository regionRepository;
  private final S3Uploader s3Uploader;


  @Value("${oauth.kakao.rest-api-key}")
  private String kakaoApiKey;
  @Value("${oauth.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  @Value("${oauth.google.auth-url}")
  private String googleAuthUrl;
  @Value("${oauth.google.redirect-uri}")
  private String googleRedirectUrl;
  @Value("${oauth.google.client-id}")
  private String googleClientId;
  @Value("${oauth.google.secret}")
  private String googleSecret;
  @Value("${oauth.google.auth-scope}")
  private String googleScopes;

  @Value("${upload.url}")
  private String uploadUrl;


  public KakaoTokenDto getKakaoToken(String code) {
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

  public GoogleTokenDto getGoogleToken(String code) {
    try {
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
      params.add("grant_type", "authorization_code");
      params.add("client_id", googleClientId);
      params.add("client_secret", googleSecret);
      params.add("code", code);
      params.add("redirect_uri", googleRedirectUrl);

      ResponseEntity<GoogleTokenDto> response = new RestTemplate().exchange(
          "https://oauth2.googleapis.com/token",
          HttpMethod.POST,
          new HttpEntity<>(params, headers),
          GoogleTokenDto.class
      );

      return response.getBody();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  private KakaoAccountDto getKakaoAccount(String accessToken) {
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

  private GoogleAccountDto getGoogleAccount(String accessToken) {
    try {
      if (accessToken.indexOf("Bearer ") != 0) accessToken = "Bearer " + accessToken;

      HttpHeaders headers = new HttpHeaders();
      headers.set("Authorization", accessToken);
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      ResponseEntity<GoogleAccountDto> response = new RestTemplate().exchange(
          "https://people.googleapis.com/v1/people/me?personFields=birthdays,genders,names,emailAddresses,photos",
          HttpMethod.GET,
          new HttpEntity<>(null, headers),
          GoogleAccountDto.class
      );

      return response.getBody();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.KAKAO_SERVER_ERROR, e);
    }
  }


  public Object loginWithOAuthToken(String accessToken, String oAuthType) {
    try {
      Optional<User> user = null;

      if (oAuthType.equals("kakao")) {
        KakaoAccountDto kakaoAccount = getKakaoAccount(accessToken);
        user = userRepository.findByKakaoAccountId(kakaoAccount.id());
        if (user.isEmpty()) return kakaoAccount.toUserDto();
      } else if (oAuthType.equals("google")) {
        GoogleAccountDto googleAccount = getGoogleAccount(accessToken).of();
        user = userRepository.findByGoogleAccountId(googleAccount.id());
        if (user.isEmpty()) return googleAccount.toUserDto();
      }

      TokenDto tokenDto = tokenProvider.generateTokenDto(user.get());

      refreshTokenRepository.findOneByUserId(user.get().getId())
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


  public TokenDto register(String oAuthAccessToken, String oAuthType, UserDto dto) {
    try {

      String oAuthAccountId = null;

      if (oAuthType.equals("kakao")) {
        oAuthAccountId = getKakaoAccount(oAuthAccessToken).id();
        kakaoAccountRepository.findById(oAuthAccountId).ifPresent(s -> {
          throw new GeneralException(ErrorCode.USER_ALREADY_REGISTERED);
        });
      } else if (oAuthType.equals("google")) {
        oAuthAccountId = getGoogleAccount(oAuthAccessToken).of().id();
        googleAccountRepository.findById(oAuthAccountId).ifPresent(s -> {
          throw new GeneralException(ErrorCode.USER_ALREADY_REGISTERED);
        });
      }

      // nickname duplication check
      userRepository.findByNickname(dto.nickname()).ifPresent(e -> {
        throw new GeneralException(ErrorCode.NICKNAME_DUPLICATED);
      });

      String profileImgUrl = dto.profileImg();
      if (dto.profileImgFile() != null)
        profileImgUrl = uploadUrl + s3Uploader.uploadFile("profile", dto.profileImgFile());

      Region defaultRegion = Region.of(dto.region1(), dto.region2(), true);
      defaultRegion = regionRepository.save(defaultRegion);
      User user = dto.toEntity();
      user.setProfileImg(profileImgUrl);
      user.setOAuthType(oAuthType.equals("kakao")? OAuthType.KAKAO : oAuthType.equals("google")? OAuthType.GOOGLE : OAuthType.APPLE);
      user.setDefaultRegion(defaultRegion);
      user = userRepository.save(user);

      defaultRegion.setUser(user);
      regionRepository.save(defaultRegion);

      if (oAuthType.equals("kakao")) {
        KakaoAccount kakaoAccount = kakaoAccountRepository.save(KakaoAccount.of(oAuthAccountId));
        kakaoAccount.setUser(user);
      } else if (oAuthType.equals("google")) {
        GoogleAccount googleAccount = googleAccountRepository.save(GoogleAccount.of(oAuthAccountId));
        googleAccount.setUser(user);
      }

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

      RefreshToken refreshToken = refreshTokenRepository.findOneByUserId(userId)
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