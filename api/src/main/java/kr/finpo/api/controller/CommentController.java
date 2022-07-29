package kr.finpo.api.controller;

import kr.finpo.api.dto.CommentDto;
import kr.finpo.api.dto.CommunityReportDto;
import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.service.BlockedUserService;
import kr.finpo.api.service.CommentService;
import kr.finpo.api.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RequiredArgsConstructor
@RestController
@Secured({"ROLE_ADMIN", "ROLE_USER"})
@RequestMapping("/comment")
public class CommentController {

    private final CommentService commentService;
    private final ReportService reportService;
    private final BlockedUserService blockedUserService;

    @PutMapping("/{id}")
    public DataResponse<Object> update(
        @PathVariable Long id,
        @RequestBody CommentDto body
    ) {
        return DataResponse.of(commentService.update(id, body));
    }

    @DeleteMapping("/{id}")
    public DataResponse<Object> delete(@PathVariable Long id) {
        return DataResponse.of(commentService.delete(id));
    }

    @PostMapping("/{id}/report")
    public DataResponse<Object> reportComment(
        @PathVariable Long id,
        @RequestBody CommunityReportDto body
    ) {
        return DataResponse.of(reportService.insertComment(id, body));
    }

    @PostMapping("/{id}/block")
    public DataResponse<Object> blockComment(@PathVariable Long id) {
        return DataResponse.of(blockedUserService.insert(null, id));
    }
}
