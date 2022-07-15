package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/report")
public class ReportController {

  private final ReportService reportService;

  @GetMapping(path = "/reason")
  public DataResponse<Object> getReasons() {
    return DataResponse.of(reportService.getAll());
  }
}
