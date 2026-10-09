package skyware.product.skywareapi.superadmin.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "superadmins")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SuperAdminEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password")
    private String password;

    @Column(name = "mobile", length = 20)
    private String mobile;

    @Column(name = "avatar")
    private String avatar;

    @Column(name = "otp")
    private Integer otp;

    @Column(name = "otp_expire")
    private LocalDateTime otpExpire;

    @Builder.Default
    @Column(name = "otp_verified")
    private Boolean otpVerified = false;

    @Builder.Default
    @Column(name = "is_verified")
    private Boolean isVerified = false;

    @Column(name = "o_auth")
    private String oAuth;

    @Column(name = "oauth_provider")
    private String oauthProvider;

    @Column(name = "status_id")
    private Long statusId;

    @Builder.Default
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
