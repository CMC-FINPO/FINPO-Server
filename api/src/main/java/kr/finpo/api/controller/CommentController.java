package kr.finpo.api.controller;

import kr.finpo.api.dto.CommentDto;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.service.CommentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/comment")
public class CommentController {

  private final CommentService commentService;

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
}
