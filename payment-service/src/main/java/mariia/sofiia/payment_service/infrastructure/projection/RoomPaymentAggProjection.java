package mariia.sofiia.payment_service.infrastructure.projection;

import java.math.BigDecimal;

public interface RoomPaymentAggProjection {
    String getRoomNumber();
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses();
}