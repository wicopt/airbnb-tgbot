package mariia.sofiia.payment_service.presentation.dto.request;

import java.time.LocalDate;

import lombok.Data;

@Data
public class StatisticsRequestDto {
    private String groupId;
    private String roomNumber;
    private LocalDate from;   // nullable
    private LocalDate to;     // nullable
    private String correlationId;   
}