package kr.finpo.api.service.openapi;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.Region;
import kr.finpo.api.dto.GgdataDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.PolicyRepository;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.service.FcmService;
import kr.finpo.api.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class GgdataService {

  Map<String, String> regionName = new HashMap<String, String>() {{
    put("4182", "가평");
    put("4100", "경기");
    put("4128", "고양");
    put("4129", "과천");
    put("4121", "광명");
    put("4161", "광주");
    put("4131", "구리");
    put("4141", "군포");
    put("4157", "김포");
    put("4136", "남양주");
    put("4125", "동두천");
    put("4119", "부천");
    put("4113", "성남");
    put("4111", "수원");
    put("4139", "시흥");
    put("4127", "안산");
    put("4155", "안성");
    put("4117", "안양");
    put("4163", "양주");
    put("4183", "양평");
    put("4167", "여주");
    put("4180", "연천");
    put("4137", "오산");
    put("4146", "용인");
    put("4143", "의왕");
    put("4115", "의정부");
    put("4150", "이천");
    put("4148", "파주");
    put("4122", "평택");
    put("4165", "포천");
    put("4145", "하남");
    put("4159", "화성");
  }};
  Map<String, Long> categoryName = new HashMap<String, Long>() {{
    put("01", 6L); // 구직활동
    put("07", 6L); // 기업지원
    put("04", 8L); // 생활지원
    put("06", 6L); // 재직지원
  }};

  private final PolicyRepository policyRepository;
  private final CategoryRepository categoryRepository;
  private final RegionRepository regionRepository;

  @Value("${ggdata.key}")
  private String apiKey;
  @Value("${ggdata.url}")
  private String url;

  // 10시, 15시, 19시마다 업데이트
  @Scheduled(cron = "0 0 10,15,19 * * *")
  public void initialize() {
    initialize(true);
  }

  public void initialize(Boolean isAuto) {
    try {
      log.debug("batch start");
      HttpHeaders headers = new HttpHeaders();
      headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

      for (int i = 1; ; i++) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
            .queryParam("Type", "json")
            .queryParam("KEY", apiKey)
            .queryParam("pIndex", i)
            .queryParam("pSize", "100");

        ResponseEntity<String> response = new RestTemplate().exchange(
            builder.buildAndExpand().toUri(),
            HttpMethod.POST,
            new HttpEntity<>(null, headers),
            String.class
        );
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        JsonNode jsonNode = objectMapper.readTree(response.getBody());
        GgdataDto dto = objectMapper.treeToValue(jsonNode, GgdataDto.class);
        log.debug(dto.toString());

        if (dto.JobFndtnSportPolocy() == null) break;

        for (GgdataDto.JobFndtnSportPolocyWrapper.Row row : dto.JobFndtnSportPolocy().get(1).row()) {
          Policy policy = row.toEntity();
          if (categoryName.get(row.DIV_CD()) == null) continue;
          if (policyRepository.findOneByPolicyKey(policy.getPolicyKey()).isPresent()) {
            log.debug("batch end");
            return;
          }

          try {
            Region region = regionRepository.findById(RegionService.name2regionId("경기", regionName.get(row.REGION_CD()))).get();
            policy.setRegion(region);
          } catch (Exception e) {
            log.debug(e.toString());
            continue;
          }

          categoryRepository.findById(categoryName.get(row.DIV_CD())).ifPresent(policy::setCategory);
          if(isAuto) policy.setStatus(false);
          policyRepository.save(policy);
        }
      }
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


}

