package skyware.product.skywareapi.teacher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.teacher.dto.TeacherRegisterRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherResponseDto;
import skyware.product.skywareapi.teacher.dto.TeacherVerifyOtpRequestDto;
import skyware.product.skywareapi.teacher.service.TeacherService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/skyware/api/teacher")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;

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
}
