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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class RegionService {

  public static final List<String> seoul = Arrays.asList("종로", "중구", "용산", "성동", "광진", "동대문", "중랑", "성북", "강북", "도봉", "노원", "은평", "서대문", "마포", "양천", "강서", "구로", "금천", "영등포", "동작", "관악", "서초", "강남", "송파", "강동");

  public static final List<String> gyeonggi = Arrays.asList("수원", "성남", "의정부", "안양", "부천", "광명", "평택", "동두천", "안산", "고양", "과천", "구리", "남양주", "오산", "시흥", "군포", "의왕", "하남", "용인", "파주", "이천", "안성", "김포", "화성", "광주", "양주", "포천", "여주", "가평", "연천", "양평");

  public static final List<String> busan = Arrays.asList("중구", "서구", "동구", "영도", "부산진", "동래구", "남구", "북구", "해운대", "사하", "금정", "강서", "연제", "수영", "사상", "기장");

  public static final Long REGION2_MAX = 100L;

  public static final Map<String, List<String>> regions2 = new LinkedHashMap<>() {{
    put("서울", seoul);
    put("경기", gyeonggi);
    put("부산", busan);
    put("대구", null);
    put("인천", null);
    put("광주", null);
    put("대전", null);
    put("울산", null);
    put("충북", null);
    put("충남", null);
    put("전북", null);
    put("전남", null);
    put("경북", null);
    put("경남", null);
    put("제주", null);
    put("세종", null);
  }};

  public static final List<String> regions1 = new ArrayList<>(regions2.keySet());

  public static Long name2regionId(String region1, String region2) {
    return regions1.indexOf(region1) * REGION2_MAX + (StringUtils.isNullOrEmpty(region2) ? 0 : (regions2.get(region1).indexOf(region2) + 1));
  }

  public User getMe() {
    return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
        () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
    );
  }

  public void authorizeMe(Long id) {
    if (!id.equals(SecurityUtil.getCurrentUserId()))
      throw new GeneralException(ErrorCode.USER_NOT_EQUAL);
  }

  public void initialize() {
    for (int i = 0; i < regions1.size(); i++) {
      Boolean status = regions2.get(regions1.get(i)) != null;
      Region parent = regionRepository.save(Region.of(i * REGION2_MAX, regions1.get(i), 1L, status));

      if (!status) continue;

      for (int j = 0; j < regions2.get(regions1.get(i)).size(); j++) {
        Region region = Region.of(i * REGION2_MAX + j + 1, regions2.get(regions1.get(i)).get(j), 2L, true);
        region.setParent(parent);
        regionRepository.save(region);
      }
    }
  }

  private final InterestRegionRepository interestRegionRepository;
  private final RegionRepository regionRepository;
  private final UserRepository userRepository;

  public List<Region> getAll() {
    try {
      return regionRepository.findAll();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public List<Region> getByParentId(Long parentId) {
    try {
      return regionRepository.findByParentIdOrderByNameAsc(parentId);
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
      if (dtos.size() > 5)
        throw new GeneralException(ErrorCode.BAD_REQUEST, "Interest region should be less than or equal to 5");

      ArrayList<InterestRegionDto> res = new ArrayList<>();
      User user = getMe();

      dtos.forEach(dto -> {
        // 중복 지역 넘기기
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


  public List<InterestRegionDto> updateMyInterests(List<InterestRegionDto> dtos) {
    try {
      User user = getMe();
      interestRegionRepository.deleteByUserIdAndIsDefault(user.getId(), false);
      return insertMyInterests(dtos);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteMyInterest(Long id) {
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

  public Boolean deleteMyInterestByParams(List<Long> ids) {
    try {
      ids.forEach(this::deleteMyInterest);
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
      return interestRegionRepository.findByUserId(SecurityUtil.getCurrentUserId()).stream().map(InterestRegionDto::response).toList();
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

      // delete interest if new region is interest
      interestRegionRepository.findOneByUserIdAndRegionIdAndIsDefault(SecurityUtil.getCurrentUserId(), dto.regionId(), false)
          .ifPresent(interestRegion -> {
            interestRegionRepository.deleteById(interestRegion.getId());
          });

      InterestRegion defaultRegion = interestRegionRepository.findOneByUserIdAndIsDefault(SecurityUtil.getCurrentUserId(), true).get();
      defaultRegion.updateDefault(newRegion);
      User user = getMe();
      user.setDefaultRegion(defaultRegion);

      return InterestRegionDto.response(interestRegionRepository.save(defaultRegion));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

