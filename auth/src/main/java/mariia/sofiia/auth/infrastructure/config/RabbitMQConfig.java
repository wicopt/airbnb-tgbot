package mariia.sofiia.auth.infrastructure.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queues.use-invite}")
    private String useInviteQueue;

    @Value("${rabbitmq.queues.generate-invite}")
    private String generateInviteQueue;

    @Value("${rabbitmq.queues.get-members}")
    private String getMembersQueue;

    @Value("${rabbitmq.queues.kick-member}")
    private String kickMemberQueue;

    @Value("${rabbitmq.queues.use-invite-response}")
    private String useInviteResponseQueue;

    @Value("${rabbitmq.queues.generate-invite-response}")
    private String generateInviteResponseQueue;

    @Value("${rabbitmq.queues.get-members-response}")
    private String getMembersResponseQueue;

    @Value("${rabbitmq.queues.kick-member-response}")
    private String kickMemberResponseQueue;

    @Value("${rabbitmq.queues.member-joined-event}")
    private String memberJoinedEventQueue;

    // Очереди
    @Bean public Queue useInviteQueue() { return new Queue(useInviteQueue, true); }
    @Bean public Queue generateInviteQueue() { return new Queue(generateInviteQueue, true); }
    @Bean public Queue getMembersQueue() { return new Queue(getMembersQueue, true); }
    @Bean public Queue kickMemberQueue() { return new Queue(kickMemberQueue, true); }
    @Bean public Queue useInviteResponseQueue() { return new Queue(useInviteResponseQueue, true); }
    @Bean public Queue generateInviteResponseQueue() { return new Queue(generateInviteResponseQueue, true); }
    @Bean public Queue getMembersResponseQueue() { return new Queue(getMembersResponseQueue, true); }
    @Bean public Queue kickMemberResponseQueue() { return new Queue(kickMemberResponseQueue, true); }
    @Bean public Queue memberJoinedEventQueue() { return new Queue(memberJoinedEventQueue, true); }

    // ObjectMapper для JSON
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.LOWER_CAMEL_CASE);
        return mapper;
    }

    // MessageConverter
    @Bean
    public MessageConverter messageConverter(ObjectMapper objectMapper) {
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    // RabbitTemplate
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory, 
                                         MessageConverter messageConverter) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(messageConverter);
        return template;
    }

    // Listener Container Factory
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory(
            ConnectionFactory connectionFactory,
            MessageConverter messageConverter) {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory);
        factory.setMessageConverter(messageConverter);
        return factory;
    }
}