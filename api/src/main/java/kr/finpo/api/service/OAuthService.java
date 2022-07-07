package kr.finpo.api.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Base64Utils;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@Transactional
@Service
public class OAuthService {

  public final CategoryService categoryService;
  private final TokenProvider tokenProvider;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final GoogleAccountRepository googleAccountRepository;
  private final AppleAccountRepository appleAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final InterestRegionRepository interestRegionRepository;
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

  private String getAppleAccount(String identityToken) {
    try {
      identityToken = identityToken.replace("Bearer ", "");

      ApplePublicKeyDto applePublicKey = new RestTemplate().exchange(
          "https://appleid.apple.com/auth/keys",
          HttpMethod.GET,
          new HttpEntity<>(null, null),
          ApplePublicKeyDto.class
      ).getBody();

      String headerOfIdentityToken = identityToken.substring(0, identityToken.indexOf("."));

      Map<String, String> header = new ObjectMapper().readValue(new String(Base64Utils.decodeFromUrlSafeString(headerOfIdentityToken), "UTF-8"), Map.class);
      ApplePublicKeyDto.Key key = applePublicKey.getMatchedKeyBy(header.get("kid"), header.get("alg"))
          .orElseThrow(() -> new NullPointerException("Failed get public key from apple's id server."));

      byte[] nBytes = Base64.getUrlDecoder().decode(key.n());
      byte[] eBytes = Base64.getUrlDecoder().decode(key.e());

      BigInteger n = new BigInteger(1, nBytes);
      BigInteger e = new BigInteger(1, eBytes);

      RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(n, e);
      KeyFactory keyFactory = KeyFactory.getInstance(key.kty());
      PublicKey publicKey = keyFactory.generatePublic(publicKeySpec);

      Claims userInfo = Jwts.parser().setSigningKey(publicKey).parseClaimsJws(identityToken).getBody();
      return userInfo.get("sub", String.class);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.APPlE_IDENTITY_TOKEN_ERROR, e);
    }
  }


  public Object loginWithOAuthToken(String oAuthAccessToken, String oAuthType) {
    try {
      Optional<User> user = null;

      if (oAuthType.equals("kakao")) {
        KakaoAccountDto kakaoAccount = getKakaoAccount(oAuthAccessToken);
        user = userRepository.findByKakaoAccountId(kakaoAccount.id());
        if (user.isEmpty()) return kakaoAccount.toUserDto();
      }
      else if (oAuthType.equals("google")) {
        GoogleAccountDto googleAccount = getGoogleAccount(oAuthAccessToken).of();
        user = userRepository.findByGoogleAccountId(googleAccount.id());
        if (user.isEmpty()) return googleAccount.toUserDto();
      }
      else if (oAuthType.equals("apple")) {
        String appleId = getAppleAccount(oAuthAccessToken);
        user = userRepository.findByAppleAccountId(appleId);
        if (user.isEmpty()) return UserDto.appleUserDto();
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
      }
      else if (oAuthType.equals("google")) {
        oAuthAccountId = getGoogleAccount(oAuthAccessToken).of().id();
        googleAccountRepository.findById(oAuthAccountId).ifPresent(s -> {
          throw new GeneralException(ErrorCode.USER_ALREADY_REGISTERED);
        });
      }
      else if (oAuthType.equals("apple")) {
        oAuthAccountId = getAppleAccount(oAuthAccessToken);
        appleAccountRepository.findById(oAuthAccountId).ifPresent(s -> {
          throw new GeneralException(ErrorCode.USER_ALREADY_REGISTERED);
        });
      }

      // nickname duplication check
      userRepository.findByNickname(dto.nickname()).ifPresent(e -> {
        throw new GeneralException(ErrorCode.VALIDATION_ERROR, "nickname duplicated");
      });


      if (StringUtils.hasText(dto.email())) {
        // email duplication check
        userRepository.findByEmail(dto.email()).ifPresent(e -> {
          throw new GeneralException(ErrorCode.VALIDATION_ERROR, "email duplicated");
        });

        // email format check
        if (!dto.email().matches("^(.+)@(\\S+)$"))
          throw new GeneralException(ErrorCode.VALIDATION_ERROR, "email format error");
      }

      // region check
      String profileImgUrl = dto.profileImg();
      if (dto.profileImgFile() != null)
        profileImgUrl = s3Uploader.uploadFile("profile", dto.profileImgFile());

      Region region = regionRepository.findById(dto.regionId()).orElseThrow(
          () -> new GeneralException(ErrorCode.BAD_REQUEST, "region id not valid")
      );
      User user = dto.toEntity();
      user.setProfileImg(profileImgUrl);
      user.setOAuthType(oAuthType.equals("kakao") ? OAuthType.KAKAO : oAuthType.equals("google") ? OAuthType.GOOGLE : oAuthType.equals("apple") ? OAuthType.APPLE :OAuthType.TEST );

      // 기본 지역 설정
      InterestRegion defaultRegion = InterestRegion.of(null, region, true);
      defaultRegion = interestRegionRepository.save(defaultRegion);
      user.setDefaultRegion(defaultRegion);
      user = userRepository.save(user);

      defaultRegion.setUser(user);
      interestRegionRepository.save(defaultRegion);

      // 관심카테고리 설정
      if (dto.categories() != null) {
        List<InterestCategoryDto> categories = new ObjectMapper().readValue(dto.categories(), new TypeReference<>() {
        });
        categoryService.insertInterests(categories, user);
      }


      if (oAuthType.equals("kakao")) {
        KakaoAccount kakaoAccount = kakaoAccountRepository.save(KakaoAccount.of(oAuthAccountId));
        kakaoAccount.setUser(user);
      }
      else if (oAuthType.equals("google")) {
        GoogleAccount googleAccount = googleAccountRepository.save(GoogleAccount.of(oAuthAccountId));
        googleAccount.setUser(user);
      }
      else if (oAuthType.equals("apple")) {
        AppleAccount appleAccount = appleAccountRepository.save(AppleAccount.of(oAuthAccountId));
        appleAccount.setUser(user);
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