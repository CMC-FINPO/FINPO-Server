package kr.finpo.api.service;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.*;
import kr.finpo.api.constant.Constraint;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.domain.*;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.FcmRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import kr.finpo.api.repository.InterestRegionRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class FcmService {

  private final FcmRepository fcmRepository;
  private final InterestCategoryRepository interestCategoryRepository;
  private final InterestRegionRepository interestRegionRepository;


  public List<Long> sendPolicyPush(Policy policy) {
    List<String> registrationTokens = new ArrayList<>();

    List<Long> interestRegionUsers = interestRegionRepository.findByRegionIdAndSubscribe(policy.getRegion().getId(), true).stream().map(InterestRegion::getUser).map(User::getId).toList();
    List<Long> interestCategoryUsers = interestCategoryRepository.findByCategoryIdAndSubscribe(policy.getCategory().getId(), true).stream().map(InterestCategory::getUser).map(User::getId).toList();

    // intersection
    List<Long> userIds = interestCategoryUsers.stream().filter(interestRegionUsers::contains).toList();

    if (userIds.size() == 0) return null;

    userIds.forEach(userId -> {
      Fcm fcm = fcmRepository.findOneByUserId(userId).get();
      registrationTokens.add(fcm.getRegistrationToken());
    });

    List<List<String>> registrationTokensPartition = Lists.partition(registrationTokens, 1000);

    registrationTokensPartition.forEach(registrationTokensPart -> {
      MulticastMessage message = MulticastMessage.builder()
          .putData("type", "policy")
          .putData("id", Long.toString(policy.getId()))
          .putData("title", policy.getTitle())
          .putData("region2", policy.getRegion().getName())
          .putData("region1", Optional.ofNullable(policy.getRegion()).map(region -> region.getParent().getName()).orElse(null))
          .putData("category2", policy.getCategory().getName())
          .putData("category1", policy.getCategory().getParent().getName())
          .putData("title", policy.getTitle())
          .addAllTokens(registrationTokens)
          .build();

      try {
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        log.debug("FCM) " + response.getSuccessCount() + " messages were sent successfully");
      } catch (FirebaseMessagingException e) {
        log.error(e.toString());
      }
    });
    return userIds;
  }

  public List<Long> sendCommentPush(Comment comment) {
    List<Long> userIds = new ArrayList<>();

    try {
      Optional.ofNullable(comment.getParent()).flatMap(parentComment -> Optional.ofNullable(parentComment.getUser())).ifPresent(parentUser -> {
        if(parentUser.getId().equals(SecurityUtil.getCurrentUserId())) return;

        fcmRepository.findOneByUserId(parentUser.getId()).ifPresent(parentUserFcm -> {
          String parentRegistrationToken = parentUserFcm.getRegistrationToken();

          Message message = Message.builder()
              .putData("type", "childComment")
              .putData("id", Long.toString(comment.getId()))
              .putData("content", comment.getContent().substring(0, Math.min(Constraint.CONTENT_PREVIEW_MAX_LENGTH, comment.getContent().length())) + (comment.getContent().length() > Constraint.CONTENT_PREVIEW_MAX_LENGTH ? "..." : ""))
              .putData("postId", Long.toString(comment.getPost().getId()))
              .setToken(parentRegistrationToken)
              .build();

          try {
            FirebaseMessaging.getInstance().send(message);
            userIds.add(parentUser.getId());
          } catch (FirebaseMessagingException e) {
            log.error(e.toString());
          }
        });
      });

      Optional.ofNullable(comment.getPost().getUser()).ifPresent(postUser -> {
        if(postUser.getId().equals(SecurityUtil.getCurrentUserId()) || userIds.contains(postUser.getId())) return;


        fcmRepository.findOneByUserId(postUser.getId()).ifPresent(postUserFcm -> {

          String postUserRegistrationToken = postUserFcm.getRegistrationToken();

          Message message = Message.builder()
              .putData("type", "comment")
              .putData("id", Long.toString(comment.getId()))
              .putData("content", comment.getContent().substring(0, Math.min(Constraint.CONTENT_PREVIEW_MAX_LENGTH, comment.getContent().length())) + (comment.getContent().length() > Constraint.CONTENT_PREVIEW_MAX_LENGTH ? "..." : ""))
              .putData("postId", Long.toString(comment.getPost().getId()))
              .setToken(postUserRegistrationToken)
              .build();
          try {
            FirebaseMessaging.getInstance().send(message);
            userIds.add(postUser.getId());
          } catch (FirebaseMessagingException e) {
            log.error(e.toString());
          }
        });
      });
      return userIds;
    } catch (Exception e) {
      throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
    }
  }
}

