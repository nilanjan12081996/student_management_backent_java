package skyware.product.skywareapi.superadmin.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginPayloadDto {

    @NotBlank(message="email required")
    @Email(message = "invalid email format")
    private String email;

    @NotBlank(message="password required")
    private String password;
}
