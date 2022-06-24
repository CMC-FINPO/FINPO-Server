package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestPolicyDto;
import kr.finpo.api.dto.JoinedPolicyDto;
import kr.finpo.api.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/policy")
public class PolicyController {

  private final PolicyService policyService;

  @GetMapping("/{id}")
  public DataResponse<Object> get(@PathVariable Long id) {
    return DataResponse.of(policyService.get(id));
  }

  @GetMapping("/me")
  public DataResponse<Object> getMy(Pageable pageable) {
    return DataResponse.of(policyService.getMy(pageable));
  }

  @GetMapping("/search")
  public DataResponse<Object> search(
      @RequestParam(value = "title", required = false) String title,
      @RequestParam(value = "startDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate startDate,
      @RequestParam(value = "endDate", required = false)  @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate endDate,
      @RequestParam(value = "region", required = false) List<Long> regionIds,
      @RequestParam(value = "category", required = false) List<Long> categoryIds,
      Pageable pageable
  ) {
    return DataResponse.of(policyService.search(title, startDate, endDate, regionIds, categoryIds, pageable));
  }

  @GetMapping("/interest/me")
  public DataResponse<Object> getMyInterests() {
    return DataResponse.of(policyService.getMyInterests());
  }

  @GetMapping("/joined/me")
  public DataResponse<Object> getMyJoins() {
    return DataResponse.of(policyService.getMyJoins());
  }

  @PostMapping("/interest/me")
  public DataResponse<Object> insertMyInterest(@RequestBody InterestPolicyDto body) {
    return DataResponse.of(policyService.insertMyInterest(body));
  }

  @PostMapping("/joined/me")
  public DataResponse<Object> insertMyJoined(@RequestBody JoinedPolicyDto body) {
    return DataResponse.of(policyService.insertMyJoined(body));
  }

  @DeleteMapping("/interest/me")
  public DataResponse<Object> deleteMyInterest(@RequestParam(name="id") Long id) {
    return DataResponse.of(policyService.deleteMyInterest(id));
  }

  @DeleteMapping("/joined/me")
  public DataResponse<Object> deleteMyJoined(@RequestParam(name="id") Long id) {
    return DataResponse.of(policyService.deleteMyJoined(id));
  }
}
