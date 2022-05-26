package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.KakaoTokenDto;
import kr.finpo.api.dto.TokenDto;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URI;
import java.net.URISyntaxException;

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

    KakaoTokenDto kakaoToken = oAuthService.getKakaoAccessToken(code);
    log.debug("카카오 tokens:" + kakaoToken.toString());
    Object loginRes = oAuthService.loginWithKakaoToken(kakaoToken.access_token());


    HttpHeaders headers = new HttpHeaders();
    if (loginRes.getClass() == UserDto.class) { // not registered
      UserDto userDto = (UserDto) loginRes;
      headers.set("KakaoToken", kakaoToken.access_token());
      headers.setLocation(new URI(String.format("%s/register/kakao?%s&kakao-token=%s",url, userDto.toUrlParameter(), kakaoToken.access_token())));
    }
    else {
      TokenDto tokenDto = (TokenDto) loginRes;
      headers.setLocation(new URI(String.format("%s?access-token=%s&refresh-token=%s", url, tokenDto.getAccessToken(), tokenDto.getRefreshToken())));
    }
    return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER); // redirect
  }


  @GetMapping("/login/kakao")
  public DataResponse<Object> loginWithKakaoToken(@RequestHeader("Authorization") String kakaoAccessToken) {

    Object loginRes = oAuthService.loginWithKakaoToken(kakaoAccessToken);
    if (loginRes.getClass() == UserDto.class) // not registered
      return DataResponse.of(loginRes, "need register");
    return DataResponse.of(loginRes);
  }


  @PostMapping("/register/kakao")
  public DataResponse<Object> registerByKakao(
      @RequestHeader("Authorization") String kakaoAccessToken,
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(oAuthService.registerByKakao(kakaoAccessToken, body));
  }


  @PostMapping("/reissue")
  public DataResponse<Object> reissueTokens(@RequestBody TokenDto tokenDto) {
    return DataResponse.of(oAuthService.reissueTokens(tokenDto));
  }

}

