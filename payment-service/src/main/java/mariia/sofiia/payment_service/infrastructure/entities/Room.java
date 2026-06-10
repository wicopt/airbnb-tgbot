package mariia.sofiia.payment_service.infrastructure.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "room", schema = "core")
@Getter
@Setter
public class Room {

    @Id
    @Column(name = "room_number")
    private String roomNumber;

    @Column(name = "group_id")
    private String groupId;

    @Column(name = "building_name")
    private String buildingName;

    @Column(name = "message_name")
    private String messageName;

    @Column(name = "purchase_price")
    private BigDecimal purchasePrice;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;
}