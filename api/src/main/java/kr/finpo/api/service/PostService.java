package kr.finpo.api.service;

import kr.finpo.api.constant.Constraint;
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
import java.util.Optional;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class PostService {

  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final LikePostRepository likePostRepository;
  private final BookmarkPostRepository bookmarkPostRepository;
  private final CommentRepository commentRepository;
  private final PostImgRepository postImgRepository;

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
    if (!postRepository.findById(id).get().getStatus())
      throw new GeneralException(ErrorCode.BAD_REQUEST, "This post already had been deleted");
  }

  public PostDto get(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      checkStatus(id);
      postRepository.increaseHits(id);
      return PostDto.response(post, postImgRepository.findByPostId(post.getId()), likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> getMy(Pageable pageable) {
    try {
      return postRepository.querydslFindMy(pageable).map(e -> PostDto.previewResponse(e, likePostRepository, bookmarkPostRepository));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> getMyLikes(Pageable pageable) {
    try {
      return likePostRepository.findByUserId(SecurityUtil.getCurrentUserId(), pageable).map(likePost -> PostDto.previewResponse(likePost.getPost(), likePostRepository, bookmarkPostRepository));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> getMyBookmarks(Pageable pageable) {
    try {
      return bookmarkPostRepository.findByUserId(SecurityUtil.getCurrentUserId(), pageable).map(likePost -> PostDto.previewResponse(likePost.getPost(), likePostRepository, bookmarkPostRepository));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> getMyCommentPosts(Pageable pageable) {
    try {
      return commentRepository.findByUserId(SecurityUtil.getCurrentUserId(), pageable).map(comment -> PostDto.previewResponse(comment.getPost(), likePostRepository, bookmarkPostRepository));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<PostDto> search(String content, Pageable pageable) {
    try {
      return postRepository.querydslFindbyContent(content, pageable).map(e -> PostDto.previewResponse(e, likePostRepository, bookmarkPostRepository));
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
      return PostDto.response(post, postImgs, likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  private List<PostImg> insertPostImg(PostDto dto, Post post) {
    List<PostImg> postImgs = new ArrayList<>();
    Optional.ofNullable(dto.imgs()).ifPresent(imgDtos -> {
          if (imgDtos.size() > Constraint.POST_IMAGE_MAX_CNT)
            throw new GeneralException(ErrorCode.BAD_REQUEST, "Images must equal or less than " + Constraint.POST_IMAGE_MAX_CNT);
          imgDtos.forEach(imgDto ->
              postImgs.add(postImgRepository.save(PostImg.of(imgDto.img(), imgDto.order(), post)))
          );
        }
    );
    return postImgs;
  }

  public PostDto update(PostDto dto, Long id) {
    try {
      Post post = dto.updateEntity(postRepository.findById(id).get());
      authorizeMe(post.getUser().getId());
      checkStatus(id);
      Post finalPost = postRepository.save(post);

      Optional.ofNullable(dto.imgs()).ifPresent((e) -> {
        postImgRepository.deleteByPostId(id);
        insertPostImg(dto, finalPost);
      });

      return PostDto.response(post, postImgRepository.findByPostId(id), likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean delete(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      authorizeMe(post.getUser().getId());
      checkStatus(id);
      post.setStatus(false);
      postRepository.save(post);
      likePostRepository.deleteByPostId(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto like(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      User user = getMe();
      checkStatus(id);

      // 자추 금지
      Optional.ofNullable(post.getUser()).ifPresent(postUser -> {
        if (postUser.getId().equals(user.getId()))
          throw new GeneralException(ErrorCode.BAD_REQUEST, "You're the writer of this post");
      });


      likePostRepository.findOneByUserIdAndPostId(user.getId(), id).ifPresentOrElse(null, () -> {
        likePostRepository.save(LikePost.of(user, post));
      });

      return PostDto.previewResponse(post, likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto deleteLike(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      User user = getMe();
      checkStatus(id);

      likePostRepository.findOneByUserIdAndPostId(user.getId(), id).ifPresent(likePostRepository::delete);

      return PostDto.previewResponse(post, likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto bookmark(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      User user = getMe();
      checkStatus(id);

      bookmarkPostRepository.findOneByUserIdAndPostId(user.getId(), id).ifPresentOrElse(null, () -> {
        bookmarkPostRepository.save(BookmarkPost.of(user, post));
      });

      return PostDto.previewResponse(post, likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public PostDto deleteBookmark(Long id) {
    try {
      Post post = postRepository.findById(id).get();
      User user = getMe();
      checkStatus(id);

      bookmarkPostRepository.findOneByUserIdAndPostId(user.getId(), id).ifPresent(bookmarkPostRepository::delete);

      return PostDto.previewResponse(post, likePostRepository, bookmarkPostRepository);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

