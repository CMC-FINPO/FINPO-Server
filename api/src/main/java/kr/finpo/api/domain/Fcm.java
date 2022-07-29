package kr.finpo.api.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Fcm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String registrationToken;

    @Setter
    @Column(nullable = false)
    private Boolean subscribe = false;

    @Setter
    @Column(nullable = false)
    private Boolean communitySubscribe = true;

    @Setter
    @Column(nullable = false)
    private Boolean adSubscribe = false;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    protected Fcm() {
    }

    protected Fcm(Boolean subscribe, String registrationToken) {
        this.subscribe = subscribe;
        this.registrationToken = registrationToken;
    }

    public static Fcm of(Boolean subscribe, String registrationToken) {
        return new Fcm(subscribe, registrationToken);
    }

    @Setter
    @OneToOne
    @JoinColumn
    private User user;
}