package mariia.sofiia.payment_service.presentation.dto.request;

import lombok.Data;

@Data
public class CategoryUpdateRequestDto {
    private String categoryName;
    private boolean isShared;
}