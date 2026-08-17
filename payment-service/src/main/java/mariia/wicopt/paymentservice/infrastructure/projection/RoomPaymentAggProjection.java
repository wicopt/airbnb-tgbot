package mariia.wicopt.paymentservice.infrastructure.projection;

import java.math.BigDecimal;

public interface RoomPaymentAggProjection {
    String getRoomNumber();
    BigDecimal getTotalIncome();
    BigDecimal getTotalExpenses();
}