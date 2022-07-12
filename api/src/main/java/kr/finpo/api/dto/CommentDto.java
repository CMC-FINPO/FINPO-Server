package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Comment;
import kr.finpo.api.domain.User;
import kr.finpo.api.util.SecurityUtil;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommentDto(
    Boolean status,
    Long id,
    CommentDto parent,
    String content,
    Boolean anonymity,
    UserDto user,
    Boolean isUserWithdraw,
    Integer anonymityId,
    Boolean isWriter,
    Boolean isMine,
    Boolean isModified,
    LocalDateTime createdAt,
    LocalDateTime modifiedAt,
    PostDto post,
    List<CommentDto> childs
) {
  public Comment updateEntity(Comment comment) {
    if (content != null) {
      comment.setContent(content);
      comment.setIsModified(true);
    }
    return comment;
  }

  public static CommentDto response(Comment comment, Boolean showPost, List<Comment> childs) {
    if (!comment.getUser().getStatus()) return withdrawResponse(comment, showPost, childs);
    if (!comment.getStatus()) return deletedResponse(comment, showPost, childs);
    return new CommentDto(
        comment.getStatus(),
        comment.getId(),
        comment.getParent() == null ? null : CommentDto.idOnly(comment.getParent()),
        comment.getContent(),
        comment.getAnonymity(),
        comment.getAnonymity() ? null : UserDto.communityResponse(comment.getUser()),
        null,
        comment.getAnonymityId().equals(0) ? null : comment.getAnonymityId(),
        comment.getUser().getId().equals(Optional.ofNullable(comment.getPost().getUser()).map(User::getId).orElse(null)),
        comment.getUser().getId().equals(SecurityUtil.getCurrentUserId()),
        comment.getIsModified(),
        comment.getCreatedAt(),
        comment.getModifiedAt(),
        showPost ? PostDto.previewResponse(comment.getPost()) : null,
        childs == null || childs.isEmpty() ? null : childs.stream().map(child -> CommentDto.response(child, false, null)).toList()
    );
  }

  public static CommentDto idOnly(Comment comment) {
    return new CommentDto(
        comment.getStatus(),
        comment.getId(),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    );
  }

  public static CommentDto deletedResponse(Comment comment, Boolean showPost, List<Comment> childs) {
    return new CommentDto(
        comment.getStatus(),
        comment.getId(),
        comment.getParent() == null ? null : CommentDto.idOnly(comment.getParent()),
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        comment.getIsModified(),
        null,
        null,
        showPost ? PostDto.previewResponse(comment.getPost()) : null,
        childs == null || childs.isEmpty() ? null : childs.stream().map(child -> CommentDto.response(child, false, null)).toList()
    );
  }

  public static CommentDto withdrawResponse(Comment comment, Boolean showPost, List<Comment> childs) {
    return new CommentDto(
        comment.getStatus(),
        comment.getId(),
        comment.getParent() == null ? null : CommentDto.idOnly(comment.getParent()),
        comment.getContent(),
        comment.getAnonymity(),
        null,
        true,
        comment.getAnonymityId().equals(0) ? null : comment.getAnonymityId(),
        null,
        null,
        comment.getIsModified(),
        comment.getCreatedAt(),
        comment.getModifiedAt(),
        showPost ? PostDto.previewResponse(comment.getPost()) : null,
        childs == null || childs.isEmpty() ? null : childs.stream().map(child -> CommentDto.response(child, false, null)).toList()
    );
  }

  public static CommentDto adminResponse(Comment comment) {
    return new CommentDto(
        comment.getStatus(),
        comment.getId(),
        comment.getParent() == null ? null : CommentDto.idOnly(comment.getParent()),
        comment.getContent(),
        comment.getAnonymity(),
        UserDto.communityResponse(comment.getUser()),
        null,
        comment.getAnonymityId().equals(0) ? null : comment.getAnonymityId(),
        comment.getUser().getId().equals(Optional.ofNullable(comment.getPost().getUser()).map(User::getId).orElse(null)),
        comment.getUser().getId().equals(SecurityUtil.getCurrentUserId()),
        comment.getIsModified(),
        comment.getCreatedAt(),
        comment.getModifiedAt(),
        PostDto.previewResponse(comment.getPost()),
        null
    );
  }
}
