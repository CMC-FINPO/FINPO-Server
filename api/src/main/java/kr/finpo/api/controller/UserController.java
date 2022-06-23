package kr.finpo.api.controller;

import kr.finpo.api.constant.UserPurpose;
import kr.finpo.api.constant.UserStatus;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.dto.UserPurposeDto;
import kr.finpo.api.dto.UserStatusDto;
import kr.finpo.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Stream;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;


  @GetMapping("")
  public DataResponse<Object> getAll() {
    return DataResponse.of(userService.getAll());
  }

  @GetMapping(path = "/status/name")
  public DataResponse<Object> getAllStatus() {
    return DataResponse.of(Stream.of(UserStatus.values()).map(e->UserStatusDto.response(e)).toList());
  }

  @GetMapping(path = "/purpose/name")
  public DataResponse<Object> getAllPurpose() {
    return DataResponse.of(Stream.of(UserPurpose.values()).map(e-> UserPurposeDto.response(e)).toList());
  }

  @GetMapping("/{id}")
  public DataResponse<Object> getById(@PathVariable Long id) {
    return DataResponse.of(userService.getById(id));
  }


  @GetMapping("/me")
  public DataResponse<Object> getMyInfo() {
    return DataResponse.of(userService.getMyInfo());
  }


  @PutMapping("/{id}")
  public DataResponse<Object> update(
      @PathVariable Long id,
      @RequestBody UserDto body
  ) {
    return DataResponse.of(userService.update(id, body));
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
  public DataResponse<Object> deleteMe() {
    return DataResponse.of(userService.deleteMe());
  }

  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(@PathVariable Long id) {
    return DataResponse.of(userService.delete(id));
  }


  @GetMapping("/check-duplicate")
  public DataResponse<Object> checkNicknameDuplicate(@RequestParam Map<String, String> params) {
    Boolean res = false;
    if(params.containsKey("nickname"))
      res = userService.isNicknameDuplicated(params.get("nickname"));
    else if(params.containsKey("email"))
      res = userService.isEmailDuplicated(params.get("email"));
    return DataResponse.of(res);
  }
}
