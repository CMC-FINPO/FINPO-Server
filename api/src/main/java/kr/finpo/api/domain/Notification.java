package kr.finpo.api.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import kr.finpo.api.constant.NotificationType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private NotificationType type;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected Notification() {
    }

    protected Notification(User user, NotificationType type, Comment comment, Policy policy) {
        this.user = user;
        this.type = type;
        this.comment = comment;
        this.policy = policy;
    }

    public static Notification of() {
        return new Notification();
    }

    public static Notification of(User user, NotificationType type, Comment comment) {
        return new Notification(user, type, comment, null);
    }

    public static Notification of(User user, NotificationType type, Policy policy) {
        return new Notification(user, type, null, policy);
    }

    @Setter
    @ManyToOne
    @JoinColumn
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn
    private Comment comment;

    @Setter
    @ManyToOne
    @JoinColumn
    private Policy policy;
}