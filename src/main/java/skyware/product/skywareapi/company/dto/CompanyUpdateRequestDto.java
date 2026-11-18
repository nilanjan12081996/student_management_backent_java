package skyware.product.skywareapi.company.dto;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyUpdateRequestDto {

    private Long id;

    private String name;

    @Email(message = "Valid email is required")
    private String email;

    private String phone;

    private String domain;

    private Integer status;

    private Integer adminApprove;

    private Integer verifyotp;
}
