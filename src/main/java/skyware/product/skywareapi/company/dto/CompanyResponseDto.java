package skyware.product.skywareapi.company.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompanyResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private String domain;
    private Integer otp;
    private Integer verifyotp;
    private Integer status;
    private Integer adminApprove;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
