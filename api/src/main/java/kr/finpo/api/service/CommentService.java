package kr.finpo.api.service;

import kr.finpo.api.constant.Constraint;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.CommentDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class CommentService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final CommentRepository commentRepository;
  private final PostService postService;
  private final FcmService fcmService;

  private User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  private void authorizeMe(Long id) {
    if (!id.equals(SecurityUtil.getCurrentUserId()))
      throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
  }

  public void checkStatus(Long id) {
    if (!commentRepository.findById(id).get().getStatus())
      throw new GeneralException(ErrorCode.BAD_REQUEST, "This comment already had been deleted");
  }

  public Page<CommentDto> getByPostId(Long postId, Pageable pageable) {
    try {
      postService.checkStatus(postId);

      List<Comment> comments = commentRepository.findByPostIdAndParentId(postId, null, pageable);

      List<CommentDto> commentDtos = comments.stream().map(comment ->
          CommentDto.response(comment, false, commentRepository.findByPostIdAndParentId(postId, comment.getId()))
      ).toList();

      return new PageImpl<>(commentDtos, pageable, commentDtos.size());
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public CommentDto insert(Long postId, CommentDto dto) {
    try {
      if (dto.content().length() > Constraint.COMMENT_MAX_LENGTH)
        throw new GeneralException(ErrorCode.BAD_REQUEST, "Content's length must equal or less than " + Constraint.COMMENT_MAX_LENGTH);


      Post post = postRepository.findById(postId).get();
      postService.checkStatus(postId);

      Comment comment = Comment.of(dto.content(), dto.anonymity());

      // 익명 id 배정
      if (post.getUser() != null && !post.getUser().getId().equals(SecurityUtil.getCurrentUserId()) && dto.anonymity()) {
        Optional<Comment> beforeComment = commentRepository.findFirst1ByPostIdAndUserIdAndAnonymity(postId, SecurityUtil.getCurrentUserId(), true);

        Optional<Comment> latestAnonymityComment = commentRepository.findFirst1ByPostIdAndAnonymity(postId, true);

        comment.setAnonymityId(
            beforeComment.map(Comment::getAnonymityId)
                .orElse(latestAnonymityComment.map(value -> value.getAnonymityId() + 1).orElse(1))
        );
      }

      if (dto.parent() != null && dto.parent().id() != null)
        comment.setParent(commentRepository.findById(dto.parent().id()).get());
      comment.setUser(getMe());
      comment.setPost(post);
      comment = commentRepository.save(comment);

      log.debug("sendCommentPush: " + fcmService.sendCommentPush(comment).toString());

      return CommentDto.response(comment, true, null);
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public CommentDto update(Long id, CommentDto dto) {
    try {
      Comment comment = dto.updateEntity(commentRepository.findById(id).get());
      authorizeMe(comment.getUser().getId());
      checkStatus(id);
      comment = commentRepository.save(comment);
      return CommentDto.response(comment, false, null);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean delete(Long id) {
    try {
      Comment comment = commentRepository.findById(id).get();
      authorizeMe(comment.getUser().getId());
      checkStatus(id);
      comment.setStatus(false);
      commentRepository.save(comment);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

