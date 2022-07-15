package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.BlockedUser;
import kr.finpo.api.domain.User;
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

  public Boolean insert(Long postId, Long commentId) {
    try {
      User blockUser = null;
      if (!isEmpty(postId))
        blockUser = postRepository.findById(postId).get().getUser();
      else if (!isEmpty(commentId))
        blockUser = commentRepository.findById(commentId).get().getUser();

      if (blockedUserRepository.findOneByUserIdAndBlockedUserId(SecurityUtil.getCurrentUserId(), blockUser.getId()).isPresent()) return false;

      blockedUserRepository.save(BlockedUser.of(getMe(), blockUser));
      return true;
    } catch (GeneralException e) {
      throw e;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

