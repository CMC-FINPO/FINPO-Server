package kr.finpo.api.controller;

import kr.finpo.api.constant.Region;
import kr.finpo.api.dto.DataResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/region")
public class RegionController {

  private final Region region = new Region();

  public RegionController() {
  }

  @GetMapping(path = "", params = "region1")
  public DataResponse<Object> getRegion2(@RequestParam String region1) {
    return DataResponse.of(region.regions2.get(region1));
  }

  @GetMapping("")
  public DataResponse<Object> getRegion1() {
    return DataResponse.of(region.regions1);
  }
}
