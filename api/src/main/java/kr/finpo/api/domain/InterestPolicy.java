package kr.finpo.api.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class InterestPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected InterestPolicy() {
    }

    protected InterestPolicy(User user, Policy policy) {
        this.user = user;
        this.policy = policy;
    }

    public static InterestPolicy of() {
        return new InterestPolicy();
    }

    public static InterestPolicy of(User user, Policy policy) {
        return new InterestPolicy(user, policy);
    }


    @Setter
    @ManyToOne
    @JoinColumn
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn
    private Policy policy;
}