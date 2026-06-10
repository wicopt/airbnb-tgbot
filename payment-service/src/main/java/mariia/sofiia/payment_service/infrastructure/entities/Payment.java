package mariia.sofiia.payment_service.infrastructure.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "payment", schema = "core")
@Getter
@Setter
public class Payment {

    @Id
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "amount")
    private BigDecimal amount;

    @Column(name = "payment_date")
    private LocalDate paymentDate;

    @Column(name = "category")
    private String category;
}
