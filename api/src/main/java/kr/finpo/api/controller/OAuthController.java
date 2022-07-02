package kr.finpo.api.controller;

import kr.finpo.api.dto.*;
import kr.finpo.api.service.OAuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/oauth")
@Slf4j
public class OAuthController {

  private final OAuthService oAuthService;

  @GetMapping("/login/{oAuthType}")
  public Object loginWithOAuthToken(@RequestHeader("Authorization") String kakaoAccessToken, @PathVariable String oAuthType) {
    Object loginRes = oAuthService.loginWithOAuthToken(kakaoAccessToken, oAuthType);
    if (loginRes.getClass() == UserDto.class) // not registered
      return new ResponseEntity<>(DataResponse.of(loginRes, "need register"), HttpStatus.ACCEPTED);
    return DataResponse.of(loginRes);
  }


  @PostMapping("/register/{oAuth}")
  public DataResponse<Object> registerWithKakao(
      @RequestHeader("Authorization") String oAuthAccessToken,
      @ModelAttribute UserDto body,
      @PathVariable String oAuth
  ) {
    return DataResponse.of(oAuthService.register(oAuthAccessToken, oAuth, body));
  }


  @PostMapping("/reissue")
  public DataResponse<Object> reissueTokens(@RequestBody TokenDto tokenDto) {
    return DataResponse.of(oAuthService.reissueTokens(tokenDto));
  }
}

