package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.CategoryDto;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class CategoryService {

  private final CategoryRepository categoryRepository;
  private final InterestCategoryRepository interestCategoryRepository;
  private final UserRepository userRepository;

  @Value("${upload.url}")
  private String uploadUrl;

  public User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  public void authorizeMe(Long id) {
    if (!id.equals(SecurityUtil.getCurrentUserId()))
      throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
  }

  public void initialize() {
    List<String> firsts = Arrays.asList("일자리", "생활안정", "교육문화", "참여공간");
    List<String> firstImgs = Arrays.asList("work", "live", "edu", "space");
    List<String> seconds = Arrays.asList("진로", "취업", "창업", "생활지원", "건강", "교육", "문화/예술", "사회참여", "공간", "대외활동");
    List<Integer> parents = Arrays.asList(1, 1, 1, 2, 2, 3, 3, 4, 4, 4);

    Long id = 0L;
    for (int i = 0; i < firsts.size(); i++)
      categoryRepository.save(Category.of(++id, firsts.get(i), 1L, uploadUrl + firstImgs.get(i) + ".png"));

    for (int i = 0; i < seconds.size(); i++) {
      Category category = Category.of(++id, seconds.get(i), 2L);
      category.setParent(categoryRepository.findById((long) parents.get(i)).get());
      categoryRepository.save(category);
    }
  }

  public List<CategoryDto> getByParentId(Long parentId) {
    try {
      return categoryRepository.findByParentId(parentId).stream().map(CategoryDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<CategoryDto> getAllByChildFormat() {
    try {
      return categoryRepository.findByDepth(1L).stream().map(e ->
          CategoryDto.childsResponse(e, categoryRepository.findByParentId(e.getId()).stream().map(ee -> CategoryDto.childsResponse(ee, null)).toList())
      ).toList();

    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<CategoryDto> getByDepth(Long depth) {
    try {
      return categoryRepository.findByDepth(depth).stream().map(CategoryDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Optional<CategoryDto> getById(Long id) {
    try {
      return categoryRepository.findById(id).map(CategoryDto::response);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<InterestCategoryDto> getMyInterests() {
    try {
      return interestCategoryRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(InterestCategoryDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<CategoryDto> getMyInterestsByDepth() {
    try {
      return interestCategoryRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(InterestCategory::getCategory).map(Category::getParent).distinct().map(CategoryDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<InterestCategoryDto> updateMyInterests(List<InterestCategoryDto> dtos) {
    try {
      User user = getMe();
      interestCategoryRepository.deleteByUserId(user.getId());
      return insertInterests(dtos);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<InterestCategoryDto> insertInterests(List<InterestCategoryDto> dtos) {
    return insertInterests(dtos, getMe());
  }

  public List<InterestCategoryDto> insertInterests(List<InterestCategoryDto> dtos, User user) {
    try {
      ArrayList<InterestCategoryDto> res = new ArrayList<>();

      List<InterestCategoryDto> cowDtos = new CopyOnWriteArrayList<>() {{
        addAll(dtos);
      }};

      cowDtos.forEach(dto -> {
        // 부모 카테고리 지정 시 자식 카테고리 전부 추가
        if (categoryRepository.findById(dto.categoryId()).get().getDepth().equals(1L)) {
          categoryRepository.findByParentId(dto.categoryId()).forEach((category -> {
            cowDtos.add(InterestCategoryDto.of(category.getId()));
            cowDtos.remove(dto);
          }));
        }
      });

      cowDtos.forEach(dto -> {
        // 관심카테고리 이미 존재 시 넘어감
        if (interestCategoryRepository.findByUserIdAndCategoryId(user.getId(), dto.categoryId()).isPresent())
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
      ids.forEach(this::delete);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean delete(Long id) {
    try {
      InterestCategory interestCategory = interestCategoryRepository.findById(id).get();
      authorizeMe(interestCategory.getUser().getId());
      interestCategoryRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

