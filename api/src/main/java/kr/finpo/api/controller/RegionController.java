package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.service.RegionService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/region")
public class RegionController {

  private final RegionService regionService;

  @GetMapping(path = "/name")
  public DataResponse<Object> getByDepth(@RequestParam(value = "depth", required = false) Long depth) {
    return DataResponse.of(regionService.getByDepth(depth == null ? 1L : depth));
  }

  @GetMapping(path = "/name", params = "parentId")
  public DataResponse<Object> getByParentId(@RequestParam(value = "parentId", required = false) Long parentId) {
    return DataResponse.of(regionService.getByParentId(parentId));
  }

  @GetMapping(path = "/name/all")
  public DataResponse<Object> getAll() {
    return DataResponse.of(regionService.getAll());
  }

  @GetMapping("/me")
  public DataResponse<Object> getMy() {
    return DataResponse.of(regionService.getMyInterests());
  }

  @GetMapping("/my-default")
  public DataResponse<Object> getMyDefault() {
    return DataResponse.of(regionService.getMyDefault());
  }

  @PostMapping("/me")
  public DataResponse<Object> insertMyInterests(@RequestBody List<InterestRegionDto> body) {
    return DataResponse.of(regionService.insertMyInterests(body));
  }

  @PutMapping("/me")
  public DataResponse<Object> updateMyInterests(@RequestBody List<InterestRegionDto> body) {
    return DataResponse.of(regionService.updateMyInterests(body));
  }

  @PutMapping("/my-default")
  public DataResponse<Object> updateMyDefault(@RequestBody InterestRegionDto body) {
    return DataResponse.of(regionService.updateMyDefault(body));
  }

  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(@PathVariable Long id) {
    return DataResponse.of(regionService.deleteMyInterest(id));
  }

  @DeleteMapping("")
  public DataResponse<Object> deleteByParams(@RequestParam(name = "id") List<Long> ids) {
    return DataResponse.of(regionService.deleteMyInterestByParams(ids));
  }
}
