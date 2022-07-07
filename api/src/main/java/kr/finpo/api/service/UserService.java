package kr.finpo.api.service;


import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.*;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
  private final InterestPolicyRepository interestPolicyRepository;
  private final JoinedPolicyRepository joinedPolicyRepository;
  private final KakaoAccountRepository kakaoAccountRepository;
  private final AppleAccountRepository appleAccountRepository;
  private final GoogleAccountRepository googleAccountRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final FcmRepository fcmRepository;
  private final PostRepository postRepository;
  private final LikePostRepository likePostRepository;
  private final CommentRepository commentRepository;
  private final S3Uploader s3Uploader;

  @Value("${oauth.kakao.admin-key}")
  private String kakaoAdminKey;

  @Value("${oauth.apple.team-id}")
  private String appleTeamId;
  @Value("${oauth.apple.client-id}")
  private String appleClientId;
  @Value("${oauth.apple.key-id}")
  private String appleKeyId;
  @Value("${oauth.apple.private-key}")
  private String applePrivateKey;

  public List<UserDto> getAll() {
    try {
      return userRepository.findAll().stream().map(UserDto::response).toList();
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

  public List<Long> getMyPurpose() {
    try {
      return userPurposeRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(UserPurpose::getUserPurposeId).toList();
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
      String profileImgUrl = s3Uploader.uploadFile("profile", dto.profileImgFile());

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

      likePostRepository.deleteByUserId(id);
      commentRepository.findByUserId(id).forEach(comment -> {
        comment.setUser(null);
        commentRepository.save(comment);
      });
      postRepository.findByUserId(id).forEach(post -> {
        post.setUser(null);
        postRepository.save(post);
      });
      userPurposeRepository.deleteByUserId(id);
      interestRegionRepository.deleteByUserId(id);
      interestCategoryRepository.deleteByUserId(id);
      interestPolicyRepository.deleteByUserId(id);
      joinedPolicyRepository.deleteByUserId(id);
      refreshTokenRepository.deleteByUserId(id);
      fcmRepository.deleteByUserId(id);

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
        else if (user.getOAuthType().equals(OAuthType.APPLE)) {
          headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
          params.add("client_id", appleClientId);
          params.add("client_secret", getClientSecret());
          params.add("code", dto.code());
          params.add("grant_type", "authorization_code");

          AppleTokenDto appleTokenDto = new RestTemplate().exchange(
              "https://appleid.apple.com/auth/token",
              HttpMethod.POST,
              new HttpEntity<>(params, headers),
              AppleTokenDto.class
          ).getBody();

          log.debug("애플 토큰 응답" + appleTokenDto);

          params.add("token", appleTokenDto.access_token());
          params.add("token_type_hint", "access_token");

          new RestTemplate().exchange(
              "https://appleid.apple.com/auth/revoke",
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
        appleAccountRepository.deleteByUserId(id);
        userRepository.deleteById(id);
      }
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean isNicknameDuplicated(String nickname) {
    try {
      if (userRepository.findByNickname(nickname).isEmpty())
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
      if (userRepository.findByEmail(email).isEmpty())
        return false;

      if (SecurityUtil.isUserLogin())
        if (email.equals(userRepository.findById(SecurityUtil.getCurrentUserId()).get().getEmail()))
          return false;

      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public String getClientSecret() throws IOException {
    Date expirationDate = Date.from(LocalDateTime.now().plusDays(30).atZone(ZoneId.systemDefault()).toInstant());
    return Jwts.builder()
        .setHeaderParam("kid", appleKeyId)
        .setHeaderParam("alg", "ES256")
        .setIssuer(appleTeamId)
        .setIssuedAt(new Date(System.currentTimeMillis()))
        .setExpiration(expirationDate)
        .setAudience("https://appleid.apple.com")
        .setSubject(appleClientId)
        .signWith(SignatureAlgorithm.ES256, getPrivateKey())
        .compact();
  }

  private PrivateKey getPrivateKey() throws IOException {
    ClassPathResource resource = new ClassPathResource(applePrivateKey);
    String privateKey = new String(resource.getInputStream().readAllBytes());
    Reader pemReader = new StringReader(privateKey);
    PEMParser pemParser = new PEMParser(pemReader);
    JcaPEMKeyConverter converter = new JcaPEMKeyConverter();
    PrivateKeyInfo object = (PrivateKeyInfo) pemParser.readObject();
    return converter.getPrivateKey(object);
  }
}

