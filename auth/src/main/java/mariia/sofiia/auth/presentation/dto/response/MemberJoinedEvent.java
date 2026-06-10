package mariia.sofiia.auth.presentation.dto.response;

import java.time.OffsetDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberJoinedEvent {
    private String newUserId;
    private String groupId;
    private List<String> adminIds;
    private OffsetDateTime joinedAt;
    private String correlationId;
}