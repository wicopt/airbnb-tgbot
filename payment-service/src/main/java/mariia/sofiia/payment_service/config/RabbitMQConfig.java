package mariia.sofiia.payment_service.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queues.statistics-request}")
    private String statisticsRequestQueue;

    @Value("${rabbitmq.queues.statistics-response}")
    private String statisticsResponseQueue;

    @Value("${rabbitmq.queues.roi-request}")
    private String roiRequestQueue;

    @Value("${rabbitmq.queues.roi-response}")
    private String roiResponseQueue;

    @Value("${rabbitmq.queues.payment-request}")
    private String paymentRequestQueue;

    @Value("${rabbitmq.queues.payment-response}")
    private String paymentResponseQueue;

    @Bean
    public Queue statisticsRequestQueue() {
        return new Queue(statisticsRequestQueue, true);
    }

    @Bean
    public Queue statisticsResponseQueue() {
        return new Queue(statisticsResponseQueue, true);
    }

    @Bean
    public Queue roiRequestQueue() {
        return new Queue(roiRequestQueue, true);
    }

    @Bean
    public Queue roiResponseQueue() {
        return new Queue(roiResponseQueue, true);
    }
    @Bean
    public Queue paymentRequestQueue() {
        return new Queue(paymentRequestQueue, true);
    }

    @Bean
    public Queue paymentResponseQueue() {
        return new Queue(paymentResponseQueue, true);
    }
    @Bean
    public Jackson2JsonMessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter());
        return template;
    }
}