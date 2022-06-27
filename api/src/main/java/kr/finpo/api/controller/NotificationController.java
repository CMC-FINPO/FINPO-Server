package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.NotificationDto;
import kr.finpo.api.service.CategoryService;
import kr.finpo.api.service.NotificationService;
import kr.finpo.api.service.PolicyService;
import kr.finpo.api.service.openapi.YouthcenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping("/me")
  public DataResponse<Object> getMy() {
    return DataResponse.of(notificationService.getMy());
  }

  @PutMapping("/me")
  public DataResponse<Object> upsertMy(@RequestBody NotificationDto body) {
    return DataResponse.of(notificationService.upsertMy(body));
  }
}

