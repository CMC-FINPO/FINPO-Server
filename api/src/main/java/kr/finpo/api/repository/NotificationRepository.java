package kr.finpo.api.repository;

import kr.finpo.api.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
  @Query(value = "SELECT n FROM Notification n WHERE n.user.id = :userId AND n.id < :lastId AND (n.comment.status = true OR n.policy.status = true)")
  public Page<Notification> findByUserId(Long userId, Long lastId, Pageable pageable);

  @Query(value = "SELECT n FROM Notification n LEFT JOIN Comment c ON n.comment = c LEFT JOIN Policy p ON n.policy = p WHERE n.user.id = :userId AND (p.status = true OR c.status = true)")
  public Page<Notification> findByUserId(Long userId, Pageable pageable);

  public Long deleteByUserId(Long userId);

  public Long deleteByPolicyId(Long policyId);
}

