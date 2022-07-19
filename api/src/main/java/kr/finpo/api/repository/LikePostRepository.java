package kr.finpo.api.repository;

import kr.finpo.api.domain.LikePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikePostRepository extends JpaRepository<LikePost, Long> {
  @Query("SELECT lp FROM LikePost lp WHERE lp.user.id = :userId AND lp.post.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = lp.post.anonymity)")
  public Page<LikePost> findByUserId(Long userId, Pageable pageable);

  public List<LikePost> findByUserIdAndPostId(Long userId, Long postId);

  public Long deleteByUserId(Long id);

  public Long deleteByPostId(Long id);
}

