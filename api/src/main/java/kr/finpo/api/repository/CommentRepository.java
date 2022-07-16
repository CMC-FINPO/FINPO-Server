package kr.finpo.api.repository;

import kr.finpo.api.domain.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
  @Query("SELECT cm FROM Comment cm WHERE cm.parent IS NULL AND cm.post.id =:postId AND cm.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = cm.anonymity)")
  public Page<Comment> findByPostId(Long postId, Long userId, Pageable pageable);

  @Query("SELECT cm FROM Comment cm WHERE cm.parent.id =:parentId AND cm.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = cm.anonymity)")
  public List<Comment> findByParentId(Long parentId, Long userId);

  public Optional<Comment> findFirst1ByPostIdAndUserIdAndAnonymity(Long id, Long userId, Boolean anonymity);

  public Optional<Comment> findFirst1ByPostIdAndAnonymity(Long userId, Boolean anonymity);

  public List<Comment> findByUserId(Long userId);

  @Query("SELECT cm FROM Comment cm WHERE cm.status = true AND cm.user.id =:userId AND cm.post.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = cm.post.anonymity) GROUP BY cm.post.id")
  public Page<Comment> findByUserId(Long userId, Pageable pageable);
}

