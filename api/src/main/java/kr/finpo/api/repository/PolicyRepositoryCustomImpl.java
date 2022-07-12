package kr.finpo.api.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.QPolicy;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.util.QuerydslUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import javax.persistence.EntityManager;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.ObjectUtils.isEmpty;


@Slf4j
public class PolicyRepositoryCustomImpl implements PolicyRepositoryCustom {

  private final JPAQueryFactory jpaQueryFactory;
  private final QPolicy p = QPolicy.policy;

  public PolicyRepositoryCustomImpl(EntityManager em) {
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
          case "title":
            fieldName = "title";
            break;
          case "endDate":
            fieldName = "endDate";
            break;
          case "startDate":
            fieldName = "startDate";
            break;
          case "modifiedAt":
            fieldName = "modifiedAt";
            break;
          case "institution":
            fieldName = "institution";
            break;
          case "countOfInterest":
            fieldName = "countOfInterest";
            break;
          case "id":
            fieldName = "id";
            break;
          default:
            break;
        }
        orderSpecifier = QuerydslUtil.getSortedColumn(direction, QPolicy.policy, fieldName);
        orders.add(orderSpecifier);
      }
    }
    orderSpecifier = QuerydslUtil.getSortedColumn(Order.DESC, QPolicy.policy, "id");
    orders.add(orderSpecifier);

    return orders;
  }


  @Override
  public Page<Policy> querydslFindMy(List<InterestCategoryDto> myCategoryDtos, List<InterestRegionDto> myRegionDtos, Pageable pageable) {

    List<OrderSpecifier> orders = getAllOrderSpecifiers(pageable);

    BooleanBuilder categoryBuilder = new BooleanBuilder(),
        regionBuilder = new BooleanBuilder();

    myCategoryDtos.forEach(dto -> categoryBuilder.or(p.category.id.eq(dto.category().getId())));
    myRegionDtos.forEach(dto -> regionBuilder.or(p.region.id.eq(dto.region().getId())));

    QueryResults<Policy> results = jpaQueryFactory
        .selectFrom(QPolicy.policy)
        .where(categoryBuilder, regionBuilder, p.status.eq(true))
        .orderBy(orders.toArray(OrderSpecifier[]::new))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<Policy> content = results.getResults();
    ;

    return new PageImpl<>(content, pageable, results.getTotal());
  }

  @Override
  public Page<Policy> querydslFindbyTitle(String title, LocalDate startDate, LocalDate endDate, List<Long> categoryIds, List<Long> regionIds, Boolean status, Pageable pageable) {

    System.out.println(title);

    List<OrderSpecifier> orders = getAllOrderSpecifiers(pageable);

    BooleanBuilder categoryBuilder = new BooleanBuilder(),
        regionBuilder = new BooleanBuilder(),
        builder = new BooleanBuilder();

    if (!isEmpty(categoryIds))
      categoryIds.forEach(id -> {
        if (!isEmpty(id))
          categoryBuilder.or(p.category.id.eq(id));
      });
    if (!isEmpty(regionIds))
      regionIds.forEach(id -> {
        if (!isEmpty(id))
          regionBuilder.or(p.region.id.eq(id));
      });

    if (!isEmpty(title)) builder.and(p.title.contains(title));
    if (!isEmpty(startDate)) builder.and(p.startDate.after(startDate.minusDays(1)));
    if (!isEmpty(endDate)) builder.and(p.endDate.before(startDate.plusDays(1)));
    if (!isEmpty(status)) builder.and(p.status.eq(status));

    QueryResults<Policy> results = jpaQueryFactory
        .selectFrom(QPolicy.policy)
        .where(categoryBuilder, regionBuilder, builder)
        .orderBy(orders.toArray(OrderSpecifier[]::new))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<Policy> content = results.getResults();

    return new PageImpl<>(content, pageable, results.getTotal());
  }

}
