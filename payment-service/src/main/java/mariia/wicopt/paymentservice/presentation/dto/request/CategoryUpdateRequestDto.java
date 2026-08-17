package mariia.wicopt.paymentservice.presentation.dto.request;

import lombok.Data;

@Data
public class CategoryUpdateRequestDto {
    private String categoryName;
    private boolean isShared;
}