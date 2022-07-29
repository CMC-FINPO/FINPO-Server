package kr.finpo.api.controller;

import java.util.Map;
import java.util.stream.Stream;
import kr.finpo.api.constant.UserPurpose;
import kr.finpo.api.constant.UserStatus;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.dto.UserPurposeDto;
import kr.finpo.api.dto.UserStatusDto;
import kr.finpo.api.dto.WithdrawDto;
import kr.finpo.api.service.BannedUserService;
import kr.finpo.api.service.BlockedUserService;
import kr.finpo.api.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

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
    public DataResponse<Object> updateMyProfileImg(
        @ModelAttribute UserDto body
    ) {
        return DataResponse.of(userService.updateMyProfileImg(body));
    }

    @DeleteMapping("/me")
    public Object deleteMe(@RequestBody(required = false) WithdrawDto body) {
        if (userService.deleteMe(body)) {
            return DataResponse.of(true);
        }
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
