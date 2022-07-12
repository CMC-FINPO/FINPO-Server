package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.Constraint;
import kr.finpo.api.domain.Post;
import kr.finpo.api.domain.PostImg;
import kr.finpo.api.repository.BookmarkPostRepository;
import kr.finpo.api.repository.LikePostRepository;
import kr.finpo.api.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.springframework.util.ObjectUtils.isEmpty;

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
    Boolean isLiked,
    Boolean isBookmarked,
    Boolean isModified,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    List<ImgDto> imgs
) {
  public Post updateEntity(Post post) {
    if (content != null) {
      post.setContent(content);
      post.setIsModified(true);
    }
    return post;
  }


  public static PostDto response(Post post, List<PostImg> imgs, LikePostRepository likePostRepository, BookmarkPostRepository bookmarkPostRepository) {
    Boolean isLiked = !isEmpty(likePostRepository) && likePostRepository.findOneByUserIdAndPostId(SecurityUtil.getCurrentUserId(), post.getId()).isPresent();
    Boolean isBookmarked = !isEmpty(bookmarkPostRepository) && bookmarkPostRepository.findOneByUserIdAndPostId(SecurityUtil.getCurrentUserId(), post.getId()).isPresent();

    return new PostDto(
        post.getStatus(),
        post.getId(),
        post.getContent(),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getCountOfComment(),
        post.getAnonymity() ? null : Optional.ofNullable(post.getUser()).map(UserDto::communityResponse).orElse(null),
        isEmpty(post.getUser()) ? true : null,
        Optional.ofNullable(post.getUser()).map(val -> val.getId().equals(SecurityUtil.getCurrentUserId())).orElse(null),
        isLiked,
        isBookmarked,
        post.getIsModified(),
        post.getCreatedAt(),
        post.getModifiedAt(),
        isEmpty(imgs) ? null : imgs.stream().map(ImgDto::response).toList()
    );
  }

  public static PostDto previewResponse(Post post) {
    return previewResponse(post, null, null);
  }

  public static PostDto previewResponse(Post post, LikePostRepository likePostRepository, BookmarkPostRepository bookmarkPostRepository) {
    Boolean isLiked = likePostRepository == null ? null : likePostRepository.findOneByUserIdAndPostId(SecurityUtil.getCurrentUserId(), post.getId()).isPresent();
    Boolean isBookmarked = bookmarkPostRepository == null ? null : bookmarkPostRepository.findOneByUserIdAndPostId(SecurityUtil.getCurrentUserId(), post.getId()).isPresent();

    return new PostDto(
        post.getStatus(),
        post.getId(),
        post.getContent().substring(0, Math.min(Constraint.CONTENT_PREVIEW_MAX_LENGTH, post.getContent().length())) + (post.getContent().length() > Constraint.CONTENT_PREVIEW_MAX_LENGTH ? "..." : ""),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getCountOfComment(),
        post.getAnonymity() ? null : Optional.ofNullable(post.getUser()).map(UserDto::communityResponse).orElse(null),
        isEmpty(post.getUser()) ? true : null,
        Optional.ofNullable(post.getUser()).map(val -> val.getId().equals(SecurityUtil.getCurrentUserId())).orElse(null),
        isLiked,
        isBookmarked,
        post.getIsModified(),
        post.getCreatedAt(),
        post.getModifiedAt(),
        null
    );
  }

  public static PostDto adminResponse(Post post) {
    return new PostDto(
        post.getStatus(),
        post.getId(),
        post.getContent(),
        post.getAnonymity(),
        post.getLikes(),
        post.getHits(),
        post.getCountOfComment(),
        Optional.ofNullable(post.getUser()).map(UserDto::communityResponse).orElse(null),
        isEmpty(post.getUser()) ? true : null,
        Optional.ofNullable(post.getUser()).map(val -> val.getId().equals(SecurityUtil.getCurrentUserId())).orElse(null),
        null,
        null,
        post.getIsModified(),
        post.getCreatedAt(),
        post.getModifiedAt(),
        null
    );
  }
}
