package skyware.product.skywareapi.superadmin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuperAdminResponseDto {

    private SuperAdminDto superAdminDto;

    private skyware.product.skywareapi.admin.dto.AdminDto adminDto;

    private String token;
    
    private String role;
}
