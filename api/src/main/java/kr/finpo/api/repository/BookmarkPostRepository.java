package kr.finpo.api.repository;

import kr.finpo.api.domain.BookmarkPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookmarkPostRepository extends JpaRepository<BookmarkPost, Long> {
  @Query("SELECT bp FROM BookmarkPost bp WHERE bp.user.id = :userId AND bp.post.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = bp.post.anonymity)")
  public Page<BookmarkPost> findByUserId(Long userId, Pageable pageable);

  public Optional<BookmarkPost> findOneByUserIdAndPostId(Long userId, Long postId);

  public Long deleteByUserId(Long userId);

  public Long deleteByPostId(Long postId);
}

