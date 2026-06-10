package mariia.sofiia.payment_service.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.sofiia.payment_service.infrastructure.client.ExchangeRateClient;
import mariia.sofiia.payment_service.infrastructure.entities.Payment;
import mariia.sofiia.payment_service.infrastructure.repository.PaymentRepository;
import mariia.sofiia.payment_service.presentation.dto.request.PaymentCreateRequestDto;
import mariia.sofiia.payment_service.presentation.dto.response.PaymentResponseDto;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ExchangeRateClient exchangeRateClient;

    @Transactional
    public PaymentResponseDto createPayment(PaymentCreateRequestDto request) {
        PaymentResponseDto response = new PaymentResponseDto();
        response.setCorrelationId(request.getCorrelationId());

        try {
            String currency = request.getCurrency() != null
                    ? request.getCurrency().toUpperCase()
                    : "USD";

            BigDecimal originalAmount = request.getAmount();
            BigDecimal amountInUsd = exchangeRateClient.convertToUsd(originalAmount, currency);

            Payment payment = new Payment();
            payment.setPaymentId(UUID.randomUUID());
            payment.setGroupId(request.getGroupId());
            payment.setRoomNumber(request.getRoomNumber());
            payment.setCategory(request.getCategory());
            payment.setAmount(amountInUsd);
            payment.setPaymentDate(
                    request.getPaymentDate() != null ? request.getPaymentDate() : LocalDate.now()
            );

            paymentRepository.save(payment);

            response.setSuccess(true);
            response.setPaymentId(payment.getPaymentId().toString());
            response.setGroupId(payment.getGroupId());
            response.setRoomNumber(payment.getRoomNumber());
            response.setCategory(payment.getCategory());
            response.setAmount(amountInUsd);
            response.setOriginalAmount(originalAmount);
            response.setOriginalCurrency(currency);
            response.setPaymentDate(payment.getPaymentDate());

            log.info("Payment saved: id={} room={} original={} {} converted={} USD",
                    payment.getPaymentId(), payment.getRoomNumber(),
                    originalAmount, currency, amountInUsd);

        } catch (RuntimeException e) {
            log.error("Failed to save payment: correlationId={}", request.getCorrelationId(), e);
            response.setSuccess(false);
            response.setErrorCode(
                    e.getMessage().contains("Exchange rate") ? "EXCHANGE_RATE_UNAVAILABLE" : "SAVE_FAILED"
            );
        }

        return response;
    }
}