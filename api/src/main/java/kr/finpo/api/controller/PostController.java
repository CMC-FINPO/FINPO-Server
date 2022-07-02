package kr.finpo.api.controller;

import kr.finpo.api.dto.*;
import kr.finpo.api.service.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@Slf4j
@RequestMapping("/post")
public class PostController {

  private final PostService postService;

  @GetMapping("/{id}")
  public DataResponse<Object> get(@PathVariable Long id) {
    return DataResponse.of(postService.get(id));
  }

  @GetMapping("/me")
  public DataResponse<Object> getMy(Pageable pageable) {
    return DataResponse.of(postService.getMy(pageable));
  }

  @GetMapping("/search")
  public DataResponse<Object> search(
      @RequestParam(value = "content", required = false) String content,
      Pageable pageable
  ) {
    return DataResponse.of(postService.search(content, pageable));
  }

  @PostMapping
  public DataResponse<Object> insert(
      @RequestBody PostDto body
  ) {
    return DataResponse.of(postService.insert(body));
  }

  @PutMapping("/{id}")
  public DataResponse<Object> update(
      @RequestBody PostDto body, @PathVariable Long id
  ) {
    return DataResponse.of(postService.update(body, id));
  }

  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(
      @PathVariable Long id
  ) {
    return DataResponse.of(postService.delete(id));
  }

  @PostMapping("/{id}/like")
  public DataResponse<Object> like(@PathVariable Long id) {
    return DataResponse.of(postService.like(id));
  }

  @DeleteMapping("/{id}/like")
  public DataResponse<Object> deleteLike(@PathVariable Long id) {
    return DataResponse.of(postService.deleteLike(id));
  }
}
