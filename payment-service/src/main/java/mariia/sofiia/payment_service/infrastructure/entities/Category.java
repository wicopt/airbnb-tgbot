package mariia.sofiia.payment_service.infrastructure.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "category", schema = "core")
@Getter
@Setter
public class Category {

    @Id
    @Column(name = "category_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long categoryId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "category_name")
    private String categoryName;

    @Column(name = "is_shared")
    private boolean isShared;
}
