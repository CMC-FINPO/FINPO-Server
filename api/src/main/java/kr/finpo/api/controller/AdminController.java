package kr.finpo.api.controller;

import kr.finpo.api.dto.*;
import kr.finpo.api.service.OAuthService;
import kr.finpo.api.service.PolicyService;
import kr.finpo.api.service.ReportService;
import kr.finpo.api.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@RestController
@RequestMapping
@Slf4j
//@Secured("ROLE_ADMIN")
public class AdminController {

  private final PolicyService policyService;
  private final ReportService reportService;
  private final UserService userService;

  @PostMapping("/policy")
  public DataResponse<Object> insertCustom(
      @RequestBody List<PolicyDto> body
  ) {
    return DataResponse.of(policyService.insertCustom(body));
  }

  @PutMapping("/policy/{id}")
  public DataResponse<Object> update(
      @PathVariable Long id, @RequestBody PolicyDto body
  ) {
    return DataResponse.of(policyService.update(id, body));
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
  public DataResponse<Object> deleteUser(@PathVariable Long id, @RequestBody(required=false) WithdrawDto body) {
    return DataResponse.of(userService.delete(id, body));
  }
}



