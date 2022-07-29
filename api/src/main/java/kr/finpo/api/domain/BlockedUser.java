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
public class BlockedUser {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    @Setter
    @Column(nullable = false)
    private Boolean anonymity;

    protected BlockedUser() {
    }

    public BlockedUser(User user, User blockedUser, Boolean anonymity) {
        this.user = user;
        this.blockedUser = blockedUser;
        this.anonymity = anonymity;
    }

    public static BlockedUser of() {
        return new BlockedUser();
    }

    public static BlockedUser of(User user, User blockedUser, Boolean anonymity) {
        return new BlockedUser(user, blockedUser, anonymity);
    }

    @Setter
    @ManyToOne
    @JoinColumn
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn
    private User blockedUser;
}