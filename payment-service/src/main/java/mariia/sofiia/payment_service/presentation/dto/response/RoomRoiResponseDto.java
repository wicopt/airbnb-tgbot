package mariia.sofiia.payment_service.presentation.dto.response;

import java.math.BigDecimal;

import lombok.Data;

@Data
    public class RoomRoiResponseDto {
        private String roomNumber;
        private String messageName;
        private BigDecimal purchasePrice;
        private BigDecimal totalIncome;
        private BigDecimal totalExpenses;
        private BigDecimal netProfit;
        private BigDecimal roiPercent;   
    }