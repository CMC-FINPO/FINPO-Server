package kr.finpo.api.controller;

import kr.finpo.api.dto.CommentDto;
import kr.finpo.api.dto.CommunityReportDto;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.service.CommentService;
import kr.finpo.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/comment")
public class CommentController {

  private final CommentService commentService;
  private final ReportService reportService;

  @PutMapping("/{id}")
  public DataResponse<Object> update(
      @PathVariable Long id, @RequestBody CommentDto body
  ) {
    return DataResponse.of(commentService.update(id, body));
  }

  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(@PathVariable Long id) {
    return DataResponse.of(commentService.delete(id));
  }

  @PostMapping("/{id}/report")
  public DataResponse<Object> reportComment(@PathVariable Long id, @RequestBody CommunityReportDto body) {
    return DataResponse.of(reportService.insertComment(id, body));
  }
}
