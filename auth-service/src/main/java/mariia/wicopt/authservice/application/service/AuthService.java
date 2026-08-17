package mariia.wicopt.authservice.application.service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import mariia.wicopt.authservice.infrastructure.config.ErrorCode;
import mariia.wicopt.authservice.infrastructure.entity.Group;
import mariia.wicopt.authservice.infrastructure.entity.InviteCode;
import mariia.wicopt.authservice.infrastructure.entity.UserGroup;
import mariia.wicopt.authservice.infrastructure.enums.UserRole;
import mariia.wicopt.authservice.infrastructure.repository.GroupRepository;
import mariia.wicopt.authservice.infrastructure.repository.InviteCodeRepository;
import mariia.wicopt.authservice.infrastructure.repository.UserGroupRepository;
import mariia.wicopt.authservice.presentation.dto.request.GenerateInviteRequest;
import mariia.wicopt.authservice.presentation.dto.request.GetMembersRequest;
import mariia.wicopt.authservice.presentation.dto.request.KickMemberRequest;
import mariia.wicopt.authservice.presentation.dto.request.UseInviteRequest;
import mariia.wicopt.authservice.presentation.dto.response.GenerateInviteResponse;
import mariia.wicopt.authservice.presentation.dto.response.GetMembersResponse;
import mariia.wicopt.authservice.presentation.dto.response.KickMemberResponse;
import mariia.wicopt.authservice.presentation.dto.response.UseInviteResponse;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

        private final GroupRepository groupRepository;
        private final UserGroupRepository userGroupRepository;
        private final InviteCodeRepository inviteCodeRepository;

        @Transactional
        public UseInviteResponse useInvite(UseInviteRequest request) {
                log.info("=== useInvite called ===");
                log.info("Request: userId={}, inviteCode={}, correlationId={}",
                                request.getUserId(), request.getInviteCode(), request.getCorrelationId());

                // 1. Ищем инвайт-код
                Optional<InviteCode> inviteOpt = inviteCodeRepository.findByCode(request.getInviteCode());

                if (inviteOpt.isEmpty()) {
                        log.warn("Invite code not found: {}", request.getInviteCode());
                        return UseInviteResponse.builder()
                                        .success(false)
                                        .userId(request.getUserId())
                                        .errorCode(ErrorCode.INVITE_NOT_FOUND)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                InviteCode invite = inviteOpt.get();
                log.info("Found invite: code={}, groupId={}", invite.getCode(), invite.getGroup().getGroupId());

                // 2. Проверяем не использован ли
                if (invite.getUsedAt() != null) {
                        log.warn("Invite already used: code={}, usedBy={}", invite.getCode(), invite.getUsedBy());
                        return UseInviteResponse.builder()
                                        .success(false)
                                        .userId(request.getUserId())
                                        .errorCode(ErrorCode.INVITE_ALREADY_USED)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                // 3. Проверяем срок действия
                if (invite.getExpiresAt() != null && invite.getExpiresAt().isBefore(OffsetDateTime.now())) {
                        log.warn("Invite expired: code={}, expiresAt={}", invite.getCode(), invite.getExpiresAt());
                        return UseInviteResponse.builder()
                                        .success(false)
                                        .userId(request.getUserId())
                                        .errorCode(ErrorCode.INVITE_EXPIRED)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                String groupId = invite.getGroup().getGroupId();

                // 4. Проверяем не в группе ли уже
                if (userGroupRepository.existsByUserIdAndGroup_GroupId(request.getUserId(), groupId)) {
                        log.warn("User {} is already in group {}", request.getUserId(), groupId);
                        return UseInviteResponse.builder()
                                        .success(false)
                                        .userId(request.getUserId())
                                        .errorCode(ErrorCode.ALREADY_IN_GROUP)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                // 5. Добавляем в группу
                log.info("Adding user {} to group {}", request.getUserId(), groupId);
                UserGroup newMember = UserGroup.builder()
                                .userId(request.getUserId())
                                .group(invite.getGroup())
                                .role(UserRole.member)
                                .joinedAt(OffsetDateTime.now())
                                .build();
                userGroupRepository.save(newMember);

                // 6. Помечаем инвайт использованным
                invite.setUsedAt(OffsetDateTime.now());
                invite.setUsedBy(request.getUserId());
                inviteCodeRepository.save(invite);

                log.info("User {} successfully joined group {}", request.getUserId(), groupId);

                return UseInviteResponse.builder()
                                .success(true)
                                .userId(request.getUserId())
                                .groupId(groupId)
                                .correlationId(request.getCorrelationId())
                                .build();
        }

        @Transactional
        public GenerateInviteResponse generateInvite(GenerateInviteRequest request) {
                log.info("=== generateInvite called ===");
                log.info("Request: userId={}, groupId={}, expiresInHours={}, correlationId={}",
                                request.getUserId(), request.getGroupId(), request.getExpiresInHours(),
                                request.getCorrelationId());

                // 1. Определяем groupId: если не передан, берем из БД по userId (как в
                // getMembers)
                String groupId = request.getGroupId();
                String userId = request.getUserId();

                if (groupId == null || groupId.isBlank()) {
                        log.info("groupId not provided, finding by userId={}", userId);
                        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);

                        if (userGroups.isEmpty()) {
                                log.warn("User {} is not a member of any group", userId);
                                return GenerateInviteResponse.builder()
                                                .success(false)
                                                .errorCode(ErrorCode.GROUP_NOT_FOUND)
                                                .correlationId(request.getCorrelationId())
                                                .build();
                        }
                        groupId = userGroups.get(0).getGroup().getGroupId();
                        log.info("Found groupId={} for userId={}", groupId, userId);
                }

                // 2. Проверяем что группа существует
                log.info("Checking if group exists: groupId={}", groupId);
                Optional<Group> groupOpt = groupRepository.findById(groupId);
                if (groupOpt.isEmpty()) {
                        log.warn("Group not found: groupId={}", groupId);
                        return GenerateInviteResponse.builder()
                                        .success(false)
                                        .errorCode(ErrorCode.GROUP_NOT_FOUND)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                // 3. Проверяем что requester — админ
                log.info("Checking if user {} is admin of group {}", userId, groupId);
                Optional<UserGroup> requesterOpt = userGroupRepository
                                .findByUserIdAndGroup_GroupId(userId, groupId);

                if (requesterOpt.isEmpty() || requesterOpt.get().getRole() != UserRole.admin) {
                        log.warn("User {} is not admin of group {}. Role: {}",
                                        userId, groupId, requesterOpt.map(UserGroup::getRole).orElse(null));
                        return GenerateInviteResponse.builder()
                                        .success(false)
                                        .errorCode(ErrorCode.ACCESS_DENIED)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                // 4. Генерируем код
                String code = UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
                OffsetDateTime expiresAt = request.getExpiresInHours() != null
                                ? OffsetDateTime.now().plusHours(request.getExpiresInHours())
                                : null;

                log.info("Generating invite code: code={}, expiresAt={}", code, expiresAt);

                InviteCode invite = InviteCode.builder()
                                .code(code)
                                .group(groupOpt.get())
                                .createdBy(userId)
                                .expiresAt(expiresAt)
                                .build();
                inviteCodeRepository.save(invite);

                log.info("Invite code generated successfully: code={} for group {}", code, groupId);

                return GenerateInviteResponse.builder()
                                .success(true)
                                .inviteCode(code)
                                .groupId(groupId)
                                .expiresAt(expiresAt)
                                .correlationId(request.getCorrelationId())
                                .build();
        }

        @Transactional(readOnly = true)
        public GetMembersResponse getMembers(GetMembersRequest request) {
                log.info("=== getMembers called ===");
                log.info("Request: requesterId={}, groupId={}, correlationId={}",
                                request.getRequesterId(), request.getGroupId(), request.getCorrelationId());

                // 1. Определяем groupId: если не передан, берем из БД по requesterId
                String groupId = request.getGroupId();
                String userId = request.getRequesterId();
                String requesterId = request.getRequesterId();

                if (groupId == null || groupId.isBlank()) {
                        log.info("groupId not provided, finding by userId={}", userId);
                        List<UserGroup> userGroups = userGroupRepository.findByUserId(userId);

                        if (userGroups.isEmpty()) {
                                log.warn("User {} is not a member of any group", userId);
                                return GetMembersResponse.builder()
                                                .success(false)
                                                .errorCode(ErrorCode.GROUP_NOT_FOUND)
                                                .correlationId(request.getCorrelationId())
                                                .build();
                        }
                        groupId = userGroups.get(0).getGroup().getGroupId();
                        log.info("Found groupId={} for requesterId={}", groupId, requesterId);
                }

                // 2. Проверяем что requester в группе (кроме системных вызовов)
                if (!"system".equals(requesterId)) {
                        log.info("Checking if requester {} is in group {}", requesterId, groupId);
                        if (!userGroupRepository.existsByUserIdAndGroup_GroupId(requesterId, groupId)) {
                                log.warn("Requester {} is not in group {}", requesterId, groupId);
                                return GetMembersResponse.builder()
                                                .success(false)
                                                .errorCode(ErrorCode.ACCESS_DENIED)
                                                .correlationId(request.getCorrelationId())
                                                .build();
                        }
                } else {
                        log.info("System request — skipping membership check for group {}", groupId);
                }

                // 3. Получаем всех участников
                log.info("Fetching all members for groupId={}", groupId);
                List<GetMembersResponse.MemberDto> members = userGroupRepository
                                .findByGroup_GroupId(groupId)
                                .stream()
                                .map(ug -> {
                                        log.debug("Member: userId={}, role={}", ug.getUserId(), ug.getRole());
                                        return GetMembersResponse.MemberDto.builder()
                                                        .userId(ug.getUserId())
                                                        .role(ug.getRole().name())
                                                        .joinedAt(ug.getJoinedAt())
                                                        .build();
                                })
                                .toList();

                log.info("Returning {} members for groupId={}", members.size(), groupId);

                return GetMembersResponse.builder()
                                .success(true)
                                .groupId(groupId)
                                .members(members)
                                .correlationId(request.getCorrelationId())
                                .build();
        }

        @Transactional
        public KickMemberResponse kickMember(KickMemberRequest request) {
                log.info("=== kickMember called ===");
                log.info("Request: requesterId={}, groupId={}, targetUserId={}, correlationId={}",
                                request.getRequesterId(), request.getGroupId(), request.getTargetUserId(),
                                request.getCorrelationId());

                // 1. Определяем groupId: если не передан, берем из БД по requesterId (как в
                // getMembers)
                String groupId = request.getGroupId();
                String requesterId = request.getRequesterId();

                if (groupId == null || groupId.isBlank()) {
                        log.info("groupId not provided, finding by requesterId={}", requesterId);
                        List<UserGroup> userGroups = userGroupRepository.findByUserId(requesterId);

                        if (userGroups.isEmpty()) {
                                log.warn("Requester {} is not a member of any group", requesterId);
                                return KickMemberResponse.builder()
                                                .success(false)
                                                .errorCode(ErrorCode.GROUP_NOT_FOUND)
                                                .correlationId(request.getCorrelationId())
                                                .build();
                        }
                        groupId = userGroups.get(0).getGroup().getGroupId();
                        log.info("Found groupId={} for requesterId={}", groupId, requesterId);
                }

                // 2. Проверяем что requester — админ
                log.info("Checking if requester {} is admin of group {}", requesterId, groupId);
                Optional<UserGroup> requesterOpt = userGroupRepository
                                .findByUserIdAndGroup_GroupId(requesterId, groupId);

                if (requesterOpt.isEmpty() || requesterOpt.get().getRole() != UserRole.admin) {
                        log.warn("Requester {} is not admin of group {}. Role: {}",
                                        requesterId, groupId, requesterOpt.map(UserGroup::getRole).orElse(null));
                        return KickMemberResponse.builder()
                                        .success(false)
                                        .errorCode(ErrorCode.ACCESS_DENIED)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                // 3. Проверяем что target в группе
                log.info("Checking if target user {} is in group {}", request.getTargetUserId(), groupId);
                if (!userGroupRepository.existsByUserIdAndGroup_GroupId(request.getTargetUserId(), groupId)) {
                        log.warn("Target user {} is not in group {}", request.getTargetUserId(), groupId);
                        return KickMemberResponse.builder()
                                        .success(false)
                                        .errorCode(ErrorCode.USER_NOT_IN_GROUP)
                                        .correlationId(request.getCorrelationId())
                                        .build();
                }

                // 4. Удаляем
                log.info("Removing user {} from group {}", request.getTargetUserId(), groupId);
                userGroupRepository.deleteByUserIdAndGroup_GroupId(request.getTargetUserId(), groupId);
                log.info("User {} successfully removed from group {}", request.getTargetUserId(), groupId);

                return KickMemberResponse.builder()
                                .success(true)
                                .groupId(groupId)
                                .kickedUserId(request.getTargetUserId())
                                .correlationId(request.getCorrelationId())
                                .build();
        }

        // Вспомогательный метод — получить айдишники всех админов группы
        // Нужен для MemberJoinedEvent
        @Transactional(readOnly = true)
        public List<String> getAdminIds(String groupId) {
                return userGroupRepository
                                .findByGroup_GroupIdAndRole(groupId, UserRole.admin)
                                .stream()
                                .map(UserGroup::getUserId)
                                .toList();
        }
}