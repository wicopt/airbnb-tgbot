package mariia.wicopt.paymentservice.presentation.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.wicopt.paymentservice.presentation.dto.request.PaymentCreateRequestDto;
import mariia.wicopt.paymentservice.presentation.dto.response.PaymentResponseDto;
import mariia.wicopt.paymentservice.service.PaymentService;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentListener {

    private final PaymentService paymentService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queues.payment-response}")
    private String paymentResponseQueue;

    @RabbitListener(queues = "${rabbitmq.queues.payment-request}")
    public void handlePaymentRequest(PaymentCreateRequestDto request) {
        try {
            PaymentResponseDto response = paymentService.createPayment(request);
            rabbitTemplate.convertAndSend(paymentResponseQueue, response);
        } catch (Exception e) {
            log.error("Error processing payment request", e);
        }
    }
}