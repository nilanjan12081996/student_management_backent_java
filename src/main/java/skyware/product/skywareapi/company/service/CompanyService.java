package skyware.product.skywareapi.company.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.company.dto.CompanyRegisterRequestDto;
import skyware.product.skywareapi.company.dto.CompanyResponseDto;
import skyware.product.skywareapi.company.dto.CompanyVerifyOtpRequestDto;
import skyware.product.skywareapi.company.entity.CompanyEntity;
import skyware.product.skywareapi.company.repository.CompanyRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyEmailService companyEmailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public CompanyResponseDto register(CompanyRegisterRequestDto dto) {
        // Check if email already exists
        Optional<CompanyEntity> existingEmailOpt = companyRepository.findByEmail(dto.getEmail());
        if (existingEmailOpt.isPresent()) {
            CompanyEntity existing = existingEmailOpt.get();
            if (existing.getVerifyotp() != null && existing.getVerifyotp() == 1) {
                throw new ValidationError("Company with this email is already registered and verified");
            }

            // If phone belongs to another company
            Optional<CompanyEntity> existingPhoneOpt = companyRepository.findByPhone(dto.getPhone());
            if (existingPhoneOpt.isPresent() && !existingPhoneOpt.get().getId().equals(existing.getId())) {
                throw new ValidationError("Phone number is already registered with another company");
            }

            // Unverified company re-registering: generate new OTP and resend
            int otp = 100000 + random.nextInt(900000);
            existing.setName(dto.getName());
            existing.setPhone(dto.getPhone());
            existing.setDomain(dto.getDomain());
            existing.setOtp(otp);
            existing.setVerifyotp(0);
            existing.setStatus(0);
            CompanyEntity saved = companyRepository.save(existing);

            companyEmailService.sendOtpEmail(saved.getEmail(), saved.getName(), otp);
            log.info("Company [{}] unverified registration updated, new OTP sent.", saved.getEmail());

            return mapToDto(saved);
        }

        // Check if phone number already exists
        if (companyRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new ValidationError("Phone number is already registered with another company");
        }

        // Generate 6-digit OTP
        int otp = 100000 + random.nextInt(900000);

        CompanyEntity newCompany = CompanyEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .domain(dto.getDomain())
                .otp(otp)
                .verifyotp(0)
                .status(0)
                .adminApprove(0)
                .build();

        CompanyEntity saved = companyRepository.save(newCompany);

        // Send OTP email
        companyEmailService.sendOtpEmail(saved.getEmail(), saved.getName(), otp);
        log.info("Company [{}] registered successfully, OTP sent.", saved.getEmail());

        return mapToDto(saved);
    }

    @Transactional
    public CompanyResponseDto verifyOtp(CompanyVerifyOtpRequestDto dto) {
        CompanyEntity company = companyRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ValidationError("Company not found with email: " + dto.getEmail()));

        if (company.getVerifyotp() != null && company.getVerifyotp() == 1) {
            throw new ValidationError("Company is already verified");
        }

        if (company.getOtp() == null) {
            throw new ValidationError("No OTP found. Please register or request OTP again.");
        }

        if (!company.getOtp().equals(dto.getOtp())) {
            throw new ValidationError("Invalid OTP. Please enter the correct OTP.");
        }

        // OTP matched, mark as verified (keep otp stored in database)
        company.setVerifyotp(1);
        CompanyEntity updated = companyRepository.save(company);

        log.info("Company [{}] verified successfully via OTP.", updated.getEmail());

        // Generate activation link and send email with 'Activate your account' button
        String encryptedEmail = generateActivationToken(updated.getEmail());
        String activationLink = getActivationLink(encryptedEmail);
        companyEmailService.sendActivationEmail(updated.getEmail(), updated.getName(), activationLink);

        return mapToDto(updated);
    }

    public String generateActivationToken(String email) {
        return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(email.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }

    public String getActivationLink(String token) {
        return "http://localhost:8080/skyware/api/company/activate?token=" + token;
    }

    public void sendActivationEmailByEmail(String email) {
        CompanyEntity company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationError("Company not found with email: " + email));

        if (company.getStatus() != null && company.getStatus() == 1) {
            throw new ValidationError("Company account is already activated");
        }

        String encryptedEmail = generateActivationToken(company.getEmail());
        String activationLink = getActivationLink(encryptedEmail);
        companyEmailService.sendActivationEmail(company.getEmail(), company.getName(), activationLink);
    }

    @Transactional
    public skyware.product.skywareapi.company.dto.CompanyActivationResponseDto activateCompany(String tokenOrEmail) {
        if (tokenOrEmail == null || tokenOrEmail.trim().isEmpty()) {
            throw new ValidationError("Email or activation token is required");
        }
        final String email = resolveEmail(tokenOrEmail.trim());

        CompanyEntity company = companyRepository.findByEmail(email)
                .orElseThrow(() -> new ValidationError("Company not found with email: " + email));

        if (company.getStatus() != null && company.getStatus() == 1) {
            return skyware.product.skywareapi.company.dto.CompanyActivationResponseDto.builder()
                    .newlyActivated(false)
                    .message("already activated")
                    .company(mapToDto(company))
                    .build();
        }

        company.setStatus(1);
        CompanyEntity saved = companyRepository.save(company);
        log.info("Company [{}] activated successfully, status changed to 1.", saved.getEmail());

        return skyware.product.skywareapi.company.dto.CompanyActivationResponseDto.builder()
                .newlyActivated(true)
                .message("activated successfully")
                .company(mapToDto(saved))
                .build();
    }

    private String resolveEmail(String tokenOrEmail) {
        if (tokenOrEmail.contains("@")) {
            return tokenOrEmail;
        }
        try {
            return new String(java.util.Base64.getUrlDecoder().decode(tokenOrEmail), java.nio.charset.StandardCharsets.UTF_8);
        } catch (Exception e) {
            try {
                return new String(java.util.Base64.getDecoder().decode(tokenOrEmail), java.nio.charset.StandardCharsets.UTF_8);
            } catch (Exception ex) {
                throw new ValidationError("Invalid activation token or email");
            }
        }
    }

    public List<CompanyResponseDto> getAllCompanies() {
        return companyRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyResponseDto toggleCompanyStatus(Long id) {
        if (id == null) {
            throw new ValidationError("Company ID is required");
        }
        CompanyEntity company = companyRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Company not found with id: " + id));

        int newStatus = (company.getStatus() != null && company.getStatus() == 1) ? 0 : 1;
        company.setStatus(newStatus);
        CompanyEntity updated = companyRepository.save(company);

        log.info("Company ID [{}] ({}) status toggled to {}.", id, updated.getName(), newStatus);
        return mapToDto(updated);
    }

    private CompanyResponseDto mapToDto(CompanyEntity entity) {
        return CompanyResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .domain(entity.getDomain())
                .otp(entity.getOtp())
                .verifyotp(entity.getVerifyotp())
                .status(entity.getStatus())
                .adminApprove(entity.getAdminApprove())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
