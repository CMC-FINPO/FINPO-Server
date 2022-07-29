package kr.finpo.api.domain;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
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
public class KakaoAccount {

    @Id
    @Setter
    private String id;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    protected KakaoAccount() {
    }

    protected KakaoAccount(String kakaoId, User user) {
        this.id = kakaoId;
        this.user = user;
    }

    public static KakaoAccount of(String kakaoId, User user) {
        return new KakaoAccount(kakaoId, user);
    }

    @Setter
    @OneToOne
    @JoinColumn
    private User user;
}