package mariia.sofiia.payment_service.presentation.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class RoiResponseDto {
    private String groupId;
    private List<RoomRoiResponseDto> rooms;
    private String correlationId; 
}