package kr.finpo.api.repository;

import java.util.List;
import kr.finpo.api.domain.PostImg;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostImgRepository extends JpaRepository<PostImg, Long> {

    List<PostImg> findByPostId(Long id);

    Long deleteByPostId(Long id);
}

