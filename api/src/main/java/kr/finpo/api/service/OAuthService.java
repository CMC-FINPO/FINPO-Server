package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.Gender;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.KakaoAccount;
import kr.finpo.api.domain.RefreshToken;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.KakaoTokenDto;
import kr.finpo.api.dto.KakaoAccountDto;
import kr.finpo.api.dto.TokenDto;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.jwt.TokenProvider;
import kr.finpo.api.repository.KakaoAccountRepository;
import kr.finpo.api.repository.RefreshTokenRepository;
import kr.finpo.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OAuthService {

  private final TokenProvider tokenProvider;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;


  @Value("${oauth.kakao.rest-api-key}")
  private String kakaoApiKey;
  @Value("${oauth.kakao.redirect-uri}")
  private String kakaoRedirectUri;

  private final static Logger logger = LoggerFactory.getLogger(UserService.class);


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

      logger.debug(response.getBody().toString());

      return response.getBody();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.KAKAO_SERVER_ERROR, e);
    }
  }


  public KakaoAccountDto getKakaoAccount(String accessToken) {
    try {
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


  public Object loginWithKakaoToken(String accessToken) {
    try {
      KakaoAccountDto kakaoAccount = getKakaoAccount(accessToken);

      Optional<User> user = userRepository.findByKakaoAccountId(kakaoAccount.id());
      if (user.isEmpty())
        return new UserDto(
            null,
            kakaoAccount.name(),
            null,
            kakaoAccount.gender().equals("male") ? Gender.MALE : kakaoAccount.gender().equals("female") ? Gender.FEMALE : null,
            kakaoAccount.email(),
            null
        );

      TokenDto tokenDto = tokenProvider.generateTokenDto(user.get());

      RefreshToken refreshToken = refreshTokenRepository.findByUserId(user.get().getId()).get();
      refreshToken.setRefreshToken(tokenDto.getRefreshToken());
      refreshTokenRepository.save(refreshToken);

      return tokenDto;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.INTERNAL_ERROR, e);
    }
  }


  public TokenDto registerByKakao(String kakaoAccessToken, UserDto dto) {
    try {
      String kakaoAccountId = getKakaoAccount(kakaoAccessToken).id();
      KakaoAccount kakaoAccount = kakaoAccountRepository.save(KakaoAccount.of(kakaoAccountId));

      User user = dto.toEntity();
      user.setOAuthType(OAuthType.KAKAO);
      user.setKakaoAccount(kakaoAccount);
      user = userRepository.save(user);

      TokenDto tokenDto = tokenProvider.generateTokenDto(user);
      RefreshToken refreshToken = RefreshToken.of(tokenDto.getRefreshToken());
      user.setRefreshToken(refreshToken);
      userRepository.save(user);

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

      User user = userRepository.findById(userId).get();
      tokenDto = tokenProvider.generateTokenDto(user);
      refreshToken.setRefreshToken(tokenDto.getRefreshToken());
      refreshTokenRepository.save(refreshToken);

      return tokenDto;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}