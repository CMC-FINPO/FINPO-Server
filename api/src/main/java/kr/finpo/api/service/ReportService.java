package kr.finpo.api.service;

import java.util.Arrays;
import java.util.List;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.Comment;
import kr.finpo.api.domain.CommunityReport;
import kr.finpo.api.domain.Post;
import kr.finpo.api.domain.Report;
import kr.finpo.api.domain.User;
import kr.finpo.api.dto.CommunityReportDto;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.CommentRepository;
import kr.finpo.api.repository.CommunityReportRepository;
import kr.finpo.api.repository.PostRepository;
import kr.finpo.api.repository.ReportRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


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

    private User getMe() {
        return userRepository.findById(SecurityUtil.getCurrentUserId()).orElseThrow(
            () -> new GeneralException(ErrorCode.USER_UNAUTHORIZED)
        );
    }

    public void initialize() {
        List<String> reasons = Arrays.asList("낚시/놀람/도배", "정당/정치인 비하 및 선거운동", "유출/사칭/사기", "게시판 성격에 부적절함",
            "음란물/불건전한 만남 및 대화", "상업적 광고 및 판매", "욕설/비하");

        Long id = 0L;
        for (String reason : reasons) {
            reportRepository.save(Report.of(++id, reason));
        }
    }

    public List<Report> getAll() {
        try {
            return reportRepository.findAll();
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    public Page<CommunityReportDto> getCommunity(Pageable pageable) {
        try {
            return communityReportRepository.findAll(pageable).map(CommunityReportDto::response);
        } catch (GeneralException e) {
            throw e;
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
        } catch (GeneralException e) {
            throw e;
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
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }
}

