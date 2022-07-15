package kr.finpo.api.controller;

import kr.finpo.api.constant.UserPurpose;
import kr.finpo.api.constant.UserStatus;
import kr.finpo.api.dto.*;
import kr.finpo.api.service.BannedUserService;
import kr.finpo.api.service.BlockedUserService;
import kr.finpo.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;
  private final BannedUserService bannedUserService;
  private final BlockedUserService blockedUserService;

  @GetMapping(path = "/status/name")
  public DataResponse<Object> getAllStatus() {
    return DataResponse.of(Stream.of(UserStatus.values()).map(UserStatusDto::response).toList());
  }

  @GetMapping(path = "/purpose/name")
  public DataResponse<Object> getAllPurpose() {
    return DataResponse.of(Stream.of(UserPurpose.values()).map(UserPurposeDto::response).toList());
  }

  @GetMapping("/{id}")
  public DataResponse<Object> getById(@PathVariable Long id) {
    return DataResponse.of(userService.getById(id));
  }

  @GetMapping("/me")
  public DataResponse<Object> getMy() {
    return DataResponse.of(userService.getMy());
  }

  @GetMapping(path = "/me/purpose")
  public DataResponse<Object> getMyPurpose() {
    return DataResponse.of(userService.getMyPurpose());
  }

  @PutMapping("/me")
  public DataResponse<Object> updateMe(
      @RequestBody UserDto body
  ) {
    return DataResponse.of(userService.updateMe(body));
  }

  @PostMapping("/me/profile-img")
  public DataResponse<Object> updateMyProfileimg(
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(userService.updateMyProfileImg(body));
  }

  @DeleteMapping("/me")
  public Object deleteMe(@RequestBody(required = false) WithdrawDto body) {
    if (userService.deleteMe(body)) return DataResponse.of(true);
    return new ResponseEntity<>(DataResponse.of(true, "OAuth account has already withdrawn"), HttpStatus.ACCEPTED);
  }

  @GetMapping("/check-duplicate")
  public DataResponse<Object> checkNicknameDuplicate(@RequestParam Map<String, String> params) {
    return DataResponse.of(userService.isNicknameDuplicated(params.get("nickname")));
  }

  @GetMapping(value = "/banned/me")
  public DataResponse<Object> getMyBanned() {
    return DataResponse.of(bannedUserService.getMe());
  }

  @GetMapping(value = "/block/me")
  public DataResponse<Object> getMyBlock() {
    return DataResponse.of(blockedUserService.getNonAnonymities());
  }

  @DeleteMapping(value = "/block/{id}")
  public DataResponse<Object> deleteBlock(@PathVariable Long id) {
    return DataResponse.of(blockedUserService.deleteNonAnonymity(id));
  }
}
