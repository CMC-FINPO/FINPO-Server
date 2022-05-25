package kr.finpo.api.service;


import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.dto.UserDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.StreamSupport;

@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class UserService {

  private final UserRepository userRepository;


  public List<UserDto> getAll() {
    try {
      return StreamSupport.stream(userRepository.findAll().spliterator(), false).map(UserDto::info).toList();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Optional<UserDto> getById(Long id) {
    try {
      return userRepository.findById(id).map(UserDto::info);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Optional<UserDto> getMyInfo() {
    try {
      return userRepository.findById(SecurityUtil.getCurrentUserId()).map(UserDto::info);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public UserDto update(Long id, UserDto dto) {
    try {
      return UserDto.info(userRepository.save(dto.updateEntity(userRepository.findById(id).get())));
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean delete(Long id) {
    try {
      userRepository.deleteById(id);
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

