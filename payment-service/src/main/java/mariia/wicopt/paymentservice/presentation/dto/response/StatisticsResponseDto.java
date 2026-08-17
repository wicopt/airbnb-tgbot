package mariia.wicopt.paymentservice.presentation.dto.response;


import java.math.BigDecimal;
import java.util.List;

import lombok.Data;

@Data
public class StatisticsResponseDto {
    private String groupId;
    private String roomNumber;
    private BigDecimal totalIncome;
    private BigDecimal totalExpenses;
    private BigDecimal netProfit;
    private List<PaymentResponseDto> payments;
    private String correlationId; 

}