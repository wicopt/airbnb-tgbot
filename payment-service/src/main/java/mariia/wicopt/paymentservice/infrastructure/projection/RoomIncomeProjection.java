// RoomIncomeProjection.java
package mariia.wicopt.paymentservice.infrastructure.projection;

import java.math.BigDecimal;

public interface RoomIncomeProjection {
    String getRoomNumber();
    BigDecimal getTotalIncome();
}