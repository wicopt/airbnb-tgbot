package mariia.sofiia.auth.presentation.messaging;

import java.time.OffsetDateTime;
import java.util.List;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.sofiia.auth.application.service.AuthService;
import mariia.sofiia.auth.presentation.dto.request.GenerateInviteRequest;
import mariia.sofiia.auth.presentation.dto.request.GetMembersRequest;
import mariia.sofiia.auth.presentation.dto.request.KickMemberRequest;
import mariia.sofiia.auth.presentation.dto.request.UseInviteRequest;
import mariia.sofiia.auth.presentation.dto.response.GenerateInviteResponse;
import mariia.sofiia.auth.presentation.dto.response.GetMembersResponse;
import mariia.sofiia.auth.presentation.dto.response.KickMemberResponse;
import mariia.sofiia.auth.presentation.dto.response.MemberJoinedEvent;
import mariia.sofiia.auth.presentation.dto.response.UseInviteResponse;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthConsumer {

    private final AuthService authService;
    private final AuthProducer authProducer;

    @RabbitListener(queues = "${rabbitmq.queues.use-invite}")
    public void handleUseInvite(UseInviteRequest request) {
        log.info("Received UseInviteRequest: user={}, code={}",
                request.getUserId(), request.getInviteCode());
        try {
            UseInviteResponse response = authService.useInvite(request);
            authProducer.sendUseInviteResponse(response);

            // Если успех — дополнительно шлём событие для рассылки админам
            if (response.isSuccess()) {
                List<String> adminIds = authService.getAdminIds(response.getGroupId());
                MemberJoinedEvent event = MemberJoinedEvent.builder()
                        .newUserId(response.getUserId())
                        .groupId(response.getGroupId())
                        .adminIds(adminIds)
                        .joinedAt(OffsetDateTime.now())
                        .build();
                authProducer.sendMemberJoinedEvent(event);
            }
        } catch (Exception e) {
            log.error("Error handling UseInviteRequest: {}", e.getMessage(), e);
            authProducer.sendUseInviteResponse(UseInviteResponse.builder()
                    .success(false)
                    .userId(request.getUserId())
                    .errorCode("INTERNAL_ERROR")
                    .build());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.generate-invite}")
    public void handleGenerateInvite(GenerateInviteRequest request) {
        log.info("Received GenerateInviteRequest: user={}, group={}",
                request.getUserId(), request.getGroupId());
        try {
            GenerateInviteResponse response = authService.generateInvite(request);
            authProducer.sendGenerateInviteResponse(response);
        } catch (Exception e) {
            log.error("Error handling GenerateInviteRequest: {}", e.getMessage(), e);
            authProducer.sendGenerateInviteResponse(GenerateInviteResponse.builder()
                    .success(false)
                    .errorCode("INTERNAL_ERROR")
                    .build());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.get-members}")
    public void handleGetMembers(GetMembersRequest request) {
        log.info("Received GetMembersRequest: requester={}, group={}",
                request.getRequesterId(), request.getGroupId());
        try {
            GetMembersResponse response = authService.getMembers(request);
            authProducer.sendGetMembersResponse(response);
        } catch (Exception e) {
            log.error("Error handling GetMembersRequest: {}", e.getMessage(), e);
            authProducer.sendGetMembersResponse(GetMembersResponse.builder()
                    .success(false)
                    .errorCode("INTERNAL_ERROR")
                    .build());
        }
    }

    @RabbitListener(queues = "${rabbitmq.queues.kick-member}")
    public void handleKickMember(KickMemberRequest request) {
        log.info("Received KickMemberRequest: requester={}, target={}, group={}",
                request.getRequesterId(), request.getTargetUserId(), request.getGroupId());
        try {
            KickMemberResponse response = authService.kickMember(request);
            authProducer.sendKickMemberResponse(response);
        } catch (Exception e) {
            log.error("Error handling KickMemberRequest: {}", e.getMessage(), e);
            authProducer.sendKickMemberResponse(KickMemberResponse.builder()
                    .success(false)
                    .errorCode("INTERNAL_ERROR")
                    .build());
        }
    }
}