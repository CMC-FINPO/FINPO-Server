package kr.finpo.api.controller;

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
@RequestMapping
@Slf4j
public class TestController {

  private final OAuthService oAuthService;

  @PostMapping("/oauth/register/test")
  public DataResponse<Object> registerTest(
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(oAuthService.register(null, "test", body));
  }

  @GetMapping(path = "/oauth/login/kakao", params = "code")
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
    }
    else {
      TokenDto tokenDto = (TokenDto) loginRes;
      headers.setLocation(new URI(String.format("%s?access-token=%s&refresh-token=%s", url, tokenDto.getAccessToken(), tokenDto.getRefreshToken())));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }

  @GetMapping(path = "/oauth/googleloginurl")
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

  @GetMapping(path = "/oauth/login/google", params = "code")
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
    }
    else {
      TokenDto tokenDto = (TokenDto) loginRes;
      headers.setLocation(new URI(String.format("%s?access-token=%s&refresh-token=%s", url, tokenDto.getAccessToken(), tokenDto.getRefreshToken())));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }

  @PostMapping(path = "/oauth/login/apple")
  public ResponseEntity<Object> loginWithAppleId(
      AppleAuthDto appleAuthDto,
      @Value("${admin-page.url}") String url
  ) throws URISyntaxException {
    log.debug("애플 인증: " + appleAuthDto.toString());
    Object loginRes = oAuthService.loginWithOAuthToken(appleAuthDto.id_token(), "apple");

    log.debug("loginRes: " + loginRes);
    HttpHeaders headers = new HttpHeaders();
    if (loginRes.getClass() == UserDto.class) { // not registered
      UserDto userDto = (UserDto) loginRes;

      headers.setLocation(new URI(String.format("%s/register/apple?%s&token=%s", url, userDto.toUrlParameter(), appleAuthDto.id_token())));
    }
    else {
      TokenDto tokenDto = (TokenDto) loginRes;
      headers.setLocation(new URI(String.format("%s?access-token=%s&refresh-token=%s", url, tokenDto.getAccessToken(), tokenDto.getRefreshToken())));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }
}



