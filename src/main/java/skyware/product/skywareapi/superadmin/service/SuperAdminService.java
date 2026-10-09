package skyware.product.skywareapi.superadmin.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.config.JwtUtil;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.superadmin.dto.LoginPayloadDto;
import skyware.product.skywareapi.superadmin.dto.SuperAdminDto;
import skyware.product.skywareapi.superadmin.dto.SuperAdminResponseDto;
import skyware.product.skywareapi.superadmin.entity.SuperAdminEntity;
import skyware.product.skywareapi.superadmin.mapper.SuperAdminMapper;
import skyware.product.skywareapi.superadmin.repository.SuperAdminRepository;
import skyware.product.skywareapi.superadmin.dto.ForgotPasswordDto;
import skyware.product.skywareapi.superadmin.dto.ResetPasswordDto;
import skyware.product.skywareapi.superadmin.dto.SuperAdminCreateDto;
import skyware.product.skywareapi.admin.entity.AdminEntity;
import skyware.product.skywareapi.admin.repository.AdminRepository;
import org.springframework.transaction.annotation.Transactional;

import skyware.product.skywareapi.role.repository.RoleRepository;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class SuperAdminService {

    private final SuperAdminRepository superAdminRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;
    private final AdminRepository adminRepository;
    private final RoleRepository roleRepository;

    public SuperAdminResponseDto login(LoginPayloadDto loginPayloadDto){
        Optional<SuperAdminEntity> superAdminEntity = superAdminRepository.findByEmail(loginPayloadDto.getEmail());
        if(superAdminEntity.isPresent()){
            SuperAdminEntity superAdmin = superAdminEntity.get();
            if (!passwordEncoder.matches(loginPayloadDto.getPassword(), superAdmin.getPassword())) {
                throw new ValidationError("Password is wrong");
            }
            SuperAdminDto superAdminDto = SuperAdminMapper.superAdminDto(superAdmin);
            String jwtToken = jwtUtil.generateSuperAdminToken(superAdminDto);
            return SuperAdminResponseDto.builder()
                    .token(jwtToken)
                    .superAdminDto(superAdminDto)
                    .role("SUPERADMIN")
                    .build();
        }

        Optional<AdminEntity> adminOpt = adminRepository.findByEmail(loginPayloadDto.getEmail());
        if(adminOpt.isEmpty()){
            throw new ValidationError("email not found");
        }

        AdminEntity admin = adminOpt.get();
        if (!passwordEncoder.matches(loginPayloadDto.getPassword(), admin.getPassword())) {
            throw new ValidationError("Password is wrong");
        }

        if (admin.getIsVerified() == null || !admin.getIsVerified()) {
            throw new ValidationError("Account is not verified");
        }

        skyware.product.skywareapi.admin.dto.AdminDto adminDto = skyware.product.skywareapi.admin.dto.AdminDto.builder()
                .id(admin.getId())
                .fullName(admin.getFullName())
                .email(admin.getEmail())
                .mobile(admin.getMobile())
                .avatar(admin.getAvatar())
                .roleId(admin.getRoleId())
                .isVerified(admin.getIsVerified())
                .otpVerified(admin.getOtpVerified())
                .oAuth(admin.getOAuth())
                .oauthProvider(admin.getOauthProvider())
                .statusId(admin.getStatusId())
                .isActive(admin.getIsActive())
                .createdAt(admin.getCreatedAt())
                .updatedAt(admin.getUpdatedAt())
                .build();

        String roleName = "ADMIN";
        String roleCode = "ADMIN";
        if (admin.getRoleId() != null) {
            Optional<skyware.product.skywareapi.role.entity.RoleEntity> roleOpt = roleRepository.findById(admin.getRoleId());
            if (roleOpt.isPresent()) {
                roleName = roleOpt.get().getName();
                roleCode = roleOpt.get().getCode();
                adminDto.setRoleName(roleName);
                adminDto.setRoleCode(roleCode);
            }
        }

        String jwtToken = jwtUtil.generateToken(admin.getEmail(), roleCode, admin.getId());

        return SuperAdminResponseDto.builder()
                .token(jwtToken)
                .adminDto(adminDto)
                .role(roleCode)
                .build();
    }

    @Transactional
    public void addAdmin(SuperAdminCreateDto createDto) {
        if (adminRepository.findByEmail(createDto.getEmail()).isPresent()) {
            throw new ValidationError("Email already exists");
        }

        // Generate random password
        String generatedPassword = java.util.UUID.randomUUID().toString().substring(0, 8);
        String encryptedPassword = passwordEncoder.encode(generatedPassword);

        AdminEntity newAdmin = AdminEntity.builder()
                .fullName(createDto.getFullName())
                .email(createDto.getEmail())
                .mobile(createDto.getMobile())
                .statusId(createDto.getStatusId())
                .roleId(createDto.getRoleId())
                .password(encryptedPassword)
                .isVerified(false)
                .build();

        adminRepository.save(newAdmin);

        // Encrypt email for verification link
        String encryptedEmail = java.util.Base64.getEncoder().encodeToString(createDto.getEmail().getBytes());
        String verifyLink = "http://localhost:8080/skyware/api/superadmin/verify?token=" + encryptedEmail;

        emailService.sendVerificationEmail(createDto.getEmail(), generatedPassword, verifyLink);
    }

    public String verifyAdmin(String token) {
        String email;
        try {
            email = new String(java.util.Base64.getDecoder().decode(token));
        } catch (Exception e) {
            throw new ValidationError("Invalid verification token");
        }

        Optional<AdminEntity> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isEmpty()) {
            throw new ValidationError("Admin not found");
        }

        AdminEntity admin = adminOpt.get();
        if (Boolean.TRUE.equals(admin.getIsVerified())) {
            return "you are already verified";
        }

        admin.setIsVerified(true);
        adminRepository.save(admin);

        return "Verification successful";
    }

    public String forgotPassword(ForgotPasswordDto dto) {
        String email = dto.getEmail();
        boolean exists = superAdminRepository.findByEmail(email).isPresent() || adminRepository.findByEmail(email).isPresent();

        if (!exists) {
            throw new ValidationError("Email not found");
        }

        String resetToken = java.util.Base64.getEncoder().encodeToString(email.getBytes());
        // Link to the frontend or the backend HTML form
        String resetLink = "http://localhost:8080/skyware/api/superadmin/reset-password-page?token=" + resetToken;

        emailService.sendResetPasswordEmail(email, resetLink);
        return resetLink;
    }

    public void resetPassword(ResetPasswordDto dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmPassword())) {
            throw new ValidationError("Passwords do not match");
        }

        String email;
        try {
            email = new String(java.util.Base64.getDecoder().decode(dto.getToken()));
        } catch (Exception e) {
            throw new ValidationError("Invalid token");
        }

        Optional<SuperAdminEntity> superAdminOpt = superAdminRepository.findByEmail(email);
        if (superAdminOpt.isPresent()) {
            SuperAdminEntity sa = superAdminOpt.get();
            sa.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            superAdminRepository.save(sa);
            return;
        }

        Optional<AdminEntity> adminOpt = adminRepository.findByEmail(email);
        if (adminOpt.isPresent()) {
            AdminEntity admin = adminOpt.get();
            admin.setPassword(passwordEncoder.encode(dto.getNewPassword()));
            adminRepository.save(admin);
            return;
        }

        throw new ValidationError("User not found");
    }
}
