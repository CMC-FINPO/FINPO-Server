package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.service.CategoryService;
import kr.finpo.api.service.PolicyService;
import kr.finpo.api.service.openapi.YouthcenterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/policy")
public class PolicyController {

  private final PolicyService policyService;


  @GetMapping("/me")
  public DataResponse<Object> getMy(Pageable pageable) {
    return DataResponse.of(policyService.getMy(pageable));
  }
}
