package kr.finpo.api.repository;

import java.util.List;
import kr.finpo.api.domain.BookmarkPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface BookmarkPostRepository extends JpaRepository<BookmarkPost, Long> {

    @Query("SELECT bp FROM BookmarkPost bp WHERE bp.user.id = :userId AND bp.post.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = bp.post.anonymity)")
    Page<BookmarkPost> findByUserId(Long userId, Pageable pageable);

    Long countByUserId(Long userId);

    List<BookmarkPost> findByUserIdAndPostId(Long userId, Long postId);

    Long deleteByUserId(Long userId);

    Long deleteByPostId(Long postId);
}

