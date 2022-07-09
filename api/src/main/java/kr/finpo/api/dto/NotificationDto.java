package kr.finpo.api.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import kr.finpo.api.constant.NotificationType;
import kr.finpo.api.domain.Fcm;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.InterestRegion;
import kr.finpo.api.domain.Notification;

import java.util.List;
import java.util.Optional;

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
        Optional.ofNullable(notification.getComment()).map(comment -> CommentDto.response(comment, true, null)).orElse(null));
  }
}
