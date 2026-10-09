package skyware.product.skywareapi.admin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDto {
    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    private String avatar;
    private Long roleId;
    private String roleName;
    private String roleCode;
    private Boolean isVerified;
    private Boolean otpVerified;
    private String oAuth;
    private String oauthProvider;
    private Long statusId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
