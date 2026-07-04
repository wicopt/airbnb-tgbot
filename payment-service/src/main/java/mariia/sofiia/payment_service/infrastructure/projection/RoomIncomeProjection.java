// RoomIncomeProjection.java
package mariia.sofiia.payment_service.infrastructure.projection;

import java.math.BigDecimal;

public interface RoomIncomeProjection {
    String getRoomNumber();
    BigDecimal getTotalIncome();
}