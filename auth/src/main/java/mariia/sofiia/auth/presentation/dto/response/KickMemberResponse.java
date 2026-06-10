package mariia.sofiia.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class KickMemberResponse {
    private boolean success;
    private String groupId;
    private String kickedUserId;
    private String errorCode;
    private String correlationId;
}