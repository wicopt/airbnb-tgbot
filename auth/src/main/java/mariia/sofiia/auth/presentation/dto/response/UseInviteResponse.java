package mariia.sofiia.auth.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UseInviteResponse {
    private boolean success;
    private String userId;
    private String groupId;
    private String errorCode;
    private String correlationId;
}