package mariia.wicopt.paymentservice.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentResponseDto {
    private String paymentId;
    private String groupId;
    private String roomNumber;
    private BigDecimal amount;
    private BigDecimal originalAmount;
    private String originalCurrency;
    private LocalDate paymentDate;
    private Long categoryId;
    private String categoryName;
    private String correlationId;
    private boolean success;
    private String errorCode;
}