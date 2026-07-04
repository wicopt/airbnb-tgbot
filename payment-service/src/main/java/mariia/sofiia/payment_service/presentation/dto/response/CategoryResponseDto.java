package mariia.sofiia.payment_service.presentation.dto.response;

import lombok.Data;

@Data
public class CategoryResponseDto {
    private Long categoryId;
    private String categoryName;
    private String groupId;
    private boolean isShared;
}