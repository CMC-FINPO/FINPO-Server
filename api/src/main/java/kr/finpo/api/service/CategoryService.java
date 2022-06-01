package kr.finpo.api.service;


import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final InterestCategoryRepository interestCategoryRepository;
  private final UserRepository userRepository;

  public void initialize() {
    List<String> firsts = Arrays.asList("일자리", "생활안정", "교육문화", "참여공간");
    List<String> seconds = Arrays.asList("진로", "취업", "창업", "생활지원", "건강", "교육", "문화/예술", "사회참여", "공간", "대외활동");
    List<Integer> parents = Arrays.asList(1, 1, 1, 2, 2, 3, 3, 4, 4, 4);

    Long id = 0L;
    for (int i = 0; i < firsts.size(); i++)
      categoryRepository.save(Category.of(++id, firsts.get(i), 1L));

    for (int i = 0; i < seconds.size(); i++) {
      Category category = Category.of(++id, seconds.get(i), 2L);
      category.setParent(categoryRepository.findById((long) parents.get(i)).get());
      categoryRepository.save(category);
    }
  }

  public List<Category> getByParentId(Long parentId) {
    try {
      return categoryRepository.findByParentId(parentId);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<Category> getByDepth(Long depth) {
    try {
      return categoryRepository.findByDepth(depth);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Optional<Category> getById(Long id) {
    try {
      return categoryRepository.findById(id);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.BAD_REQUEST, "id not valid");
    }
  }

  public List<InterestCategoryDto> getMyInterests() {
    try {
      return StreamSupport.stream(interestCategoryRepository.findByUserId(SecurityUtil.getCurrentUserId()).spliterator(), false).map(InterestCategoryDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<InterestCategoryDto> insertMyInterests(List<InterestCategoryDto> dtos) {
    try {
      ArrayList<InterestCategoryDto> res = new ArrayList<InterestCategoryDto>();

      User user = userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
          () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED));

      dtos.stream().forEach(dto -> {
        // 관심카테고리 이미 존재 시 넘어감
        if (interestCategoryRepository.findByUserIdAndCategoryId(SecurityUtil.getCurrentUserId(), dto.categoryId()).isPresent())
          return;

        Category category = categoryRepository.findById(dto.categoryId()).get();
        InterestCategory interestCategory = InterestCategory.of(user, category);
        interestCategory = interestCategoryRepository.save(interestCategory);
        res.add(InterestCategoryDto.response(interestCategory));
      });

      return res;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

    public Boolean deleteByParams(List<Long> ids) {
    try {
      ids.stream().forEach(id->{
        delete(id);
      });
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean delete(Long id) {
    try {
      InterestCategory interestCategory = interestCategoryRepository.findById(id).get();
      log.debug("삭제카테고리유저: " + interestCategory.getUser().getId());
      log.debug("현재유저 : " + SecurityUtil.getCurrentUserId());
      if (!interestCategory.getUser().getId().equals(SecurityUtil.getCurrentUserId()))
        throw new GeneralException(ErrorCode.USER_NOT_EQUAL);

      interestCategoryRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

