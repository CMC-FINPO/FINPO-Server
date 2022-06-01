package kr.finpo.api.controller;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.PolicyRepository;
import kr.finpo.api.service.CategoryService;
import kr.finpo.api.service.RegionService;
import kr.finpo.api.service.S3Uploader;
import kr.finpo.api.service.openapi.GgdataService;
import kr.finpo.api.service.openapi.YouthcenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class Initalizer {

  private final GgdataService ggdataService;
  private final YouthcenterService youthcenterService;
  private final RegionService regionService;
  private final CategoryService categoryService;

//  @EventListener(ApplicationReadyEvent.class)
  public void initialize() {
    try {
      regionService.initialize();
      categoryService.initialize();
      ggdataService.initialize();
      youthcenterService.initialize();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

}
