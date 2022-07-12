package kr.finpo.api.config;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.service.CategoryService;
import kr.finpo.api.service.RegionService;
import kr.finpo.api.service.ReportService;
import kr.finpo.api.service.openapi.GgdataService;
import kr.finpo.api.service.openapi.YouthcenterService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

@Configuration
@RequiredArgsConstructor
public class DbConfig {

  private final GgdataService ggdataService;
  private final YouthcenterService youthcenterService;
  private final RegionService regionService;
  private final CategoryService categoryService;
  private final ReportService reportService;

  @PostConstruct
  public void initialize() {
    try {
      regionService.initialize();
      categoryService.initialize();
      reportService.initialize();
      ggdataService.initialize();
      youthcenterService.initialize();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  @PostConstruct
  void started() {
    TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
  }
}