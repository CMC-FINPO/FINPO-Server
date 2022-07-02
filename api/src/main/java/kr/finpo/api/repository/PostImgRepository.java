package kr.finpo.api.repository;

import kr.finpo.api.domain.PostImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PostImgRepository extends JpaRepository<PostImg, Long> {
  public List<PostImg> findByPostId(Long id);
  public Long deleteByPostId(Long id);
}

