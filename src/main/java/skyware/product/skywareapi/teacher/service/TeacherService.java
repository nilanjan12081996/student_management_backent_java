package skyware.product.skywareapi.teacher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.teacher.dto.TeacherRegisterRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherResponseDto;
import skyware.product.skywareapi.teacher.dto.TeacherUpdateRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherVerifyOtpRequestDto;
import skyware.product.skywareapi.teacher.entity.TeacherEntity;
import skyware.product.skywareapi.teacher.repository.TeacherCompanyMapRepository;
import skyware.product.skywareapi.teacher.repository.TeacherRepository;
import skyware.product.skywareapi.teacher.repository.TeacherSpecialisationMapRepository;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherEmailService teacherEmailService;
    private final TeacherCompanyMapRepository teacherCompanyMapRepository;
    private final TeacherSpecialisationMapRepository teacherSpecialisationMapRepository;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public TeacherResponseDto register(TeacherRegisterRequestDto dto) {
        int createdBySuperadmin = (isCurrentUserSuperAdmin() || (dto.getCreatedBySuperadmin() != null && dto.getCreatedBySuperadmin() == 1)) ? 1 : 0;

        // Check if email already exists
        Optional<TeacherEntity> existingEmailOpt = teacherRepository.findByEmail(dto.getEmail());
        if (existingEmailOpt.isPresent()) {
            TeacherEntity existing = existingEmailOpt.get();
            if (existing.getVerifyotp() != null && existing.getVerifyotp() == 1) {
                throw new ValidationError("Teacher with this email is already registered and verified");
            }

            // If phone belongs to another teacher
            Optional<TeacherEntity> existingPhoneOpt = teacherRepository.findByPhone(dto.getPhone());
            if (existingPhoneOpt.isPresent() && !existingPhoneOpt.get().getId().equals(existing.getId())) {
                throw new ValidationError("Phone number is already registered with another teacher");
            }

            // Unverified teacher re-registering: generate new OTP and resend
            int otp = 100000 + random.nextInt(900000);
            existing.setName(dto.getName());
            existing.setPhone(dto.getPhone());
            existing.setOtp(otp);
            existing.setVerifyotp(0);
            existing.setStatus(0);
            if (createdBySuperadmin == 1) {
                existing.setCreatedBySuperadmin(1);
            }
            TeacherEntity saved = teacherRepository.save(existing);

            teacherEmailService.sendOtpEmail(saved.getEmail(), saved.getName(), otp);
            log.info("Teacher [{}] unverified registration updated, new OTP sent.", saved.getEmail());

            return mapToDto(saved);
        }

        // Check if phone number already exists
        if (teacherRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new ValidationError("Phone number is already registered with another teacher");
        }

        // Generate 6-digit OTP
        int otp = 100000 + random.nextInt(900000);

        TeacherEntity newTeacher = TeacherEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .otp(otp)
                .verifyotp(0)
                .status(0)
                .createdBySuperadmin(createdBySuperadmin)
                .build();

        TeacherEntity saved = teacherRepository.save(newTeacher);

        // Send OTP email
        teacherEmailService.sendOtpEmail(saved.getEmail(), saved.getName(), otp);
        log.info("Teacher [{}] registered successfully, OTP sent.", saved.getEmail());

        return mapToDto(saved);
    }

    @Transactional
    public TeacherResponseDto verifyOtp(TeacherVerifyOtpRequestDto dto) {
        TeacherEntity teacher = teacherRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ValidationError("Teacher not found with email: " + dto.getEmail()));

        if (teacher.getVerifyotp() != null && teacher.getVerifyotp() == 1) {
            throw new ValidationError("Teacher is already verified");
        }

        if (teacher.getOtp() == null) {
            throw new ValidationError("No OTP found. Please register or request OTP again.");
        }

        if (!teacher.getOtp().equals(dto.getOtp())) {
            throw new ValidationError("Invalid OTP. Please enter the correct OTP.");
        }

        // OTP matched, mark both verifyotp and status as 1
        teacher.setVerifyotp(1);
        teacher.setStatus(1);
        TeacherEntity updated = teacherRepository.save(teacher);

        log.info("Teacher [{}] verified successfully via OTP. verifyotp=1, status=1", updated.getEmail());

        return mapToDto(updated);
    }

    @Transactional
    public TeacherResponseDto createTeacherBySuperAdmin(TeacherRegisterRequestDto dto) {
        // Enforce created_by_superadmin = 1
        dto.setCreatedBySuperadmin(1);

        // Check if email already exists
        Optional<TeacherEntity> existingEmailOpt = teacherRepository.findByEmail(dto.getEmail());
        if (existingEmailOpt.isPresent()) {
            TeacherEntity existing = existingEmailOpt.get();
            if (existing.getVerifyotp() != null && existing.getVerifyotp() == 1) {
                throw new ValidationError("Teacher with this email is already registered and verified");
            }

            // If phone belongs to another teacher
            Optional<TeacherEntity> existingPhoneOpt = teacherRepository.findByPhone(dto.getPhone());
            if (existingPhoneOpt.isPresent() && !existingPhoneOpt.get().getId().equals(existing.getId())) {
                throw new ValidationError("Phone number is already registered with another teacher");
            }

            // Unverified teacher re-registering: activate directly for SuperAdmin
            int otp = 100000 + random.nextInt(900000);
            existing.setName(dto.getName());
            existing.setPhone(dto.getPhone());
            existing.setOtp(otp);
            existing.setVerifyotp(1);
            existing.setStatus(1);
            existing.setCreatedBySuperadmin(1);
            TeacherEntity saved = teacherRepository.save(existing);

            log.info("Teacher [{}] created and activated by SuperAdmin.", saved.getEmail());
            return mapToDto(saved);
        }

        // Check if phone number already exists
        if (teacherRepository.findByPhone(dto.getPhone()).isPresent()) {
            throw new ValidationError("Phone number is already registered with another teacher");
        }

        // Generate 6-digit OTP
        int otp = 100000 + random.nextInt(900000);

        TeacherEntity newTeacher = TeacherEntity.builder()
                .name(dto.getName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .otp(otp)
                .verifyotp(1)
                .status(1)
                .createdBySuperadmin(1)
                .build();

        TeacherEntity saved = teacherRepository.save(newTeacher);
        log.info("Teacher [{}] created successfully by SuperAdmin with created_by_superadmin=1, status=1, verifyotp=1.", saved.getEmail());

        try {
            teacherEmailService.sendOtpEmail(saved.getEmail(), saved.getName(), otp);
        } catch (Exception e) {
            log.warn("Could not send email to teacher [{}]: {}", saved.getEmail(), e.getMessage());
        }

        return mapToDto(saved);
    }

    public TeacherResponseDto getTeacherById(Long id) {
        if (id == null) {
            throw new ValidationError("Teacher ID is required");
        }
        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + id));
        return mapToDto(teacher);
    }

    @Transactional
    public TeacherResponseDto updateTeacher(Long id, TeacherUpdateRequestDto dto) {
        if (id == null) {
            throw new ValidationError("Teacher ID is required");
        }

        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + id));

        if (dto.getName() != null && !dto.getName().trim().isEmpty()) {
            teacher.setName(dto.getName().trim());
        }

        if (dto.getEmail() != null && !dto.getEmail().trim().isEmpty()) {
            String newEmail = dto.getEmail().trim();
            if (!newEmail.equalsIgnoreCase(teacher.getEmail())) {
                Optional<TeacherEntity> existingEmail = teacherRepository.findByEmail(newEmail);
                if (existingEmail.isPresent() && !existingEmail.get().getId().equals(id)) {
                    throw new ValidationError("Email is already registered with another teacher");
                }
                teacher.setEmail(newEmail);
            }
        }

        if (dto.getPhone() != null && !dto.getPhone().trim().isEmpty()) {
            String newPhone = dto.getPhone().trim();
            if (!newPhone.equals(teacher.getPhone())) {
                Optional<TeacherEntity> existingPhone = teacherRepository.findByPhone(newPhone);
                if (existingPhone.isPresent() && !existingPhone.get().getId().equals(id)) {
                    throw new ValidationError("Phone number is already registered with another teacher");
                }
                teacher.setPhone(newPhone);
            }
        }

        if (dto.getStatus() != null) {
            teacher.setStatus(dto.getStatus());
        }

        if (dto.getVerifyotp() != null) {
            teacher.setVerifyotp(dto.getVerifyotp());
        }

        TeacherEntity updated = teacherRepository.save(teacher);
        log.info("Teacher ID [{}] updated successfully by SuperAdmin.", id);
        return mapToDto(updated);
    }

    @Transactional
    public TeacherResponseDto deleteTeacher(Long id) {
        if (id == null) {
            throw new ValidationError("Teacher ID is required");
        }

        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + id));

        // Clean up mappings
        try {
            teacherCompanyMapRepository.deleteByTeacherId(id);
            teacherSpecialisationMapRepository.deleteByTeacherId(id);
        } catch (Exception e) {
            log.warn("Error cleaning up mappings for teacher ID [{}]: {}", id, e.getMessage());
        }

        teacherRepository.delete(teacher);
        log.info("Teacher ID [{}] deleted successfully by SuperAdmin.", id);

        return mapToDto(teacher);
    }

    public List<TeacherResponseDto> getAllTeachers() {
        return teacherRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public TeacherResponseDto toggleTeacherStatus(Long id) {
        if (id == null) {
            throw new ValidationError("Teacher ID is required");
        }
        TeacherEntity teacher = teacherRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + id));

        int newStatus = (teacher.getStatus() != null && teacher.getStatus() == 1) ? 0 : 1;
        teacher.setStatus(newStatus);
        TeacherEntity updated = teacherRepository.save(teacher);

        log.info("Teacher ID [{}] ({}) status toggled to {}.", id, updated.getName(), newStatus);
        return mapToDto(updated);
    }

    private TeacherResponseDto mapToDto(TeacherEntity teacher) {
        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .otp(teacher.getOtp())
                .verifyotp(teacher.getVerifyotp())
                .status(teacher.getStatus())
                .createdBySuperadmin(teacher.getCreatedBySuperadmin() != null ? teacher.getCreatedBySuperadmin() : 0)
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }

    private boolean isCurrentUserSuperAdmin() {
        try {
            org.springframework.security.core.Authentication auth =
                    org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated()) {
                return false;
            }
            return auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_SUPERADMIN".equals(a.getAuthority()) || "SUPERADMIN".equals(a.getAuthority()));
        } catch (Exception e) {
            return false;
        }
    }
}
