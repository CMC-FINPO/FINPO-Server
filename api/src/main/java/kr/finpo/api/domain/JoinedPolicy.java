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
import kr.finpo.api.constant.Constraint;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class JoinedPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(length = Constraint.JOINED_POLICY_MEMO_MAX_LENGTH)
    private String memo;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected JoinedPolicy() {
    }

    protected JoinedPolicy(User user, Policy policy, String memo) {
        this.user = user;
        this.policy = policy;
        this.memo = memo;
    }

    public static JoinedPolicy of() {
        return new JoinedPolicy();
    }

    public static JoinedPolicy of(User user, Policy policy, String memo) {
        return new JoinedPolicy(user, policy, memo);
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