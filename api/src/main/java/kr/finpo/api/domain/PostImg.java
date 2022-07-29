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
public class PostImg {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String img;

    @Setter
    @Column(nullable = false)
    private Integer oorder;

    @Column(nullable = false, insertable = false, updatable = false, columnDefinition = "datetime default CURRENT_TIMESTAMP")
    @CreatedDate
    private LocalDateTime createdAt;

    protected PostImg() {
    }

    protected PostImg(String img, Integer oorder, Post post) {
        this.img = img;
        this.oorder = oorder;
        this.post = post;
    }

    public static PostImg of() {
        return new PostImg();
    }

    public static PostImg of(String img, Integer order, Post post) {
        return new PostImg(img, order, post);
    }

    @Setter
    @ManyToOne
    @JoinColumn
    private Post post;
}