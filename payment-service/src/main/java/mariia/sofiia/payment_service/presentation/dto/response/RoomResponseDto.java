package mariia.sofiia.payment_service.presentation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class RoomResponseDto {
    private String roomNumber;
    private String messageName;
}