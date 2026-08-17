package mariia.sofiia.payment_service.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.sofiia.payment_service.infrastructure.entities.Payment;
import mariia.sofiia.payment_service.infrastructure.entities.Room;
import mariia.sofiia.payment_service.infrastructure.projection.RoomPaymentAggProjection;
import mariia.sofiia.payment_service.infrastructure.repository.PaymentRepository;
import mariia.sofiia.payment_service.infrastructure.repository.RoomRepository;
import mariia.sofiia.payment_service.presentation.dto.request.StatisticsRequestDto;
import mariia.sofiia.payment_service.presentation.dto.response.PaymentResponseDto;
import mariia.sofiia.payment_service.presentation.dto.response.StatisticsResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatisticsService {

        private final PaymentRepository paymentRepository;
        private final RoomRepository roomRepository;

        public StatisticsResponseDto getStatistics(StatisticsRequestDto request) {
                List<Payment> payments;

                if (request.getFrom() != null && request.getTo() != null) {
                        payments = paymentRepository.findByGroupIdAndRoomNumberAndPaymentDateBetween(
                                        request.getGroupId(), request.getRoomNumber(),
                                        request.getFrom(), request.getTo());
                } else {
                        payments = paymentRepository.findByGroupIdAndRoomNumber(
                                        request.getGroupId(), request.getRoomNumber());
                }

                BigDecimal totalIncome = payments.stream()
                                .map(Payment::getAmount)
                                .filter(a -> a.compareTo(BigDecimal.ZERO) > 0)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal totalExpenses = payments.stream()
                                .map(Payment::getAmount)
                                .filter(a -> a.compareTo(BigDecimal.ZERO) < 0)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);

                BigDecimal netProfit = totalIncome.add(totalExpenses);

                List<PaymentResponseDto> paymentDtos = payments.stream()
                                .map(p -> {
                                        PaymentResponseDto dto = new PaymentResponseDto();
                                        dto.setPaymentId(p.getPaymentId().toString());
                                        dto.setAmount(p.getAmount());
                                        dto.setPaymentDate(p.getPaymentDate());
                                        dto.setCategoryId(p.getCategory().getCategoryId());
                                        dto.setCategoryName(p.getCategory().getCategoryName());
                                        return dto;
                                })
                                .collect(Collectors.toList());

                StatisticsResponseDto response = new StatisticsResponseDto();
                response.setGroupId(request.getGroupId());
                response.setRoomNumber(request.getRoomNumber());
                response.setTotalIncome(totalIncome);
                response.setTotalExpenses(totalExpenses);
                response.setNetProfit(netProfit);
                response.setPayments(paymentDtos);
                response.setCorrelationId(request.getCorrelationId());
                return response;
        }

}