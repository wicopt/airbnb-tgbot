package mariia.wicopt.authservice.presentation.messaging;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.wicopt.authservice.presentation.dto.response.GenerateInviteResponse;
import mariia.wicopt.authservice.presentation.dto.response.GetMembersResponse;
import mariia.wicopt.authservice.presentation.dto.response.KickMemberResponse;
import mariia.wicopt.authservice.presentation.dto.response.MemberJoinedEvent;
import mariia.wicopt.authservice.presentation.dto.response.UseInviteResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthProducer {

    private final RabbitTemplate rabbitTemplate;

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

    public void sendUseInviteResponse(UseInviteResponse response) {
        log.info("Sending UseInviteResponse for user {}: success={}",
                response.getUserId(), response.isSuccess());
        rabbitTemplate.convertAndSend(useInviteResponseQueue, response);
    }

    public void sendGenerateInviteResponse(GenerateInviteResponse response) {
        log.info("Sending GenerateInviteResponse for group {}: success={}",
                response.getGroupId(), response.isSuccess());
        rabbitTemplate.convertAndSend(generateInviteResponseQueue, response);
    }

    public void sendGetMembersResponse(GetMembersResponse response) {
        log.info("Sending GetMembersResponse for group {}: success={}",
                response.getGroupId(), response.isSuccess());
        rabbitTemplate.convertAndSend(getMembersResponseQueue, response);
    }

    public void sendKickMemberResponse(KickMemberResponse response) {
        log.info("Sending KickMemberResponse for group {}: success={}",
                response.getGroupId(), response.isSuccess());
        rabbitTemplate.convertAndSend(kickMemberResponseQueue, response);
    }

    public void sendMemberJoinedEvent(MemberJoinedEvent event) {
        log.info("Sending MemberJoinedEvent: user {} joined group {}, notifying {} admins",
                event.getNewUserId(), event.getGroupId(), event.getAdminIds().size());
        rabbitTemplate.convertAndSend(memberJoinedEventQueue, event);
    }
}