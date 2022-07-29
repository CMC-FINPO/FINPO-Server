package kr.finpo.api.service.openapi;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Category;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.Region;
import kr.finpo.api.dto.YouthcenterDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CategoryRepository;
import kr.finpo.api.repository.PolicyRepository;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.service.RegionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class YouthcenterService {

    private class Pair {

        String a, b;

        public Pair(String a, String b) {
            this.a = a;
            this.b = b;
        }
    }

    private final Map<String, Pair> regionName = new HashMap<String, Pair>() {{
        put("003002001001", new Pair("서울", "종로"));
        put("003002001002", new Pair("서울", "중구"));
        put("003002001003", new Pair("서울", "용산"));
        put("003002001004", new Pair("서울", "성동"));
        put("003002001005", new Pair("서울", "광진"));
        put("003002001006", new Pair("서울", "동대문"));
        put("003002001007", new Pair("서울", "중랑"));
        put("003002001008", new Pair("서울", "성북"));
        put("003002001009", new Pair("서울", "강북"));
        put("003002001010", new Pair("서울", "도봉"));
        put("003002001011", new Pair("서울", "노원"));
        put("003002001012", new Pair("서울", "은평"));
        put("003002001013", new Pair("서울", "서대문"));
        put("003002001014", new Pair("서울", "마포"));
        put("003002001015", new Pair("서울", "양천"));
        put("003002001016", new Pair("서울", "강서"));
        put("003002001017", new Pair("서울", "구로"));
        put("003002001018", new Pair("서울", "금천"));
        put("003002001019", new Pair("서울", "영등포"));
        put("003002001020", new Pair("서울", "동작"));
        put("003002001021", new Pair("서울", "관악"));
        put("003002001022", new Pair("서울", "서초"));
        put("003002001023", new Pair("서울", "강남"));
        put("003002001024", new Pair("서울", "송파"));
        put("003002001025", new Pair("서울", "강동"));

        put("003002002001", new Pair("부산", "중구"));
        put("003002002002", new Pair("부산", "서구"));
        put("003002002003", new Pair("부산", "동구"));
        put("003002002004", new Pair("부산", "영도"));
        put("003002002005", new Pair("부산", "부산진"));
        put("003002002006", new Pair("부산", "동래"));
        put("003002002007", new Pair("부산", "남구"));
        put("003002002008", new Pair("부산", "북구"));
        put("003002002009", new Pair("부산", "해운대"));
        put("003002002010", new Pair("부산", "사하"));
        put("003002002011", new Pair("부산", "금정"));
        put("003002002012", new Pair("부산", "강서"));
        put("003002002013", new Pair("부산", "연제"));
        put("003002002014", new Pair("부산", "수영"));
        put("003002002015", new Pair("부산", "사상"));
        put("003002002016", new Pair("부산", "기장"));

        put("003002008001", new Pair("경기", "수원"));
        put("003002008002", new Pair("경기", "성남"));
        put("003002008003", new Pair("경기", "의정부"));
        put("003002008004", new Pair("경기", "안양"));
        put("003002008005", new Pair("경기", "부천"));
        put("003002008006", new Pair("경기", "광명"));
        put("003002008007", new Pair("경기", "평택"));
        put("003002008008", new Pair("경기", "동두천"));
        put("003002008009", new Pair("경기", "안산"));
        put("003002008010", new Pair("경기", "고양"));
        put("003002008011", new Pair("경기", "과천"));
        put("003002008012", new Pair("경기", "구리"));
        put("003002008013", new Pair("경기", "남양주"));
        put("003002008014", new Pair("경기", "오산"));
        put("003002008015", new Pair("경기", "시흥"));
        put("003002008016", new Pair("경기", "군포"));
        put("003002008017", new Pair("경기", "의왕"));
        put("003002008018", new Pair("경기", "하남"));
        put("003002008019", new Pair("경기", "용인"));
        put("003002008020", new Pair("경기", "파주"));
        put("003002008021", new Pair("경기", "이천"));
        put("003002008022", new Pair("경기", "안성"));
        put("003002008023", new Pair("경기", "김포"));
        put("003002008024", new Pair("경기", "화성"));
        put("003002008025", new Pair("경기", "광주"));
        put("003002008026", new Pair("경기", "양주"));
        put("003002008027", new Pair("경기", "포천"));
        put("003002008028", new Pair("경기", "여주"));
        put("003002008029", new Pair("경기", "양주"));
        put("003002008030", new Pair("경기", "여주"));
        put("003002008031", new Pair("경기", "연천"));
        put("003002008032", new Pair("경기", "포천"));
        put("003002008033", new Pair("경기", "가평"));
        put("003002008034", new Pair("경기", "양평"));
    }};

    private final Map<String, Long> categoryName = new HashMap<String, Long>() {{
        put("004001001", 6L);
        put("004001002", 6L);
        put("004001003", 6L);
        put("004001004", 6L);

        put("004002001", 7L);
        put("004002002", 7L);
        put("004002003", 7L);

        put("004003001", 8L);
        put("004003003", 8L);

        put("004004001", 9L);
        put("004004002", 11L);

        put("004005001", 12L);
        put("004005002", 12L);
        put("004005003", 12L);

        put("004006001", 8L);
        put("004006002", 8L);
        put("004006003", 8L);
        put("004006004", 8L);
        put("004006005", 8L);
        put("004006006", 9L);
    }};

    private final Map<Long, List<String>> categories = new HashMap() {{
        put(6L, new ArrayList() {{
            add("004001001");
            add("004001002");
            add("004001003");
            add("004001004");
        }});

        put(7L, new ArrayList() {{
            add("004002001");
            add("004002002");
            add("004002003");
        }});

        put(8L, new ArrayList() {{
            add("004003001");
            add("004003003");
            add("004006001");
            add("004006002");
            add("004006003");
            add("004006004");
            add("004006005");
        }});

        put(9L, new ArrayList() {{
            add("004004001");
            add("004006006");
        }});

        put(11L, new ArrayList() {{
            add("004004002");
        }});

        put(12L, new ArrayList() {{
            add("004005001");
            add("004005002");
            add("004005003");
        }});
    }};

    private final List<Long> categoryList = new ArrayList<>(categories.keySet());


    private final PolicyRepository policyRepository;
    private final CategoryRepository categoryRepository;
    private final RegionRepository regionRepository;

    @Value("${youthcenter.key}")
    private String apiKey;
    @Value("${youthcenter.url}")
    private String url;


    // 10시, 15시, 19시마다 업데이트
    @Scheduled(cron = "0 0 10,15,19 * * *", zone = "Asia/Seoul")
    public void initialize() {
        initialize(true);
    }

    public void initialize(Boolean isAuto) {
        try {
            log.info("youthcenter batch start");
            Integer cnt = 0;

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            for (Long categoryId : categoryList) {
                log.debug("category " + categoryId + "start");
                Boolean endFlag = false;

                StringBuilder bizTycdSelSb = new StringBuilder();

                categories.get(categoryId).forEach(category -> {
                    bizTycdSelSb.append(category);
                    bizTycdSelSb.append(",");
                });
                log.debug("str: " + bizTycdSelSb);

                for (int i = 1; ; i++) {
                    UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(url)
                        .queryParam("openApiVlak", apiKey)
                        .queryParam("pageIndex", i)
                        .queryParam("display", "100")
                        .queryParam("srchPolyBizSecd", "003002001,003002002,003002008")
                        .queryParam("bizTycdSel", bizTycdSelSb);

                    ResponseEntity<String> response = new RestTemplate().exchange(
                        builder.buildAndExpand().toUri(),
                        HttpMethod.GET,
                        new HttpEntity<>(null, headers),
                        String.class
                    );

                    JAXBContext jaxbContext = JAXBContext.newInstance(YouthcenterDto.class);
                    Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
                    YouthcenterDto dto;
                    try {
                        dto = (YouthcenterDto) unmarshaller.unmarshal(
                            new ByteArrayInputStream(response.getBody().getBytes()));
                    } catch (Exception e) {
                        continue;
                    }

                    if (dto.getEmp() == null) {
                        break;
                    }

                    for (YouthcenterDto.Emp row : dto.getEmp()) {
                        log.debug(row.toString());

                        Policy policy = row.toEntity();
                        if (policyRepository.findOneByPolicyKey(policy.getPolicyKey()).isPresent()) {
                            endFlag = true;
                            log.debug("category " + categoryId + "end");
                            break;
                        }

                        try {
                            Region region = regionRepository.findById(
                                RegionService.name2regionId(regionName.get(row.getPolyBizSecd()).a,
                                    regionName.get(row.getPolyBizSecd()).b)).get();
                            policy.setRegion(region);

                            Category category = categoryRepository.findById(categoryId).get();
                            policy.setCategory(category);
                        } catch (Exception e) {
                            continue;
                        }

                        if (isAuto) {
                            policy.setStatus(false);
                        }
                        policyRepository.save(policy);
                        cnt++;
                    }
                    if (endFlag) {
                        break;
                    }
                }
            }
            log.info(cnt + " policy added");
            log.info("youthcenter batch end");
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }


}

