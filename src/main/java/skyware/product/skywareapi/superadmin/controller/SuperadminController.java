package skyware.product.skywareapi.superadmin.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.superadmin.dto.LoginPayloadDto;
import skyware.product.skywareapi.superadmin.dto.SuperAdminCreateDto;
import skyware.product.skywareapi.superadmin.dto.SuperAdminResponseDto;
import skyware.product.skywareapi.superadmin.service.SuperAdminService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("skyware/api/superadmin")
@RequiredArgsConstructor
public class SuperadminController {
    private final SuperAdminService superadminService;

    @PostMapping("/login")
    public ResponseEntity<?> register(@Valid @RequestBody LoginPayloadDto loginPayloadDto, BindingResult bindingResult){
        Map<String,Object> response = new HashMap<>();
        if(bindingResult.hasErrors()){
            Map<String,String> errors = new HashMap<>();
            for(FieldError error : bindingResult.getFieldErrors()){
                errors.put(error.getField(),error.getDefaultMessage());
            }
            response.put("message","validation error");
            response.put("status",false);
            response.put("statusCode",422);
            response.put("error",errors);

            return ResponseEntity.status(422).body(response);
        }
        try{
            SuperAdminResponseDto superAdminResponseDto = superadminService.login(loginPayloadDto);
            response.put("message","login successfully");
            response.put("status",true);
            response.put("statusCode",200);
            response.put("data",superAdminResponseDto);
            return ResponseEntity.status(200).body(response);
        }catch(ValidationError e){
            response.put("message",e.getMessage());
            response.put("status",false);
            response.put("statusCode",422);
            return ResponseEntity.status(422).body(response);
        }catch(Exception e){
            response.put("message",e.getMessage());
            response.put("status",false);
            response.put("statusCode",400);
            response.put("error",e.getStackTrace());

            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> addAdmin(@Valid @RequestBody SuperAdminCreateDto createDto, BindingResult bindingResult) {
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
            superadminService.addAdmin(createDto);
            response.put("message", "Admin created successfully and verification email sent.");
            response.put("status", true);
            response.put("statusCode", 201);
            return ResponseEntity.status(201).body(response);
        } catch (ValidationError e) {
            response.put("message", e.getMessage());
            response.put("status", false);
            response.put("statusCode", 422);
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("message", e.getMessage());
            response.put("status", false);
            response.put("statusCode", 400);
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping(value = "/verify", produces = org.springframework.http.MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> verifyAdmin(@RequestParam("token") String token) {
        try {
            String resultMessage = superadminService.verifyAdmin(token);
            String html = "<html><body style='text-align:center; padding: 50px; font-family: sans-serif;'>"
                        + "<h1 style='color: #4CAF50;'>" + resultMessage + "</h1>"
                        + "</body></html>";
            return ResponseEntity.ok(html);
        } catch (ValidationError e) {
            String html = "<html><body style='text-align:center; padding: 50px; font-family: sans-serif;'>"
                        + "<h1 style='color: #f44336;'>" + e.getMessage() + "</h1>"
                        + "</body></html>";
            return ResponseEntity.status(422).body(html);
        } catch (Exception e) {
            String html = "<html><body style='text-align:center; padding: 50px; font-family: sans-serif;'>"
                        + "<h1 style='color: #f44336;'>An error occurred during verification</h1>"
                        + "</body></html>";
            return ResponseEntity.status(400).body(html);
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@Valid @RequestBody skyware.product.skywareapi.superadmin.dto.ForgotPasswordDto dto, BindingResult bindingResult) {
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
            String resetLink = superadminService.forgotPassword(dto);
            response.put("message", "Password reset link sent to your email.");
            response.put("resetLink", resetLink);
            response.put("status", true);
            response.put("statusCode", 200);
            return ResponseEntity.status(200).body(response);
        } catch (ValidationError e) {
            response.put("message", e.getMessage());
            response.put("status", false);
            response.put("statusCode", 422);
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred");
            response.put("status", false);
            response.put("statusCode", 400);
            return ResponseEntity.status(400).body(response);
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@Valid @RequestBody skyware.product.skywareapi.superadmin.dto.ResetPasswordDto dto, BindingResult bindingResult) {
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
            superadminService.resetPassword(dto);
            response.put("message", "Password reset successful");
            response.put("status", true);
            response.put("statusCode", 200);
            return ResponseEntity.status(200).body(response);
        } catch (ValidationError e) {
            response.put("message", e.getMessage());
            response.put("status", false);
            response.put("statusCode", 422);
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("message", "An error occurred during password reset.");
            response.put("status", false);
            response.put("statusCode", 400);
            return ResponseEntity.status(400).body(response);
        }
    }
}
