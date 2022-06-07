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
import kr.finpo.api.dto.PolicyDto;
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
import java.util.stream.StreamSupport;

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

    if (!isEmpty(pageable.getSort())) {
      for (Sort.Order order : pageable.getSort()) {
        Order direction = order.getDirection().isAscending() ? Order.ASC : Order.DESC;
        OrderSpecifier<?> orderSpecifier;
        switch (order.getProperty()) {
          case "title":
            orderSpecifier = QuerydslUtil.getSortedColumn(direction, QPolicy.policy, "title");
            orders.add(orderSpecifier);
            break;
          case "endDate":
            orderSpecifier = QuerydslUtil.getSortedColumn(direction, QPolicy.policy, "endDate");
            orders.add(orderSpecifier);
            break;
          case "startDate":
            orderSpecifier = QuerydslUtil.getSortedColumn(direction, QPolicy.policy, "startDate");
            orders.add(orderSpecifier);
            break;
          case "modifiedAt":
            orderSpecifier = QuerydslUtil.getSortedColumn(direction, QPolicy.policy, "modifiedAt");
            orders.add(orderSpecifier);
            break;
          case "institution":
            orderSpecifier = QuerydslUtil.getSortedColumn(direction, QPolicy.policy, "institution");
            orders.add(orderSpecifier);
            break;
          default:
            break;
        }
      }
    }
    return orders;
  }


  @Override
  public Page<PolicyDto> querydslFindMy(List<InterestCategoryDto> myCategoryDtos, List<InterestRegionDto> myRegionDtos, Pageable pageable) {

    List<OrderSpecifier> orders = getAllOrderSpecifiers(pageable);

    BooleanBuilder categoryBuilder = new BooleanBuilder(),
        regionBuilder = new BooleanBuilder();

    myCategoryDtos.forEach(dto -> {
      log.debug("관심카테고리 " + dto.category().getId() + " " + dto.category().getName());
      categoryBuilder.or(p.category.id.eq(dto.category().getId()));
    });
    myRegionDtos.forEach(dto -> {
      log.debug("관심지역 " + dto.region().getId() + " " + dto.region().getName());
      regionBuilder.or(p.region.id.eq(dto.region().getId()));
    });

    QueryResults<Policy> results = jpaQueryFactory
        .selectFrom(QPolicy.policy)
        .where(categoryBuilder, regionBuilder, p.status.eq(true))
        .orderBy(orders.stream().toArray(OrderSpecifier[]::new))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<PolicyDto> content = StreamSupport.stream(results.getResults().spliterator(), false).map(PolicyDto::response).toList();
    ;

    return new PageImpl<>(content, pageable, results.getTotal());
  }

  @Override
  public Page<PolicyDto> querydslFindbyTitle(String title, LocalDate startDate, LocalDate endDate, List<Long> categoryIds, List<Long> regionIds, Pageable pageable) {

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

    QueryResults<Policy> results = jpaQueryFactory
        .selectFrom(QPolicy.policy)
        .where(categoryBuilder, regionBuilder, builder, p.status.eq(true))
        .orderBy(orders.stream().toArray(OrderSpecifier[]::new))
        .offset(pageable.getOffset())
        .limit(pageable.getPageSize())
        .fetchResults();

    List<PolicyDto> content = StreamSupport.stream(results.getResults().spliterator(), false).map(PolicyDto::response).toList();
    ;

    return new PageImpl<>(content, pageable, results.getTotal());
  }

}