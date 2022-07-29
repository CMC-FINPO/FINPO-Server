package kr.finpo.api.service;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MulticastMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kr.finpo.api.constant.Constraint;
import kr.finpo.api.constant.ErrorCode;
import kr.finpo.api.constant.NotificationType;
import kr.finpo.api.domain.Comment;
import kr.finpo.api.domain.InterestCategory;
import kr.finpo.api.domain.InterestRegion;
import kr.finpo.api.domain.Notification;
import kr.finpo.api.domain.Policy;
import kr.finpo.api.domain.Region;
import kr.finpo.api.domain.User;
import kr.finpo.api.exception.GeneralException;
import kr.finpo.api.repository.BlockedUserRepository;
import kr.finpo.api.repository.FcmRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import kr.finpo.api.repository.InterestRegionRepository;
import kr.finpo.api.repository.NotificationRepository;
import kr.finpo.api.repository.UserRepository;
import kr.finpo.api.util.SecurityUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class FcmService {

    private final FcmRepository fcmRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;
    private final InterestCategoryRepository interestCategoryRepository;
    private final InterestRegionRepository interestRegionRepository;
    private final BlockedUserRepository blockedUserRepository;


    public List<Long> sendPolicyPush(Policy policy) {
        List<String> registrationTokens = new ArrayList<>();

        List<Long> interestRegionUsers = interestRegionRepository.findByRegionIdAndSubscribe(policy.getRegion().getId(),
            true).stream().map(InterestRegion::getUser).map(User::getId).toList();
        List<Long> interestCategoryUsers = interestCategoryRepository.findByCategoryIdAndSubscribe(
            policy.getCategory().getId(), true).stream().map(InterestCategory::getUser).map(User::getId).toList();

        // intersection
        List<Long> userIds = interestCategoryUsers.stream().filter(interestRegionUsers::contains).toList();

        if (userIds.size() == 0) {
            return null;
        }

        userIds.forEach(userId -> {
            fcmRepository.findFirst1ByUserIdAndSubscribe(userId, true).ifPresent(fcm -> {
                registrationTokens.add(fcm.getRegistrationToken());
                userRepository.findById(userId).ifPresent(user ->
                    notificationRepository.save(Notification.of(user, NotificationType.POLICY, policy))
                );
            });
        });

        List<List<String>> registrationTokensPartition = Lists.partition(registrationTokens, 1000);

        Region region = policy.getRegion();

        registrationTokensPartition.forEach(registrationTokensPart -> {
            MulticastMessage message = MulticastMessage.builder()
                .putData("type", "POLICY")
                .putData("id", Long.toString(policy.getId()))
                .putData("title", policy.getTitle())
                .putData("region",
                    Optional.ofNullable(region.getParent()).isPresent() ? region.getParent().getName() + " "
                        + region.getName() : region.getName() + " 전체")
                .putData("category", policy.getCategory().getParent().getName() + " " + policy.getCategory().getName())
                .putData("title", policy.getTitle())
                .addAllTokens(registrationTokens)
                .build();

            try {
                BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
            } catch (FirebaseMessagingException e) {
                log.error("policy push error");
                log.error(e.toString());
            }
        });
        return userIds;
    }

    public List<Long> sendCommentPush(Comment comment) {
        List<Long> userIds = new ArrayList<>();

        try {
            Optional.ofNullable(comment.getParent())
                .flatMap(parentComment -> Optional.ofNullable(parentComment.getUser())).ifPresent(parentUser -> {
                    if (parentUser.getId().equals(SecurityUtil.getCurrentUserId())
                        || blockedUserRepository.findOneByUserIdAndBlockedUserIdAndAnonymity(parentUser.getId(),
                        SecurityUtil.getCurrentUserId(), comment.getAnonymity()).isPresent()) {
                        return;
                    }

                    userRepository.findById(parentUser.getId()).ifPresent(user ->
                        notificationRepository.save(Notification.of(user, NotificationType.CHILDCOMMENT, comment))
                    );

                    fcmRepository.findFirst1ByUserIdAndSubscribeAndCommunitySubscribe(parentUser.getId(), true, true)
                        .ifPresent(parentUserFcm -> {
                            String parentRegistrationToken = parentUserFcm.getRegistrationToken();
                            userIds.add(parentUser.getId());

                            Message message = Message.builder()
                                .putData("type", "CHILDCOMMENT")
                                .putData("id", Long.toString(comment.getId()))
                                .putData("content",
                                    stringCutter(comment.getContent(), Constraint.CONTENT_PREVIEW_MAX_LENGTH))
                                .putData("postId", Long.toString(comment.getPost().getId()))
                                .putData("postContent",
                                    stringCutter(comment.getPost().getContent(), Constraint.CONTENT_PREVIEW_MAX_LENGTH))
                                .setToken(parentRegistrationToken)
                                .build();

                            try {
                                FirebaseMessaging.getInstance().send(message);
                            } catch (FirebaseMessagingException e) {
                                log.error("comment push error");
                                log.error(e.toString());
                            }
                        });
                });

            Optional.ofNullable(comment.getPost().getUser()).ifPresent(postUser -> {
                if (postUser.getId().equals(SecurityUtil.getCurrentUserId()) || userIds.contains(postUser.getId())
                    || blockedUserRepository.findOneByUserIdAndBlockedUserIdAndAnonymity(postUser.getId(),
                    SecurityUtil.getCurrentUserId(), comment.getAnonymity()).isPresent()) {
                    return;
                }

                userRepository.findById(postUser.getId()).ifPresent(user ->
                    notificationRepository.save(Notification.of(user, NotificationType.COMMENT, comment))
                );

                fcmRepository.findFirst1ByUserIdAndSubscribeAndCommunitySubscribe(postUser.getId(), true, true)
                    .ifPresent(postUserFcm -> {

                        String postUserRegistrationToken = postUserFcm.getRegistrationToken();

                        Message message = Message.builder()
                            .putData("type", "COMMENT")
                            .putData("id", Long.toString(comment.getId()))
                            .putData("content",
                                stringCutter(comment.getContent(), Constraint.CONTENT_PREVIEW_MAX_LENGTH))
                            .putData("postId", Long.toString(comment.getPost().getId()))
                            .putData("postContent",
                                stringCutter(comment.getPost().getContent(), Constraint.CONTENT_PREVIEW_MAX_LENGTH))
                            .setToken(postUserRegistrationToken)
                            .build();
                        try {
                            FirebaseMessaging.getInstance().send(message);
                            userIds.add(postUser.getId());
                        } catch (FirebaseMessagingException e) {
                            log.error("comment push error");
                            log.error(e.toString());
                        }
                    });
            });
            return userIds;
        } catch (Exception e) {
            throw new GeneralException(ErrorCode.DATA_ACCESS_ERROR, e);
        }
    }

    private String stringCutter(String str, Integer maximum) {
        return str.substring(0, Math.min(maximum, str.length())) + (str.length() > maximum ? "..." : "");
    }
}

