package mariia.wicopt.paymentservice.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentCreateRequestDto {
    private String groupId;
    private String roomNumber;      // null если категория is_shared
    private Long categoryId;        
    private BigDecimal amount;
    private String correlationId;
    private LocalDate paymentDate;
    private String currency;
}