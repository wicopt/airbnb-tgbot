// PaymentRepository.java
package mariia.sofiia.payment_service.infrastructure.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import mariia.sofiia.payment_service.infrastructure.entities.Payment;
import mariia.sofiia.payment_service.infrastructure.projection.RoomIncomeProjection;
import mariia.sofiia.payment_service.infrastructure.projection.RoomPaymentAggProjection;

public interface PaymentRepository extends JpaRepository<Payment, UUID> {

    // Для StatisticsService — по комнате с фильтром по датам
    List<Payment> findByGroupIdAndRoomNumberAndPaymentDateBetween(
        String groupId, String roomNumber,
        LocalDate from, LocalDate to
    );

    // Для StatisticsService — по комнате без фильтра дат
    List<Payment> findByGroupIdAndRoomNumber(String groupId, String roomNumber);

    // Для ROI — агрегат дохода/расхода по всем комнатам группы
    @Query("""
        SELECT
            p.roomNumber      AS roomNumber,
            SUM(CASE WHEN p.amount > 0 THEN p.amount ELSE 0 END) AS totalIncome,
            SUM(CASE WHEN p.amount < 0 THEN p.amount ELSE 0 END) AS totalExpenses
        FROM Payment p
        WHERE p.groupId = :groupId
        GROUP BY p.roomNumber
        """)
    List<RoomPaymentAggProjection> getAggregatedByGroupId(@Param("groupId") String groupId);

    // Для distributeSharedExpense — дата предыдущего платежа этой категории
    @Query("""
        SELECT MAX(p.paymentDate) FROM Payment p
        WHERE p.groupId = :groupId
          AND p.category.categoryId = :categoryId
          AND p.paymentDate < :currentDate
        """)
    Optional<LocalDate> findPreviousSharedPaymentDate(
        @Param("groupId") String groupId,
        @Param("categoryId") Long categoryId,
        @Param("currentDate") LocalDate currentDate
    );

    // Для distributeSharedExpense — доход каждой комнаты за период
    @Query("""
        SELECT p.roomNumber AS roomNumber, SUM(p.amount) AS totalIncome
        FROM Payment p
        WHERE p.groupId = :groupId
          AND p.amount > 0
          AND p.paymentDate > :from
          AND p.paymentDate <= :to
        GROUP BY p.roomNumber
        """)
    List<RoomIncomeProjection> getIncomeByRoomBetween(
        @Param("groupId") String groupId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );
    @Query("""
        SELECT p.roomNumber AS roomNumber, SUM(p.amount) AS totalIncome
        FROM Payment p
        WHERE p.groupId = :groupId
          AND p.amount > 0
          AND p.paymentDate > :from
          AND p.paymentDate <= :to
        GROUP BY p.roomNumber
        """)
    List<RoomIncomeProjection> get(
        @Param("groupId") String groupId,
        @Param("from") LocalDate from,
        @Param("to") LocalDate to
    );

}