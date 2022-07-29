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
public class CommunityReport {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected CommunityReport() {
    }

    public CommunityReport(Report report, Post post, Comment comment, User user) {
        this.report = report;
        this.post = post;
        this.comment = comment;
        this.user = user;
    }

    public static CommunityReport of() {
        return new CommunityReport();
    }

    public static CommunityReport of(Report report, Comment comment, User user) {
        return new CommunityReport(report, null, comment, user);
    }

    public static CommunityReport of(Report report, Post post, User user) {
        return new CommunityReport(report, post, null, user);
    }

    @Setter
    @ManyToOne
    @JoinColumn
    private Report report;

    @Setter
    @ManyToOne
    @JoinColumn
    private Comment comment;

    @Setter
    @ManyToOne
    @JoinColumn
    private Post post;

    @Setter
    @ManyToOne
    @JoinColumn
    private User user;
}