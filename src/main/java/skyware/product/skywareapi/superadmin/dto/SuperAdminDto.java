package skyware.product.skywareapi.superadmin.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminDto {

    private Long id;
    private String fullName;
    private String email;
    private String mobile;
    private String avatar;
    private String oAuth;
    private String oauthProvider;
    private Long statusId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

