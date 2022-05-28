package kr.finpo.api.service;


import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.RegionDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.RegionRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class RegionService {

  private final RegionRepository regionRepository;
  private final UserRepository userRepository;

  public List<RegionDto> getAll() {
    try {
      return StreamSupport.stream(regionRepository.findAll().spliterator(), false).map(RegionDto::result).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<RegionDto> getById(Long id) {
    try {
      return regionRepository.findById(id).map(RegionDto::result);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public List<RegionDto> insertRegions(List<RegionDto> dtos) {
    try {
      ArrayList<RegionDto> res = new ArrayList<RegionDto>();
      User user = userRepository.findById(SecurityUtil.getCurrentUserId()).get();

      dtos.stream().forEach(dto -> {
        if (regionRepository.findOneByUserIdAndRegion1AndRegion2(SecurityUtil.getCurrentUserId(), dto.region1(), dto.region2()).isPresent())
          return;
        Region region = dto.toEntity();
        region.setUser(user);
        region = regionRepository.save(region);
        res.add(RegionDto.result(region));
      });

      return res;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Boolean delete(Long id) {
    try {
      Region region = regionRepository.findById(id).get();

      if (region.getUser().getId() != SecurityUtil.getCurrentUserId())
        throw new GeneralException(ErrorCode.USER_NOT_EQUAL);

      if (region.getIsDefault())
        throw new GeneralException(ErrorCode.BAD_REQUEST, "You cannot delete default region");

      regionRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean deleteByParams(List<Long> ids) {
    try {
      ids.stream().forEach(id->{
        delete(id);
      });
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public Optional<RegionDto> getMyDefaultRegion() {
    try {
      return regionRepository.findOneByUserIdAndIsDefault(SecurityUtil.getCurrentUserId(), true).map(RegionDto::result);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public List<RegionDto> getMyRegions() {
    try {
      return StreamSupport.stream(regionRepository.findByUserId(SecurityUtil.getCurrentUserId()).spliterator(), false).map(RegionDto::result).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }


  public RegionDto upsertMyDefaultRegion(RegionDto dto) {
    try {
      Region region = regionRepository.findOneByUserIdAndIsDefault(SecurityUtil.getCurrentUserId(), true).orElse(Region.of());
      region.setRegion1(dto.region1());
      region.setRegion2(dto.region2());
      region.setIsDefault(true);
      return RegionDto.result(regionRepository.save(region));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

