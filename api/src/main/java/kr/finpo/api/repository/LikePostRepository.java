package kr.finpo.api.repository;

import kr.finpo.api.domain.LikePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface LikePostRepository extends JpaRepository<LikePost, Long> {
  public Page<LikePost> findByUserId(Long id, Pageable pageable);
  public Optional<LikePost> findOneByUserIdAndPostId(Long userId, Long postId);
  public Long deleteByPostId(Long id);
  public Long deleteByUserId(Long id);
}
