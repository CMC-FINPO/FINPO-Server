package kr.finpo.api.controller;

import kr.finpo.api.constant.RegionConstant;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.RegionDto;
import kr.finpo.api.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RequiredArgsConstructor
@RestController
@RequestMapping("/region")
@Slf4j
public class RegionController {

  private final RegionService regionService;
  private final RegionConstant region = new RegionConstant();


  @GetMapping(path = "/name", params = "region1")
  public DataResponse<Object> getRegion2(@RequestParam String region1) {
    return DataResponse.of(region.regions2.get(region1));
  }


  @GetMapping("/name")
  public DataResponse<Object> getRegion1() {
    return DataResponse.of(region.regions1);
  }


  @GetMapping("")
  public DataResponse<Object> getAll() {
    return DataResponse.of(regionService.getAll());
  }

  @GetMapping("/me")
  public DataResponse<Object> getMyRegions() {
    return DataResponse.of(regionService.getMyRegions());
  }


  @GetMapping("/my-default")
  public DataResponse<Object> getMyDefaultRegion() {
    return DataResponse.of(regionService.getMyDefaultRegion());
  }

  @PostMapping("/me")
  public DataResponse<Object> insertRegion(@RequestBody RegionDto body) {
    return DataResponse.of(regionService.insertRegion(body));
  }

  @PostMapping("/my-default")
  public DataResponse<Object> upsertMyDefaultRegion(@RequestBody RegionDto body) {
    return DataResponse.of(regionService.upsertMyDefaultRegion(body));
  }

  @DeleteMapping("/{id}")
  public DataResponse<Object> delete(@PathVariable Long id) {
    return DataResponse.of(regionService.delete(id));
  }
}
