package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@RestController
@RequestMapping("/region")
@Slf4j
public class RegionController {

  private final RegionService regionService;


  @GetMapping(path = "/name")
  public DataResponse<Object> getByParentId(@RequestParam(value = "parentId", required = false) Long parentId) {
    return DataResponse.of(regionService.getByParentId(parentId));
  }


  @GetMapping(path = "/name", params = "depth")
  public DataResponse<Object> getByDepth(@RequestParam(value = "depth", required = false) Long depth) {
    return DataResponse.of(regionService.getByDepth(depth));
  }

  @GetMapping("")
  public DataResponse<Object> getAll() {
    return DataResponse.of(regionService.getAllInterest());
  }


  @GetMapping("/me")
  public DataResponse<Object> getMyRegions() {
    return DataResponse.of(regionService.getMyInterests());
  }


  @GetMapping("/my-default")
  public DataResponse<Object> getMyDefaultRegion() {
    return DataResponse.of(regionService.getMyDefault());
  }


  @PostMapping("/me")
  public DataResponse<Object> insertMyRegions(@RequestBody List<InterestRegionDto> body) {
    return DataResponse.of(regionService.insertMyInterests(body));
  }


  @PutMapping("/my-default")
  public DataResponse<Object> upsertMyDefaultRegion(@RequestBody InterestRegionDto body) {
    return DataResponse.of(regionService.updateMyDefault(body));
  }


  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(@PathVariable Long id) {
    return DataResponse.of(regionService.delete(id));
  }


  @DeleteMapping("")
  public DataResponse<Object> deleteByParams(@RequestParam(name="id") List<Long> ids) {
    return DataResponse.of(regionService.deleteByParams(ids));
  }
}
