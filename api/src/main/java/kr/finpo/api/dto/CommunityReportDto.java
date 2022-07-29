package kr.finpo.api.dto;

import static org.springframework.util.ObjectUtils.isEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.LocalDateTime;
import kr.finpo.api.domain.CommunityReport;
import kr.finpo.api.domain.Report;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record CommunityReportDto(
    Long id,
    Report report,
    LocalDateTime createdAt,
    PostDto post,
    CommentDto comment,
    UserDto user
) {

    public CommunityReportDto {
    }

    public static CommunityReportDto response(CommunityReport communityReport) {
        return new CommunityReportDto(communityReport.getId(), communityReport.getReport(),
            communityReport.getCreatedAt(),
            isEmpty(communityReport.getPost()) ? null : PostDto.adminResponse(communityReport.getPost()),
            isEmpty(communityReport.getComment()) ? null : CommentDto.adminResponse(communityReport.getComment()),
            UserDto.response(communityReport.getUser()));
    }
}
