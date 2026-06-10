package mariia.sofiia.auth.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KickMemberRequest {
    private String requesterId;
    private String groupId;
    private String targetUserId;
    private String correlationId;
}