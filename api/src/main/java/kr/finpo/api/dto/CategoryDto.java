package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.domain.Category;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CategoryDto(
    Long id,
    String name,
    Long depth,
    String img,
    Category parent,
    List<CategoryDto> childs
) {
  public CategoryDto {
  }

  public static CategoryDto response(Category category) {
    return new CategoryDto(category.getId(), category.getName(), category.getDepth(), category.getImg(), category.getParent(), null);
  }

  public static CategoryDto childsResponse(Category category, List<CategoryDto> childs) {
    return new CategoryDto(category.getId(), category.getName(), category.getDepth(), category.getImg(), null, childs);
  }
}
