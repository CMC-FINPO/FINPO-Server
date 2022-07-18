package kr.finpo.api.controller;

import kr.finpo.api.domain.Information;
import kr.finpo.api.dto.*;
import kr.finpo.api.repository.InformationRepository;
import kr.finpo.api.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@Slf4j
//@Secured("ROLE_ADMIN")
public class AdminController {

  private final PolicyService policyService;
  private final ReportService reportService;
  private final UserService userService;
  private final BannedUserService bannedUserService;
  private final OAuthService oAuthService;
  private final InformationRepository informationRepository;

  @PostMapping("/admin/information")
  public DataResponse<Object> insert(@RequestBody Information body) {
    return DataResponse.of(informationRepository.save(Information.of(body.getType(), body.getContent(), body.getUrl(), body.getStatus())));
  }

  @PostMapping("/policy")
  public DataResponse<Object> insertCustom(
      @RequestBody List<PolicyDto> body
  ) {
    return DataResponse.of(policyService.insertCustom(body));
  }

  @PutMapping("/policy/{id}")
  public DataResponse<Object> update(
      @PathVariable Long id, @RequestBody PolicyDto body, @RequestParam("sendNotification") Boolean sendNotification
  ) {
    return DataResponse.of(policyService.update(id, body, sendNotification));
  }

  @DeleteMapping("/policy/{id}")
  public DataResponse<Object> deleteCustom(
      @PathVariable Long id
  ) {
    return DataResponse.of(policyService.deleteCustom(id));
  }

  @GetMapping("/report/community")
  public DataResponse<Object> getAllReports(Pageable pageable) {
    return DataResponse.of(reportService.getCommunity(pageable));
  }

  @DeleteMapping("/user/{id}")
  public DataResponse<Object> deleteUser(@PathVariable Long id, @RequestBody(required = false) WithdrawDto body) {
    return DataResponse.of(userService.delete(id, body));
  }

  @GetMapping("/policy/admin")
  public DataResponse<Object> search(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "startDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(value = "endDate", required = false) @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
      @RequestParam(value = "region", required = false) List<Long> regionIds,
      @RequestParam(value = "category", required = false) List<Long> categoryIds,
      Pageable pageable
  ) {
    return DataResponse.of(policyService.search(title, startDate, endDate, regionIds, categoryIds, null, pageable));
  }

  @GetMapping("/user")
  public DataResponse<Object> getAllUser(Pageable pageable) {
    return DataResponse.of(userService.getAll(pageable));
  }

  @GetMapping("/user/banned")
  public DataResponse<Object> getBannedUser(Pageable pageable) {
    return DataResponse.of(bannedUserService.getAll(pageable));
  }

  @GetMapping(value = "/user/banned", params={"userId"})
  public DataResponse<Object> getBannedUserByUserId(@RequestParam("userId") Long userId) {
    return DataResponse.of(bannedUserService.getByUserId(userId));
  }

  @PostMapping("/user/banned")
  public DataResponse<Object> insertBannedUser(@RequestBody BannedUserDto body) {
    return DataResponse.of(bannedUserService.insert(body));
  }

  @PutMapping("/user/banned/{id}")
  public DataResponse<Object> updateBannedUser(@PathVariable Long id, @RequestBody BannedUserDto body, @RequestParam("releaseNow") Boolean releaseNow) {
    return DataResponse.of(bannedUserService.update(id, body, releaseNow));
  }



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



