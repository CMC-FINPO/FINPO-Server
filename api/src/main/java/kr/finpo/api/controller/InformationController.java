package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.repository.InformationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;


@RequiredArgsConstructor
@RestController
@RequestMapping("/information")
public class InformationController {

  private final InformationRepository informationRepository;

  @GetMapping("/{key}")
  public DataResponse<Object> get(@PathVariable String key) {
    return DataResponse.of(informationRepository.findByTypeAndHidden(key, false));
  }
}
