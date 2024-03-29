package kr.finpo.api.service;


import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.Role;
import kr.finpo.api.domain.BannedUser;
import kr.finpo.api.domain.Report;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.BannedUserDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.BannedUserRepository;
import kr.finpo.api.repository.ReportRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class BannedUserService {

    private final UserRepository userRepository;
    private final BannedUserRepository bannedUserRepository;
    private final ReportRepository reportRepository;

    public Page<BannedUserDto> getAll(Pageable pageable) {
        try {
            return bannedUserRepository.findByReleaseDateGreaterThan(LocalDate.now(), pageable)
                .map(BannedUserDto::response);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public List<BannedUserDto> getMe() {
        return getByUserId(SecurityUtil.getCurrentUserId());
    }

    public List<BannedUserDto> getByUserId(Long userId) {
        try {
            return bannedUserRepository.findByUserIdOrderByIdDesc(userId).stream().map(BannedUserDto::response)
                .toList();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public BannedUserDto insert(BannedUserDto dto) {
        try {
            if (userRepository.findById(dto.user().id()).get().getRole().equals(Role.ROLE_BANNED_USER)) {
                throw new GeneralException(ErrorCode.BAD_REQUEST, "User already banned");
            }

            User user = userRepository.findById(dto.user().id()).get();
            user.setRole(Role.ROLE_BANNED_USER);
            Report report = reportRepository.findById(dto.report().getId()).get();
            BannedUser bannedUser = BannedUser.of(dto.releaseDate(), dto.detail(), user, report);

            return BannedUserDto.response(bannedUserRepository.save(bannedUser));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public BannedUserDto update(Long id, BannedUserDto dto, Boolean releaseNow) {
        try {
            BannedUser bannedUser = dto.updateEntity(bannedUserRepository.findById(id).get());
            Optional.ofNullable(dto.report()).ifPresent(report ->
                bannedUser.setReport(reportRepository.findById(report.getId()).get())
            );
            if (releaseNow) {
                bannedUser.setReleaseDate(LocalDate.now(ZoneId.of("Asia/Seoul")));
                User user = userRepository.findById(bannedUser.getUser().getId()).get();
                user.setRole(Role.ROLE_USER);
            }
            return BannedUserDto.response(bannedUserRepository.save(bannedUser));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    @Scheduled(cron = "0 0 0 * * *", zone = "Asia/Seoul")
    public void releaseUser() {
        log.info("user releasing start");
        bannedUserRepository.findByReleaseDate(LocalDate.now()).forEach(bannedUser -> {
            log.info("user " + bannedUser.getUser().getId() + " " + bannedUser.getUser().getNickname() + " free");
            User user = bannedUser.getUser();
            user.setRole(Role.ROLE_USER);
            userRepository.save(user);
        });
        log.info("user releasing finished");
    }
}

