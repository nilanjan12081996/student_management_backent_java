package skyware.product.skywareapi.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyActivationResponseDto {
    private boolean newlyActivated;
    private String message;
    private CompanyResponseDto company;
}
