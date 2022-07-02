package kr.finpo.api.repository;

import kr.finpo.api.domain.LikePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LikePostRepository extends JpaRepository<LikePost, Long> {
  public List<LikePost> findByUserId(Long id);
  public Optional<LikePost> findOneByUserIdAndPostId(Long userId, Long postId);
  public Long countByPostId(Long id);
  public Long deleteByPostId(Long id);
}

