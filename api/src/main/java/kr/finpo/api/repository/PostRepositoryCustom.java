package kr.finpo.api.repository;

import kr.finpo.api.domain.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


public interface PostRepositoryCustom {
  Page<Post> querydslFindMy(Pageable pageable);
  Page<Post> querydslFindbyContent(String keyword,Long lastId, Pageable pageable);
}

