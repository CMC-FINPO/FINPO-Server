package kr.finpo.api.repository;

import kr.finpo.api.domain.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom{
  List<Post> findByStatus(Boolean status);
  List<Post> findByUserId(Long userId);

  @Modifying
  @Query("update Post p set p.hits = p.hits + 1 where p.id = :id")
  void increaseHits(Long id);
}

