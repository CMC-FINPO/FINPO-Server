package kr.finpo.api.controller;

import kr.finpo.api.constant.OAuthType;
import kr.finpo.api.dto.*;
import kr.finpo.api.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

  private final OAuthService oAuthService;

  @GetMapping(path = "/login/kakao", params = "code")
  public ResponseEntity<Object> loginWithKakaoId(
      @RequestParam String code,
      @Value("${admin-page.url}") String url
  ) throws URISyntaxException {

    KakaoTokenDto kakaoToken = oAuthService.getKakaoToken(code);
    log.debug("카카오 tokens:" + kakaoToken.toString());
    Object loginRes = oAuthService.loginWithOAuthToken(kakaoToken.access_token(), "kakao");

    HttpHeaders headers = new HttpHeaders();
    if (loginRes.getClass() == UserDto.class) { // not registered
      UserDto userDto = (UserDto) loginRes;
      headers.setLocation(new URI(String.format("%s/register/kakao?%s&token=%s", url, userDto.toUrlParameter(), kakaoToken.access_token())));
    } else {
      TokenDto tokenDto = (TokenDto) loginRes;
      headers.setLocation(new URI(String.format("%s?access-token=%s&refresh-token=%s", url, tokenDto.getAccessToken(), tokenDto.getRefreshToken())));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }


  @GetMapping(path = "/googleloginurl")
  public ResponseEntity<Object> temp(
      @Value("${oauth.google.redirect-uri}") String googleRedirectUrl,
      @Value("${oauth.google.client-id}") String googleClientId,
      @Value("${oauth.google.auth-scope}") String googleScopes
  ) throws URISyntaxException {
    HttpHeaders headers = new HttpHeaders();
    Map<String, String> params = new HashMap<>();
    params.put("client_id", googleClientId);
    params.put("redirect_uri", googleRedirectUrl);
    params.put("scope", googleScopes.replaceAll(",", "%20"));
    String paramStr = params.entrySet().stream().map(param -> param.getKey() + "=" + param.getValue()).collect(Collectors.joining("&"));
    headers.setLocation(new URI("https://accounts.google.com/o/oauth2/v2/auth?" + paramStr + "&response_type=code"));
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }

  @GetMapping(path = "/login/google", params = "code")
  public ResponseEntity<Object> loginWithGoogleId(
      @RequestParam String code,
      @Value("${admin-page.url}") String url
  ) throws URISyntaxException {
    GoogleTokenDto googleTokenDto = oAuthService.getGoogleToken(code);
    log.debug("구글 tokens:" + googleTokenDto.toString());
    Object loginRes = oAuthService.loginWithOAuthToken(googleTokenDto.access_token(), "google");

    HttpHeaders headers = new HttpHeaders();
    if (loginRes.getClass() == UserDto.class) { // not registered
      UserDto userDto = (UserDto) loginRes;
      headers.setLocation(new URI(String.format("%s/register/google?%s&token=%s", url, userDto.toUrlParameter(), googleTokenDto.access_token())));
    } else {
      TokenDto tokenDto = (TokenDto) loginRes;
      headers.setLocation(new URI(String.format("%s?access-token=%s&refresh-token=%s", url, tokenDto.getAccessToken(), tokenDto.getRefreshToken())));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }


  @GetMapping("/login/{oAuthType}")
  public Object loginWithOAuthToken(@RequestHeader("Authorization") String kakaoAccessToken, @PathVariable String oAuthType) {

    Object loginRes = oAuthService.loginWithOAuthToken(kakaoAccessToken, oAuthType);
    if (loginRes.getClass() == UserDto.class) // not registered
      return new ResponseEntity<>(DataResponse.of(loginRes, "need register"), HttpStatus.ACCEPTED);
    return DataResponse.of(loginRes);
  }


  @PostMapping("/register/test")
  public DataResponse<Object> registerTest(
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(oAuthService.register(null, "test", body));
  }


  @PostMapping("/register/kakao")
  public DataResponse<Object> registerWithKakao(
      @RequestHeader("Authorization") String oAuthAccessToken,
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(oAuthService.register(oAuthAccessToken, "kakao", body));
  }

  @PostMapping("/register/google")
  public DataResponse<Object> registerWithGoogle(
      @RequestHeader("Authorization") String oAuthAccessToken,
      @ModelAttribute UserDto body
      ) {
    return DataResponse.of(oAuthService.register(oAuthAccessToken, "google", body));
  }

  @PostMapping("/register/apple")
  public DataResponse<Object> registerWithApple(
      @RequestHeader("Authorization") String oAuthAccessToken,
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(oAuthService.register(oAuthAccessToken, "apple", body));
  }


  @PostMapping("/reissue")
  public DataResponse<Object> reissueTokens(@RequestBody TokenDto tokenDto) {
    return DataResponse.of(oAuthService.reissueTokens(tokenDto));
  }

}

