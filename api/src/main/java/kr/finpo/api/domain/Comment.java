package kr.finpo.api.domain;

import java.time.LocalDateTime;
import javax.annotation.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import kr.finpo.api.constant.Constraint;
import kr.finpo.api.constant.State;
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
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(length = Constraint.COMMENT_MAX_LENGTH, nullable = false)
    private String content;

    @Setter
    @Column(nullable = false)
    private Boolean anonymity;

    @Setter
    @Column
    private Integer anonymityId = 0;

    @Setter
    @Column(nullable = false)
    private Boolean status = true;

    @Setter
    @Column(nullable = false)
    private State state = State.NORMAL;

    @Setter
    @Column(nullable = false)
    private Boolean isModified = false;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP on update CURRENT_TIMESTAMP")
    @LastModifiedDate
    private LocalDateTime modifiedAt;

    protected Comment() {
    }

    protected Comment(String content, Boolean anonymity) {
        this.content = content;
        this.anonymity = anonymity;
    }

    public static Comment of() {
        return new Comment();
    }

    public static Comment of(String content, Boolean anonymity) {
        return new Comment(content, anonymity);
    }

    @Setter
    @ManyToOne
    @Nullable
    @JoinColumn
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn
    private Post post;

    @Setter
    @ManyToOne
    @JoinColumn
    private Comment parent;
}