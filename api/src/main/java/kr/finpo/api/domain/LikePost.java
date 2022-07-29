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
public class LikePost {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected LikePost() {
    }

    protected LikePost(User user, Post post) {
        this.user = user;
        this.post = post;
    }

    public static LikePost of() {
        return new LikePost();
    }

    public static LikePost of(User user, Post post) {
        return new LikePost(user, post);
    }


    @Setter
    @ManyToOne
    @JoinColumn
    private User user;

    @Setter
    @ManyToOne
    @JoinColumn
    private Post post;
}