package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Optional;
import kr.finpo.api.constant.NotificationType;
import kr.finpo.api.domain.Notification;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record NotificationDto(
    Long id,
    NotificationType type,
    PolicyDto policy,
    CommentDto comment
) {

    public NotificationDto {
    }

    public static NotificationDto response(Notification notification) {
        return new NotificationDto(
            notification.getId(),
            notification.getType(),
            Optional.ofNullable(notification.getPolicy()).map(policy -> PolicyDto.response(policy, null)).orElse(null),
            Optional.ofNullable(notification.getComment()).map(comment -> CommentDto.response(comment, true, null))
                .orElse(null));
    }
}
