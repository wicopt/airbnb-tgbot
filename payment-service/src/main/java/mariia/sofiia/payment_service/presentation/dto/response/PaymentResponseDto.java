package mariia.sofiia.payment_service.presentation.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Data;

@Data
public class PaymentResponseDto {
    private boolean success;
    private String correlationId;
    private String paymentId;
    private String groupId;
    private String roomNumber;
    private String category;
    private BigDecimal amount;          // итоговая сумма в USD
    private BigDecimal originalAmount;  // оригинальная сумма
    private String originalCurrency;    // оригинальная валюта
    private LocalDate paymentDate;
    private String errorCode;
}