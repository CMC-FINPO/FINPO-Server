package kr.finpo.api.controller;

import kr.finpo.api.dto.*;
import kr.finpo.api.service.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping
@Slf4j
//@Secured("ROLE_ADMIN")
public class AdminController {

  private final PolicyService policyService;
  private final ReportService reportService;
  private final UserService userService;
  private final BannedUserService bannedUserService;

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
}



