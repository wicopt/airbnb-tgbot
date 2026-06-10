package mariia.sofiia.auth.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UseInviteRequest {
    private String userId;
    private String inviteCode;
    private String correlationId;
}