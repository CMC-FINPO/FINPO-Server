package kr.finpo.api.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.finpo.api.domain.Post;
import kr.finpo.api.domain.QPost;
import kr.finpo.api.util.QuerydslUtil;
import kr.finpo.api.util.SecurityUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;


@Slf4j
public class PostRepositoryCustomImpl implements PostRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;
  private final QPost p = QPost.post;

  public PostRepositoryCustomImpl(EntityManager em) {
    jpaQueryFactory = new JPAQueryFactory(em);
  }

  private List<OrderSpecifier> getAllOrderSpecifiers(Pageable pageable) {
    List<OrderSpecifier> orders = new ArrayList<>();
    OrderSpecifier<?> orderSpecifier;

    if (!isEmpty(pageable.getSort())) {
      for (Sort.Order order : pageable.getSort()) {
        Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
        String fieldName = null;
        switch (order.getProperty()) {
          case "createdAt":
            fieldName = "createdAt";
            break;
          case "id":
            fieldName = "id";
            break;
          case "likes":
            fieldName = "likes";
            break;
          default:
            break;
        }
        orderSpecifier = QuerydslUtil.getSortedColumn(direction, p, fieldName);
        orders.add(orderSpecifier);
      }
    }
    orderSpecifier = QuerydslUtil.getSortedColumn(Order.DESC, p, "id");
    orders.add(orderSpecifier);
    return orders;
  }

  @Override
  public Page<Post> querydslFindMy(Pageable pageable) {
    List<OrderSpecifier> orders = getAllOrderSpecifiers(pageable);

    BooleanBuilder builder = new BooleanBuilder();
    builder.and(p.user.id.eq(SecurityUtil.getCurrentUserId()));
    builder.and(p.status.eq(true));

    QueryResults<Post> results = jpaQueryFactory
        .selectFrom(p)
        .where(builder, p.status.eq(true))
        .orderBy(orders.toArray(OrderSpecifier[]::new))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<Post> content = results.getResults();

    return new PageImpl<>(content, pageable, results.getTotal());
  }

  @Override
  public Page<Post> querydslFindbyContent(String keyword, Pageable pageable) {
    List<OrderSpecifier> orders = getAllOrderSpecifiers(pageable);

    BooleanBuilder builder = new BooleanBuilder();
    if (!isEmpty(keyword)) builder.and(p.content.contains(keyword));
    builder.and(p.status.eq(true));

    QueryResults<Post> results = jpaQueryFactory
        .selectFrom(p)
        .where(builder, p.status.eq(true))
        .orderBy(orders.toArray(OrderSpecifier[]::new))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<Post> content = results.getResults();

    return new PageImpl<>(content, pageable, results.getTotal());
  }
}
