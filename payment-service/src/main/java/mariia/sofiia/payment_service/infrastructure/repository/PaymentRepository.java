package mariia.sofiia.payment_service.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mariia.sofiia.payment_service.infrastructure.entities.Payment;
import mariia.sofiia.payment_service.infrastructure.projection.RoomPaymentAggProjection;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Все платежи по квартире (без фильтра дат)
    List<Payment> findByGroupIdAndRoomNumber(String groupId, String roomNumber);

    // Платежи по квартире с фильтром дат
    List<Payment> findByGroupIdAndRoomNumberAndPaymentDateBetween(
            String groupId, String roomNumber,
            LocalDate from, LocalDate to);

    // Все платежи по всем квартирам группы (для ROI)
    @Query("SELECT p FROM Payment p WHERE p.groupId = :groupId")
    List<Payment> findAllByGroupId(@Param("groupId") String groupId);

    @Query("""
                SELECT
                    p.roomNumber AS roomNumber,

                    SUM(CASE WHEN p.amount > 0 THEN p.amount ELSE 0 END) AS totalIncome,

                    SUM(CASE WHEN p.amount < 0 THEN p.amount ELSE 0 END) AS totalExpenses

                FROM Payment p
                WHERE p.groupId = :groupId
                GROUP BY p.roomNumber
            """)
    List<RoomPaymentAggProjection> getAggregatedByGroupId(@Param("groupId") String groupId);
}