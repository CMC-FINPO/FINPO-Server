package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.InterestCategoryDto;
import kr.finpo.api.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/policy/category")
public class CategoryController {

  private final CategoryService categoryService;

  @GetMapping("/name")
  public DataResponse<Object> getByParentId(@RequestParam(value = "parentId", required = false) Long parentId) {
    return DataResponse.of(categoryService.getByParentId(parentId));
  }

  @GetMapping(path = "/name", params = "depth")
  public DataResponse<Object> getByDepth(@RequestParam("depth") Long depth) {
    return DataResponse.of(categoryService.getByDepth(depth));
  }

  @GetMapping(path = "/name/child-format")
  public DataResponse<Object> getAllByChildFormat() {
    return DataResponse.of(categoryService.getAllByChildFormat());
  }

  @GetMapping("/me/parent")
  public DataResponse<Object> getMyByDepth() {
    return DataResponse.of(categoryService.getMyInterestsByDepth());
  }

  @GetMapping("/me")
  public DataResponse<Object> getMy() {
    return DataResponse.of(categoryService.getMyInterests());
  }

  @GetMapping("/{id}")
  public DataResponse<Object> getById(@PathVariable("id") Long id) {
    return DataResponse.of(categoryService.getById(id));
  }

  @PostMapping("/me")
  public DataResponse<Object> insertMyInterests(@RequestBody List<InterestCategoryDto> body) {
    return DataResponse.of(categoryService.insertInterests(body));
  }

  @PutMapping("/me")
  public DataResponse<Object> updateMyInterests(@RequestBody List<InterestCategoryDto> body) {
    return DataResponse.of(categoryService.updateMyInterests(body));
  }

  @DeleteMapping("")
  public DataResponse<Object> deleteByParams(@RequestParam(name = "id") List<Long> ids) {
    return DataResponse.of(categoryService.deleteByParams(ids));
  }
}

