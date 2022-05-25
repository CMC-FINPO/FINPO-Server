package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.KakaoTokenDto;
import kr.finpo.api.dto.TokenDto;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.service.OAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;

@RequiredArgsConstructor
@RestController
@RequestMapping("/oauth")
public class OAuthController {

  private final OAuthService oAuthService;

  @GetMapping(path = "/login/kakao", params = "code")
  public ResponseEntity<Object> loginWithKakaoId(
      @RequestParam String code,
      @Value("${admin-page.url}") String url
  ) throws URISyntaxException {

    KakaoTokenDto kakaoToken = oAuthService.getKakaoAccessToken(code);
    Object loginRes = oAuthService.loginWithKakaoToken(kakaoToken.access_token());

    HttpHeaders headers = new HttpHeaders();
    if (loginRes.getClass() == UserDto.class) // not registered
      headers.setLocation(new URI(url + "/register"));
    else {
      headers.setLocation(new URI(url));
      headers.set("Authorization", "Bearer " + loginRes.toString());
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
      @RequestBody UserDto body
  ) {
    return DataResponse.of(oAuthService.registerByKakao(kakaoAccessToken, body));
  }


  @GetMapping("/reissue")
  public DataResponse<Object> reissueTokens(@RequestBody TokenDto tokenDto) {
    return DataResponse.of(oAuthService.reissueTokens(tokenDto));
  }

}

