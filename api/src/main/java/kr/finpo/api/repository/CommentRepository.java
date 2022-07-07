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
  public List<Comment> findByPostIdAndParentId(Long id, Long parentId, Pageable pageable);

  public List<Comment> findByPostIdAndParentId(Long id, Long parentId);

  public Optional<Comment> findFirst1ByPostIdAndUserIdAndAnonymity(Long id, Long userId, Boolean anonymity);

  public Optional<Comment> findFirst1ByPostIdAndAnonymity(Long userId, Boolean anonymity);

  public List<Comment> findByUserId(Long userId);

  @Query("SELECT comment FROM Comment comment WHERE comment.user.id = :userId GROUP BY comment.post.id")
  public Page<Comment> findByUserId(Long userId, Pageable pageable);
}

