package kr.finpo.api.repository;

import kr.finpo.api.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
  @Query("SELECT COUNT(cm.id) FROM Comment cm WHERE cm.post.id = :id AND cm.status = true AND cm.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = cm.anonymity) AND NOT EXISTS (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = cm.parent.anonymity AND bu.blockedUser.id = cm.parent.user.id)")
  public Long countComments(Long id, Long userId);

  List<Post> findByUserId(Long userId);

  @Query("SELECT po FROM Post po WHERE (:content IS NULL OR po.content LIKE CONCAT('%', :content, '%')) AND (:lastId IS NULL OR po.id < :lastId) AND po.user.id NOT IN (SELECT bu.blockedUser.id FROM BlockedUser bu WHERE bu.user.id = :userId AND bu.anonymity = po.anonymity)")
  public Page<Post> findAll(Long userId, Long lastId, String content, Pageable pageable);

  @Query("SELECT po FROM Post po WHERE po.user.id = :userId")
  public Page<Post> findMy(Long userId, Pageable pageable);

  @Modifying
  @Query("update Post p set p.hits = p.hits + 1 where p.id = :id")
  void increaseHits(Long id);
}

