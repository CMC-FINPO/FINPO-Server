package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Post;
import kr.finpo.api.domain.PostImg;
import kr.finpo.api.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostDto(
    Boolean status,
    Long id,
    String content,
    Boolean anonymity,
    Integer likes,
    Long hits,
    Integer countOfComment,
    UserDto user,
    Boolean isUserWithdraw,
    Boolean isMine,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<ImgDto> imgs
) {
  public Post updateEntity(Post post) {
    if (content != null) post.setContent(content);
    return post;
  }

  public static PostDto response(Post post, List<PostImg> imgs) {
    return new PostDto(
        post.getStatus(),
        post.getId(),
        post.getContent(),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getCountOfComment(),
        post.getAnonymity() ? null : Optional.ofNullable(post.getUser()).map(UserDto::communityResponse).orElse(null),
        post.getUser() == null ? true : null,
        Optional.ofNullable(post.getUser()).map(val -> val.getId().equals(SecurityUtil.getCurrentUserId())).orElse(null),
        post.getCreatedAt(),
        post.getModifiedAt(),
        imgs.stream().map(ImgDto::response).toList()
    );
  }

  public static PostDto previewResponse(Post post) {
    return new PostDto(
        post.getStatus(),
        post.getId(),
        post.getContent().substring(0, 10),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getCountOfComment(),
        post.getAnonymity() ? null : Optional.ofNullable(post.getUser()).map(UserDto::communityResponse).orElse(null),
        post.getUser() == null ? true : null,
        Optional.ofNullable(post.getUser()).map(val -> val.getId().equals(SecurityUtil.getCurrentUserId())).orElse(null),
        post.getCreatedAt(),
        post.getModifiedAt(),
        null
    );
  }
}
