package mariia.wicopt.authservice.presentation.dto.response;

import java.time.OffsetDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerateInviteResponse {
    private boolean success;
    private String inviteCode;
    private String groupId;
    private OffsetDateTime expiresAt;
    private String errorCode;
    private String correlationId;
}