package kr.finpo.api.service;

import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.*;
import kr.finpo.api.dto.CommunityReportDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.*;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class ReportService {

  private final ReportRepository reportRepository;
  private final CommunityReportRepository communityReportRepository;
  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;

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
    List<String> reasons = Arrays.asList("낚시/놀람/도배", "정당/정치인 비하 및 선거운동", "유출/사칭/사기", "게시판 성격에 부적절함", "음란물/불건전한 만남 및 대화", "상업적 광고 및 판매", "욕설/비하");

    Long id = 0L;
    for (String reason : reasons) reportRepository.save(Report.of(++id, reason));
  }

  public List<Report> getAll() {
    try {
      return reportRepository.findAll();
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Page<CommunityReportDto> getCommunity(Pageable pageable) {
    try {
      return communityReportRepository.findAll(pageable).map(CommunityReportDto::response);
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean insertComment(Long id, CommunityReportDto dto) {
    try {
      Report report = reportRepository.findById(dto.report().getId()).get();
      Comment comment = commentRepository.findById(id).get();
      communityReportRepository.save(CommunityReport.of(report, comment, getMe()));
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }

  public Boolean insertPost(Long id, CommunityReportDto dto) {
    try {
      Report report = reportRepository.findById(dto.report().getId()).get();
      Post post = postRepository.findById(id).get();
      communityReportRepository.save(CommunityReport.of(report, post, getMe()));
      return true;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

