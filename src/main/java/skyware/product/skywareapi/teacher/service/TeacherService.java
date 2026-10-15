package skyware.product.skywareapi.teacher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.teacher.dto.TeacherRegisterRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherResponseDto;
import skyware.product.skywareapi.teacher.dto.TeacherVerifyOtpRequestDto;
import skyware.product.skywareapi.teacher.entity.TeacherEntity;
import skyware.product.skywareapi.teacher.repository.TeacherRepository;

import java.security.SecureRandom;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherService {

    private final TeacherRepository teacherRepository;
    private final TeacherEmailService teacherEmailService;
    private final SecureRandom random = new SecureRandom();

    @Transactional
    public TeacherResponseDto register(TeacherRegisterRequestDto dto) {
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

    private TeacherResponseDto mapToDto(TeacherEntity teacher) {
        return TeacherResponseDto.builder()
                .id(teacher.getId())
                .name(teacher.getName())
                .email(teacher.getEmail())
                .phone(teacher.getPhone())
                .otp(teacher.getOtp())
                .verifyotp(teacher.getVerifyotp())
                .status(teacher.getStatus())
                .createdAt(teacher.getCreatedAt())
                .updatedAt(teacher.getUpdatedAt())
                .build();
    }
}
