package kr.finpo.api.repository;

import kr.finpo.api.domain.BookmarkPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkPostRepository extends JpaRepository<BookmarkPost, Long> {
  public Page<BookmarkPost> findByUserId(Long id, Pageable pageable);
  public Optional<BookmarkPost> findOneByUserIdAndPostId(Long userId, Long postId);
  public Long deleteByUserId(Long id);
}

