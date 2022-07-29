package kr.finpo.api.controller;

import kr.finpo.api.dto.DataResponse;
import kr.finpo.api.dto.FcmDto;
import kr.finpo.api.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("/me")
    public DataResponse<Object> getMy() {
        return DataResponse.of(notificationService.getMy());
    }

    @GetMapping("/history/me")
    public DataResponse<Object> getMyHistories(
        @RequestParam(value = "lastId", required = false) Long lastId,
        Pageable pageable
    ) {
        return DataResponse.of(notificationService.getMyHistories(lastId, pageable));
    }

    @DeleteMapping("/history/{id}")
    public DataResponse<Object> deleteMyHistory(@PathVariable Long id) {
        return DataResponse.of(notificationService.deleteMyHistory(id));
    }

    @PutMapping("/me")
    public DataResponse<Object> upsertMy(@RequestBody FcmDto body) {
        return DataResponse.of(notificationService.upsertMy(body));
    }
}

