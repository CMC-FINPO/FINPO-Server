package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.BlockedUser;
import kr.finpo.api.domain.Comment;
import kr.finpo.api.domain.Post;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.BlockedUserDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.BlockedUserRepository;
import kr.finpo.api.repository.CommentRepository;
import kr.finpo.api.repository.PostRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class BlockedUserService {

  private final UserRepository userRepository;
  private final BlockedUserRepository blockedUserRepository;
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;

  private User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  public List<BlockedUserDto> getNonAnonymities() {
    try {
      return blockedUserRepository.findByUserIdAndAnonymityOrderByIdDesc(SecurityUtil.getCurrentUserId(), false).stream().map(BlockedUserDto::response).toList();
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean insert(Long postId, Long commentId) {
    try {
      User blockUser = null;
      Boolean anonymity = null;
      if (!isEmpty(postId)) {
        Post post = postRepository.findById(postId).get();
        blockUser = post.getUser();
        anonymity = post.getAnonymity();
      }
      else if (!isEmpty(commentId)) {
        Comment comment = commentRepository.findById(commentId).get();
        blockUser = comment.getUser();
        anonymity = comment.getAnonymity();
      }

      if (blockedUserRepository.findOneByUserIdAndBlockedUserIdAndAnonymity(SecurityUtil.getCurrentUserId(), blockUser.getId(), anonymity).isPresent())
        throw new GeneralException(ErrorCode.BAD_REQUEST, "Already blocked");
      blockedUserRepository.save(BlockedUser.of(getMe(), blockUser, anonymity));
      return true;
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteNonAnonymity(Long id) {
    try {
      if (blockedUserRepository.findById(id).get().getAnonymity().equals(true))
        throw new GeneralException(ErrorCode.BAD_REQUEST);
      blockedUserRepository.deleteById(id);
      return true;
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

