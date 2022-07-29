package kr.finpo.api.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@EntityListeners(AuditingEntityListener.class)
@Entity
@ToString
public class Region {

    @Id
    @Setter
    private Long id;

    @Setter
    @Column(nullable = false)
    private String name;

    @Setter
    @Column(nullable = false)
    private Long depth;

    @Setter
    @Column(nullable = false)
    private Boolean status;

    protected Region() {
    }

    public Region(Long id, String name, Long depth, Boolean status) {
        this.id = id;
        this.name = name;
        this.depth = depth;
        this.status = status;
    }

    public static Region of() {
        return new Region();
    }

    public static Region of(Long id, String name, Long depth, Boolean status) {
        return new Region(id, name, depth, status);
    }

    @Setter
    @JoinColumn(name = "parent_id")
    @ManyToOne(optional = true)
    private Region parent;
}