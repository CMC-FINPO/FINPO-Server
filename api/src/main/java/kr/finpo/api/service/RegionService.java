package kr.finpo.api.service;


import com.amazonaws.util.StringUtils;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.InterestRegion;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.InterestRegionDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.InterestRegionRepository;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class RegionService {

  public static final List<String> seoul = Arrays.asList("종로", "중구", "용산", "성동", "광진", "동대문", "중랑", "성북", "강북", "도봉", "노원", "은평", "서대문", "마포", "양천", "강서", "구로", "금천", "영등포", "동작", "관악", "서초", "강남", "송파", "강동");

  public static final List<String> gyeonggi = Arrays.asList("수원", "성남", "의정부", "안양", "부천", "광명", "평택", "동두천", "안산", "고양", "과천", "구리", "남양주", "오산", "시흥", "군포", "의왕", "하남", "용인", "파주", "이천", "안성", "김포", "화성", "광주", "양주", "포천", "여주", "가평", "연천", "양평");

  public static final List<String> busan = Arrays.asList("중구", "서구", "동구", "영도", "부산진", "동래구", "남구", "북구", "해운대", "사하", "금정", "강서", "연제", "수영", "사상", "기장");

  public static final Long REGION2_MAX = 100L;

  public static final Map<String, List<String>> regions2 = new HashMap<>() {{
    put("서울", seoul);
    put("경기", gyeonggi);
    put("부산", busan);
  }};

  public static final List<String> regions1 = new ArrayList<>(regions2.keySet());

  public static final Long name2regionId (String region1, String region2){
      return regions1.indexOf(region1) * REGION2_MAX + (StringUtils.isNullOrEmpty(region2) ? 0 : (regions2.get(region1).indexOf(region2) + 1));
  }

  public void initialize() {
    for (int i = 0; i < regions1.size(); i++) {
      Region parent = regionRepository.save(Region.of( i * REGION2_MAX, regions1.get(i), 1L));
      for (int j = 0; j < regions2.get(regions1.get(i)).size(); j++) {
        Region region = Region.of(i * REGION2_MAX + j + 1, regions2.get(regions1.get(i)).get(j), 2L);
        region.setParent(parent);
        regionRepository.save(region);
      }
    }
  }


  private final InterestRegionRepository interestRegionRepository;
  private final RegionRepository regionRepository;
  private final UserRepository userRepository;

  public List<InterestRegionDto> getAllInterest() {
    try {
      return StreamSupport.stream(interestRegionRepository.findAll().spliterator(), false).map(InterestRegionDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<Region> getByParentId(Long parentId) {
    try {
      return regionRepository.findByParentId(parentId);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<Region> getByDepth(Long depth) {
    try {
      return regionRepository.findByDepth(depth);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Optional<Region> getById(Long id) {
    try {
      return regionRepository.findById(id);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.BAD_REQUEST, "id not valid");
    }
  }


  public List<InterestRegionDto> insertMyInterests(List<InterestRegionDto> dtos) {
    try {
      ArrayList<InterestRegionDto> res = new ArrayList<InterestRegionDto>();
      User user = userRepository.findById(SecurityUtil.getCurrentUserId()).get();

      dtos.stream().forEach(dto -> {
        log.debug("삽입할 지역" + " " + dto.regionId());

        if (interestRegionRepository.findOneByUserIdAndRegionId(SecurityUtil.getCurrentUserId(), dto.regionId()).isPresent())
          return;

        Region region = regionRepository.findById(dto.regionId()).orElseThrow(
            () -> new GeneralException(ErrorCode.BAD_REQUEST, "region id not valid")
        );

        InterestRegion interestRegion = interestRegionRepository.save(InterestRegion.of(user, region));
        res.add(InterestRegionDto.response(interestRegion));
      });

      return res;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Boolean delete(Long id) {
    try {
      InterestRegion region = interestRegionRepository.findById(id).get();

      if (!region.getUser().getId().equals(SecurityUtil.getCurrentUserId()))
        throw new GeneralException(ErrorCode.USER_NOT_EQUAL);

      if (region.getIsDefault())
        throw new GeneralException(ErrorCode.BAD_REQUEST, "You cannot delete default region");

      interestRegionRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteByParams(List<Long> ids) {
    try {
      ids.stream().forEach(id -> {
        delete(id);
      });
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<InterestRegionDto> getMyDefault() {
    try {
      return interestRegionRepository.findOneByUserIdAndIsDefault(SecurityUtil.getCurrentUserId(), true).map(InterestRegionDto::response);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public List<InterestRegionDto> getMyInterests() {
    try {
      return StreamSupport.stream(interestRegionRepository.findByUserId(SecurityUtil.getCurrentUserId()).spliterator(), false).map(InterestRegionDto::response).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public InterestRegionDto updateMyDefault(InterestRegionDto dto) {
    try {
      // region check
      Region newRegion = regionRepository.findById(dto.regionId()).orElseThrow(
          () -> new GeneralException(ErrorCode.BAD_REQUEST, "region id not valid")
      );
      InterestRegion region = interestRegionRepository.findOneByUserIdAndIsDefault(SecurityUtil.getCurrentUserId(), true).get();
      region.updateDefault(newRegion);
      return InterestRegionDto.response(interestRegionRepository.save(region));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

