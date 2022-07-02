package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Post;
import kr.finpo.api.domain.PostImg;

import java.time.LocalDateTime;
import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record PostDto(
    Long id,
    String content,
    Boolean anonymity,
    Integer likes,
    Long hits,
    UserDto user,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<ImgDto> imgs
) {

  public PostDto {
  }

  public Post updateEntity(Post post) {
    if (content != null) post.setContent(content);
    if (anonymity != null) post.setAnonymity(anonymity);
    return post;
  }

  public static PostDto response(Post post, List<PostImg> imgs) {
    return new PostDto(
        post.getId(),
        post.getContent(),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getAnonymity() ? null : UserDto.postResponse(post.getUser()),
        post.getCreatedAt(),
        post.getModifiedAt(),
        imgs.stream().map(ImgDto::response).toList()
    );
  }

  public static PostDto previewResponse(Post post) {
    return new PostDto(
        post.getId(),
        post.getContent().substring(0, 10),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getAnonymity() ? null : UserDto.postResponse(post.getUser()),
        post.getCreatedAt(),
        post.getModifiedAt(),
        null
    );
  }
}
