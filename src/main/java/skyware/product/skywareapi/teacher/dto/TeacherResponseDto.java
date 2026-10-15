package skyware.product.skywareapi.teacher.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TeacherResponseDto {
    private Long id;
    private String name;
    private String email;
    private String phone;
    private Integer otp;
    private Integer verifyotp;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
