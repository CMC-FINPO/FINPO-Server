package kr.finpo.api.service;

import com.google.common.collect.Lists;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.MulticastMessage;
import kr.finpo.api.domain.*;
import kr.finpo.api.repository.FcmRepository;
import kr.finpo.api.repository.InterestCategoryRepository;
import kr.finpo.api.repository.InterestRegionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;


@RequiredArgsConstructor
@Transactional
@Service
@Slf4j
public class FcmService {

  private final FcmRepository fcmRepository;
  private final InterestCategoryRepository interestCategoryRepository;
  private final InterestRegionRepository interestRegionRepository;


  public void sendPolicyPush(Policy policy) {
    List<String> registrationTokens = new ArrayList<>();

    List<Long> interestRegionUsers = interestRegionRepository.findByRegionId(policy.getRegion().getId()).stream().map(InterestRegion::getUser).map(User::getId).toList();
    List<Long> interestCategoryUsers = interestCategoryRepository.findByCategoryIdAndSubscribe(policy.getCategory().getId(), true).stream().map(InterestCategory::getUser).map(User::getId).toList();

    // intersection
    List<Long> userIds = interestCategoryUsers.stream().filter(interestRegionUsers::contains).toList();

    if (userIds.size() == 0) return;

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
          .addAllTokens(registrationTokens)
          .build();

      try {
        BatchResponse response = FirebaseMessaging.getInstance().sendMulticast(message);
        log.debug("FCM) " + response.getSuccessCount() + " messages were sent successfully");
      } catch (FirebaseMessagingException e) {
        log.error(e.toString());
      }
    });
  }
}

