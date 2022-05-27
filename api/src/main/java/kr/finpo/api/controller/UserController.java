package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RequiredArgsConstructor
@RestController
@RequestMapping("/user")
public class UserController {

  private final UserService userService;


  @GetMapping("")
  public DataResponse<Object> getAll() {
    return DataResponse.of(userService.getAll());
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
  public DataResponse<Object> update(
      @RequestBody UserDto body,
      @RequestParam("before") String before
  ) {
    return DataResponse.of(userService.updateMe(body, before));
  }

  @PutMapping("/me/profile-img")
  public DataResponse<Object> updateMyProfileimg(
      @ModelAttribute UserDto body
  ) {
    return DataResponse.of(userService.updateMyProfileImg(body));
  }

  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(@PathVariable Long id) {
    return DataResponse.of(userService.delete(id));
  }


  @GetMapping("/check-duplicate")
  public DataResponse<Object> checkDuplicate(@RequestParam("nickname") String nickname) {
    return DataResponse.of(userService.checkDuplicate(nickname));
  }
}
