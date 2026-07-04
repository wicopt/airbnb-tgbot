package mariia.sofiia.payment_service.presentation.dto.request;

import lombok.Data;

@Data
public class CategoryCreateRequestDto {
    private String categoryName;
    private String groupId;         // null = глобальная
    private boolean shared;
}