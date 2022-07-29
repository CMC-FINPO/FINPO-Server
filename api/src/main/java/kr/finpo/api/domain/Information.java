package kr.finpo.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Information {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Setter
    @Column(nullable = false)
    private String type;

    @Setter
    @Column(nullable = true)
    private String title;

    @Setter
    @Column(columnDefinition = "LONGTEXT", nullable = true)
    private String content;

    @Setter
    @Column(nullable = true)
    private String url;

    @Setter
    @Column(nullable = true)
    private Boolean status;

    @Setter
    @Column(nullable = false)
    private Boolean hidden = false;

    protected Information() {
    }

    public Information(String type, String title, String content, String url, Boolean status) {
        this.type = type;
        this.title = title;
        this.content = content;
        this.url = url;
        this.status = status;
    }

    public static Information of() {
        return new Information();
    }

    public static Information of(String type, String content, String url, Boolean status) {
        return new Information(type, null, content, url, status);
    }

    public static Information of(String type, String title, String content, String url, Boolean status) {
        return new Information(type, title, content, url, status);
    }
}