package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.*;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class PostService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final LikePostRepository likePostRepository;
  private final PostImgRepository postImgRepository;

  public User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  public void authorizeMe(Long id) {
    if (!id.equals(SecurityUtil.getCurrentUserId()))
      throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
  }

  public PostDto get(Long id) {
    try {
      postRepository.increaseHits(id);
      Post post = postRepository.findById(id).get();
      return PostDto.response(post, postImgRepository.findByPostId(post.getId()));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> getMy(Pageable pageable) {
    try {
      return postRepository.querydslFindMy(pageable).map(PostDto::previewResponse);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> search(String content, Pageable pageable) {
    try {
      return postRepository.querydslFindbyContent(content, pageable).map(PostDto::previewResponse);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto insert(PostDto dto) {
    try {
      Post post = Post.of(dto.content(), dto.anonymity());
      post.setUser(getMe());
      post = postRepository.save(post);
      List<PostImg> postImgs = insertPostImg(dto, post);
      return PostDto.response(post, postImgs);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  private List<PostImg> insertPostImg(PostDto dto, Post post) {
    List<PostImg> postImgs = new ArrayList<>();
    if (dto.imgs() != null)
      dto.imgs().forEach(imgDto ->
          postImgs.add(postImgRepository.save(PostImg.of(imgDto.img(), imgDto.order(), post)))
      );
    return postImgs;
  }

  public PostDto update(PostDto dto, Long id) {
    try {
      Post post = dto.updateEntity(postRepository.findById(id).get());
      authorizeMe(post.getUser().getId());
      post = postRepository.save(post);

      if (dto.imgs() != null) {
        postImgRepository.deleteByPostId(id);
        insertPostImg(dto, post);
      }

      return PostDto.response(post, postImgRepository.findByPostId(id));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean delete(Long id) {
    try {
      authorizeMe(postRepository.findById(id).get().getUser().getId());
      postRepository.deleteById(id);
      postImgRepository.deleteByPostId(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto like(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      User user = getMe();

      // 자추 금지
      if (post.getUser().getId().equals(user.getId()))
        throw new GeneralException(ErrorCode.BAD_REQUEST, "You're the writer of this post");

      likePostRepository.findOneByUserIdAndPostId(user.getId(), id).ifPresentOrElse(null, () -> {
        likePostRepository.save(LikePost.of(user, post));
      });

      return PostDto.previewResponse(post);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto deleteLike(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      User user = getMe();

      likePostRepository.findOneByUserIdAndPostId(user.getId(), id).ifPresent(likePostRepository::delete);

      return PostDto.previewResponse(post);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

