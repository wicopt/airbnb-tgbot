package mariia.wicopt.authservice.presentation.dto.response;

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
public class GetMembersResponse {
    private boolean success;
    private String groupId;
    private List<MemberDto> members;
    private String errorCode;
    private String correlationId;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class MemberDto {
        private String userId;
        private String role;
        private OffsetDateTime joinedAt;
    }
}