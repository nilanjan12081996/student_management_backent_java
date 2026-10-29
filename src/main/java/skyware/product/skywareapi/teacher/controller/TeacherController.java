package skyware.product.skywareapi.teacher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.teacher.dto.TeacherRegisterRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherResponseDto;
import skyware.product.skywareapi.teacher.dto.TeacherCompanyMapRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherCompanyMapResponseDto;
import skyware.product.skywareapi.teacher.dto.TeacherSpecialisationMapRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherSpecialisationResponseDto;
import skyware.product.skywareapi.teacher.dto.TeacherVerifyOtpRequestDto;
import skyware.product.skywareapi.teacher.service.TeacherCompanyMapService;
import skyware.product.skywareapi.teacher.service.TeacherSpecialisationMapService;
import skyware.product.skywareapi.teacher.service.TeacherService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skyware/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final TeacherSpecialisationMapService teacherSpecialisationMapService;
    private final TeacherCompanyMapService teacherCompanyMapService;

    @PostMapping("/register")
    public ResponseEntity<?> registerTeacher(@Valid @RequestBody TeacherRegisterRequestDto registerDto, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("message", "validation error");
            response.put("status", false);
            response.put("statusCode", 422);
            response.put("error", errors);
            return ResponseEntity.status(422).body(response);
        }

        try {
            TeacherResponseDto teacherResponseDto = teacherService.register(registerDto);
            response.put("message", "Teacher registered successfully. An OTP has been sent to your email.");
            response.put("status", true);
            response.put("statusCode", 201);
            response.put("data", teacherResponseDto);
            return ResponseEntity.status(201).body(response);
        } catch (ValidationError e) {
            response.put("message", e.getMessage());
            response.put("status", false);
            response.put("statusCode", 422);
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            response.put("status", false);
            response.put("statusCode", 400);
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody TeacherVerifyOtpRequestDto verifyOtpDto, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("message", "validation error");
            response.put("status", false);
            response.put("statusCode", 422);
            response.put("error", errors);
            return ResponseEntity.status(422).body(response);
        }

        try {
            TeacherResponseDto teacherResponseDto = teacherService.verifyOtp(verifyOtpDto);
            response.put("message", "OTP verified successfully. Teacher verification completed.");
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("data", teacherResponseDto);
            return ResponseEntity.status(200).body(response);
        } catch (ValidationError e) {
            response.put("message", e.getMessage());
            response.put("status", false);
            response.put("statusCode", 422);
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            response.put("status", false);
            response.put("statusCode", 400);
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping({"", "/list", "/all"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getAllTeachers() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeacherResponseDto> teachers = teacherService.getAllTeachers();
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher list fetched successfully");
            response.put("totalTeachers", teachers.size());
            response.put("data", teachers);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while fetching teacher list");
            return ResponseEntity.status(400).body(response);
        }
    }

    @PatchMapping(value = {"/toggle-status/{id}", "/status/{id}", "/{id}/toggle-status"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> toggleTeacherStatus(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            TeacherResponseDto updatedTeacher = teacherService.toggleTeacherStatus(id);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher status toggled successfully to " + updatedTeacher.getStatus());
            response.put("currentStatus", updatedTeacher.getStatus());
            response.put("data", updatedTeacher);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while toggling status");
            return ResponseEntity.status(400).body(response);
        }
    }

    @PatchMapping("/toggle-status")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> toggleTeacherStatusByParam(
            @RequestParam(value = "id", required = false) Long queryId,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();
        Long targetId = queryId;
        if (targetId == null && body != null && body.get("id") != null) {
            try {
                targetId = Long.valueOf(body.get("id").toString());
            } catch (Exception ignored) {}
        }
        if (targetId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Teacher id parameter is required (e.g. ?id=1)");
            return ResponseEntity.status(422).body(response);
        }
        try {
            TeacherResponseDto updatedTeacher = teacherService.toggleTeacherStatus(targetId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher status toggled successfully to " + updatedTeacher.getStatus());
            response.put("currentStatus", updatedTeacher.getStatus());
            response.put("data", updatedTeacher);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while toggling status");
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping({"/map-specialisation", "/map-specialisations"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> mapSpecialisations(
            @Valid @RequestBody TeacherSpecialisationMapRequestDto dto,
            BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("message", "validation error");
            response.put("status", false);
            response.put("statusCode", 422);
            response.put("error", errors);
            return ResponseEntity.status(422).body(response);
        }

        try {
            TeacherSpecialisationResponseDto result = teacherSpecialisationMapService.mapSpecialisations(dto);
            response.put("statusCode", 201);
            response.put("status", true);
            response.put("message", "Teacher specialisations mapped successfully");
            response.put("data", result);
            return ResponseEntity.status(201).body(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while mapping teacher specialisations");
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/map-company")
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> mapTeacherToCompany(
            @Valid @RequestBody TeacherCompanyMapRequestDto dto,
            BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (bindingResult.hasErrors()) {
            Map<String, String> errors = new HashMap<>();
            for (FieldError error : bindingResult.getFieldErrors()) {
                errors.put(error.getField(), error.getDefaultMessage());
            }
            response.put("message", "validation error");
            response.put("status", false);
            response.put("statusCode", 422);
            response.put("error", errors);
            return ResponseEntity.status(422).body(response);
        }

        try {
            TeacherCompanyMapResponseDto result = teacherCompanyMapService.mapTeacherToCompany(dto);
            response.put("statusCode", 201);
            response.put("status", true);
            response.put("message", "Teacher mapped to company successfully with status 1");
            response.put("data", result);
            return ResponseEntity.status(201).body(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while mapping teacher to company");
            return ResponseEntity.status(400).body(response);
        }
    }
}
