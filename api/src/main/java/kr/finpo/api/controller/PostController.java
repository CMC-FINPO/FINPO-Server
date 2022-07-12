package kr.finpo.api.controller;

import kr.finpo.api.dto.*;
import kr.finpo.api.service.CommentService;
import kr.finpo.api.service.PostService;
import kr.finpo.api.service.ReportService;
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
  private final CommentService commentService;
  private final ReportService reportService;

  @GetMapping("/{id}")
  public DataResponse<Object> get(@PathVariable Long id) {
    return DataResponse.of(postService.get(id));
  }

  @GetMapping("/me")
  public DataResponse<Object> getMy(Pageable pageable) {
    return DataResponse.of(postService.getMy(pageable));
  }

  @GetMapping("/like/me")
  public DataResponse<Object> getMyLikes(Pageable pageable) {
    return DataResponse.of(postService.getMyLikes(pageable));
  }

  @GetMapping("/bookmark/me")
  public DataResponse<Object> getMyBookmarks(Pageable pageable) {
    return DataResponse.of(postService.getMyBookmarks(pageable));
  }

  @GetMapping("/comment/me")
  public DataResponse<Object> getMyCommentPosts(Pageable pageable) {
    return DataResponse.of(postService.getMyCommentPosts(pageable));
  }

  @GetMapping("/search")
  public DataResponse<Object> search(
      @RequestParam(value = "content", required = false) String content,
      @RequestParam(value = "lastId", required = false) Long lastId,
      Pageable pageable
  ) {
    return DataResponse.of(postService.search(content, lastId, pageable));
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

  @PostMapping("/{id}/bookmark")
  public DataResponse<Object> bookmark(@PathVariable Long id) {
    return DataResponse.of(postService.bookmark(id));
  }

  @DeleteMapping("/{id}/bookmark")
  public DataResponse<Object> deleteBookmark(@PathVariable Long id) {
    return DataResponse.of(postService.deleteBookmark(id));
  }


  @GetMapping("/{id}/comment")
  public DataResponse<Object> getComments(@PathVariable Long id, Pageable pageable) {
    return DataResponse.of(commentService.getByPostId(id, pageable));
  }

  @PostMapping("/{id}/comment")
  public DataResponse<Object> insertComment(@PathVariable Long id, @RequestBody CommentDto body) {
    return DataResponse.of(commentService.insert(id, body));
  }

  @PostMapping("/{id}/report")
  public DataResponse<Object> reportComment(@PathVariable Long id, @RequestBody CommunityReportDto body) {
    return DataResponse.of(reportService.insertPost(id, body));
  }
}
