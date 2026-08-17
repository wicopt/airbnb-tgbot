package mariia.wicopt.paymentservice.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.wicopt.paymentservice.infrastructure.client.ExchangeRateClient;
import mariia.wicopt.paymentservice.infrastructure.entities.Category;
import mariia.wicopt.paymentservice.infrastructure.entities.Payment;
import mariia.wicopt.paymentservice.infrastructure.entities.Room;
import mariia.wicopt.paymentservice.infrastructure.projection.RoomIncomeProjection;
import mariia.wicopt.paymentservice.infrastructure.repository.CategoryRepository;
import mariia.wicopt.paymentservice.infrastructure.repository.PaymentRepository;
import mariia.wicopt.paymentservice.infrastructure.repository.RoomRepository;
import mariia.wicopt.paymentservice.presentation.dto.request.PaymentCreateRequestDto;
import mariia.wicopt.paymentservice.presentation.dto.response.PaymentResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

        private final PaymentRepository paymentRepository;
        private final CategoryRepository categoryRepository;
        private final RoomRepository roomRepository;
        private final ExchangeRateClient exchangeRateClient;

        @Transactional
        public PaymentResponseDto createPayment(PaymentCreateRequestDto request) {
                PaymentResponseDto response = new PaymentResponseDto();
                response.setCorrelationId(request.getCorrelationId());

                try {
                        Category category = categoryRepository.findById(request.getCategoryId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Category not found: " + request.getCategoryId()));

                        String currency = request.getCurrency() != null
                                        ? request.getCurrency().toUpperCase()
                                        : "USD";

                        BigDecimal amountInUsd = exchangeRateClient.convertToUsd(request.getAmount(), currency);
                        LocalDate paymentDate = request.getPaymentDate() != null
                                        ? request.getPaymentDate()
                                        : LocalDate.now();

                        if (category.isShared()) {
                                distributeSharedExpense(request.getGroupId(), category, amountInUsd, paymentDate);
                        } else {
                                Payment saved = paymentRepository.save(
                                                buildPayment(request.getGroupId(), request.getRoomNumber(), category,
                                                                amountInUsd, paymentDate));
                                response.setPaymentId(saved.getPaymentId().toString());
                                response.setRoomNumber(saved.getRoomNumber());
                        }

                        response.setSuccess(true);
                        response.setGroupId(request.getGroupId());
                        response.setAmount(amountInUsd);
                        response.setCategoryId(category.getCategoryId());
                        response.setCategoryName(category.getCategoryName());
                        response.setPaymentDate(paymentDate);

                        if (!"USD".equals(currency)) {
                                response.setOriginalAmount(request.getAmount());
                                response.setOriginalCurrency(currency);
                        }

                        log.info("Payment created: groupId={} category={} amount={} USD",
                                        request.getGroupId(), category.getCategoryName(), amountInUsd);

                } catch (RuntimeException e) {
                        log.error("Failed to create payment: correlationId={}", request.getCorrelationId(), e);
                        response.setSuccess(false);
                        response.setErrorCode(
                                        e.getMessage().contains("Exchange rate") ? "EXCHANGE_RATE_UNAVAILABLE"
                                                        : e.getMessage().contains("Category not found")
                                                                        ? "CATEGORY_NOT_FOUND"
                                                                        : "SAVE_FAILED");
                }

                return response;
        }

        private void distributeSharedExpense(
                        String groupId, Category category,
                        BigDecimal totalAmount, LocalDate paymentDate) {

                LocalDate from = paymentRepository
                                .findPreviousSharedPaymentDate(groupId, category.getCategoryId(), paymentDate)
                                .orElse(LocalDate.of(2000, 1, 1));

                List<RoomIncomeProjection> incomes = paymentRepository
                                .getIncomeByRoomBetween(groupId, from, paymentDate);

                BigDecimal totalIncome = incomes.stream()
                                .map(RoomIncomeProjection::getTotalIncome)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                List<Room> rooms = roomRepository.findByGroupId(groupId);

                if (totalIncome.compareTo(BigDecimal.ZERO) == 0) {
                        BigDecimal share = totalAmount.divide(
                                        BigDecimal.valueOf(rooms.size()), 4, RoundingMode.HALF_UP);
                        rooms.forEach(room -> paymentRepository.save(
                                        buildPayment(groupId, room.getRoomNumber(), category, share, paymentDate)));
                        return;
                }

                List<RoomIncomeProjection> sorted = new ArrayList<>(incomes);
                BigDecimal distributed = BigDecimal.ZERO;

                for (int i = 0; i < sorted.size(); i++) {
                        RoomIncomeProjection r = sorted.get(i);
                        BigDecimal share = (i == sorted.size() - 1)
                                        ? totalAmount.subtract(distributed)
                                        : totalAmount.multiply(r.getTotalIncome())
                                                        .divide(totalIncome, 4, RoundingMode.HALF_UP);

                        distributed = distributed.add(share);
                        paymentRepository.save(
                                        buildPayment(groupId, r.getRoomNumber(), category, share, paymentDate));
                }
        }

        private Payment buildPayment(String groupId, String roomNumber,
                        Category category, BigDecimal amount, LocalDate date) {
                Payment p = new Payment();
                p.setPaymentId(UUID.randomUUID());
                p.setGroupId(groupId);
                p.setRoomNumber(roomNumber);
                p.setCategory(category);
                p.setAmount(amount);
                p.setPaymentDate(date);
                return p;
        }
}