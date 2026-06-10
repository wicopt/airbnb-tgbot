package mariia.sofiia.payment_service.presentation.dto.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentCreateRequestDto {
    private String groupId;
    private String roomNumber;
    private String category;
    private BigDecimal amount;
    private String correlationId;    
    private LocalDate paymentDate;
    private String currency;
}