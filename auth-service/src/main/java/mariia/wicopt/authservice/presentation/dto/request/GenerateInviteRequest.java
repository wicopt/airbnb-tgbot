package mariia.wicopt.authservice.presentation.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GenerateInviteRequest {
    private String userId;
    private String groupId;
    private Integer expiresInHours;
    private String correlationId;
}