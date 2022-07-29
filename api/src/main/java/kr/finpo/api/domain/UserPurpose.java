package kr.finpo.api.domain;

import com.sun.istack.NotNull;
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
@ToString
@EntityListeners(AuditingEntityListener.class)
@Entity
public class UserPurpose {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    private Long userPurposeId;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected UserPurpose() {
    }

    protected UserPurpose(Long userPurposeId, User user) {
        this.userPurposeId = userPurposeId;
        this.user = user;
    }

    public static UserPurpose of(Long userPurposeId, User user) {
        return new UserPurpose(userPurposeId, user);
    }

    @Setter
    @ManyToOne
    @NotNull
    @JoinColumn
    private User user;
}