package mariia.sofiia.payment_service.presentation.messaging;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.sofiia.payment_service.presentation.dto.request.RoiRequestDto;
import mariia.sofiia.payment_service.presentation.dto.request.StatisticsRequestDto;
import mariia.sofiia.payment_service.presentation.dto.response.RoiResponseDto;
import mariia.sofiia.payment_service.presentation.dto.response.StatisticsResponseDto;
import mariia.sofiia.payment_service.service.StatisticsService;

@Slf4j
@Component
@RequiredArgsConstructor
public class StatisticsListener {

    private final StatisticsService statisticsService;
    private final RabbitTemplate rabbitTemplate;

    @Value("${rabbitmq.queues.statistics-response}")
    private String statisticsResponseQueue;

    @Value("${rabbitmq.queues.roi-response}")
    private String roiResponseQueue;

    @RabbitListener(queues = "${rabbitmq.queues.statistics-request}")
    public void handleStatisticsRequest(StatisticsRequestDto request) {
        log.info("Received statistics request: groupId={}, roomNumber={}",
                request.getGroupId(), request.getRoomNumber());
        try {
            StatisticsResponseDto response = statisticsService.getStatistics(request);
            rabbitTemplate.convertAndSend(statisticsResponseQueue, response);
        } catch (Exception e) {
            log.error("Error processing statistics request", e);
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.roi-request}")
    public void handleRoiRequest(RoiRequestDto request) {
        log.info("Received ROI request: groupId={}", request.getGroupId());
        try {
            RoiResponseDto response = statisticsService.getRoi(request);
            log.info("Sending ROI response to RabbitMQ: groupId={}, roomsCount={}, correlationId={}",
                    response.getGroupId(),
                    response.getRooms(),
                    response.getCorrelationId());
            rabbitTemplate.convertAndSend(roiResponseQueue, response);
        } catch (Exception e) {
            log.error("Error processing ROI request", e);
        }
    }
}