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
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class InterestRegion {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    private Boolean isDefault = false;

    @Setter
    @Column(nullable = false)
    private Boolean subscribe = true;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    protected InterestRegion() {
    }

    protected InterestRegion(User user, Region region, Boolean isDefault) {
        this.user = user;
        this.region = region;
        this.isDefault = isDefault;
    }

    protected InterestRegion(User user, Region region) {
        this.user = user;
        this.region = region;
    }

    public static InterestRegion of() {
        return new InterestRegion();
    }

    public static InterestRegion of(User user, Region region) {
        return new InterestRegion(user, region);
    }

    public static InterestRegion of(User user, Region region, Boolean isDefault) {
        return new InterestRegion(user, region, isDefault);
    }

    public void updateDefault(Region region) {
        this.region = region;
    }


    @Setter
    @ManyToOne
    @JoinColumn
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn
    private Region region;
}