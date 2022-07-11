package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.FcmDto;
import kr.finpo.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/notification")
public class NotificationController {

  private final NotificationService notificationService;

  @GetMapping("/me")
  public DataResponse<Object> getMy() {
    return DataResponse.of(notificationService.getMy());
  }

  @GetMapping("/history/me")
  public DataResponse<Object> getMyHistories(@RequestParam(value = "lastId", required = false) Long lastId, Pageable pageable) {
    return DataResponse.of(notificationService.getMyHistories(lastId, pageable));
  }

  @DeleteMapping("/history/{id}")
  public DataResponse<Object> deleteMyHistory(@PathVariable Long id) {
    return DataResponse.of(notificationService.deleteMyHistory(id));
  }

  @PutMapping("/me")
  public DataResponse<Object> upsertMy(@RequestBody FcmDto body) {
    return DataResponse.of(notificationService.upsertMy(body));
  }
}

