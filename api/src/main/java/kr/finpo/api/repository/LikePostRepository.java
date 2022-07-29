package kr.finpo.api.repository;

import java.util.List;
import kr.finpo.api.domain.LikePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface LikePostRepository extends JpaRepository<LikePost, Long> {

    @Query("SELECT lp FROM LikePost lp WHERE lp.user.id = :userId AND lp.post.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = lp.post.anonymity)")
    Page<LikePost> findByUserId(Long userId, Pageable pageable);

    List<LikePost> findByUserIdAndPostId(Long userId, Long postId);

    Long deleteByUserId(Long id);

    Long deleteByPostId(Long id);
}

