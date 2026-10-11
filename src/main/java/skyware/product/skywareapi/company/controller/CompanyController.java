package skyware.product.skywareapi.company.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.company.dto.CompanyActivateRequestDto;
import skyware.product.skywareapi.company.dto.CompanyRegisterRequestDto;
import skyware.product.skywareapi.company.dto.CompanyResponseDto;
import skyware.product.skywareapi.company.dto.CompanyVerifyOtpRequestDto;
import skyware.product.skywareapi.company.service.CompanyService;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/skyware/api/company")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;

    @PostMapping("/register")
    public ResponseEntity<?> registerCompany(@Valid @RequestBody CompanyRegisterRequestDto registerDto, BindingResult bindingResult) {
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
            CompanyResponseDto companyResponseDto = companyService.register(registerDto);
            response.put("message", "Company registered successfully. An OTP has been sent to your email.");
            response.put("status", true);
            response.put("statusCode", 201);
            response.put("data", companyResponseDto);
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
    public ResponseEntity<?> verifyOtp(@Valid @RequestBody CompanyVerifyOtpRequestDto verifyOtpDto, BindingResult bindingResult) {
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
            CompanyResponseDto companyResponseDto = companyService.verifyOtp(verifyOtpDto);
            String token = companyService.generateActivationToken(companyResponseDto.getEmail());
            String activationLink = companyService.getActivationLink(token);

            response.put("message", "OTP verified successfully. Company registration completed.");
            response.put("status", true);
            response.put("statusCode", 200);
            response.put("activationToken", token);
            response.put("activationLink", activationLink);
            response.put("data", companyResponseDto);
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

    @PostMapping("/activate")
    public ResponseEntity<?> activateCompanyPost(@RequestBody(required = false) CompanyActivateRequestDto activateDto) {
        Map<String, Object> response = new HashMap<>();
        String identifier = null;
        if (activateDto != null) {
            if (activateDto.getToken() != null && !activateDto.getToken().trim().isEmpty()) {
                identifier = activateDto.getToken().trim();
            } else if (activateDto.getEmail() != null && !activateDto.getEmail().trim().isEmpty()) {
                identifier = activateDto.getEmail().trim();
            }
        }

        if (identifier == null) {
            response.put("message", "Email or activation token is required in request body (e.g. {\"email\": \"user@example.com\"})");
            response.put("status", false);
            response.put("statusCode", 422);
            return ResponseEntity.status(422).body(response);
        }

        try {
            var result = companyService.activateCompany(identifier);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", result.getMessage());
            response.put("alreadyActivated", !result.isNewlyActivated());
            response.put("data", result.getCompany());
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred during activation");
            return ResponseEntity.status(400).body(response);
        }
    }

    @GetMapping("/activate")
    public ResponseEntity<?> activateCompany(
            @RequestParam(value = "token", required = false) String token,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "format", required = false) String format,
            @RequestHeader(value = "Accept", required = false) String acceptHeader) {

        String identifier = (token != null && !token.trim().isEmpty()) ? token.trim() : email;

        // Determine if request comes from web browser vs API/Postman
        boolean isBrowserRequest = acceptHeader != null && acceptHeader.contains("text/html") && !acceptHeader.contains("application/json");
        if ("json".equalsIgnoreCase(format)) {
            isBrowserRequest = false;
        } else if ("html".equalsIgnoreCase(format)) {
            isBrowserRequest = true;
        }

        if (identifier == null || identifier.trim().isEmpty()) {
            if (!isBrowserRequest) {
                Map<String, Object> response = new HashMap<>();
                response.put("statusCode", 422);
                response.put("status", false);
                response.put("message", "Token or email query parameter is required (e.g. ?email=user@example.com or ?token=...)");
                return ResponseEntity.status(422).body(response);
            }
            return ResponseEntity.status(422).contentType(org.springframework.http.MediaType.TEXT_HTML)
                    .body("<html><body style='font-family: sans-serif; text-align: center; padding: 50px;'><h2>Missing token or email parameter</h2></body></html>");
        }

        try {
            var result = companyService.activateCompany(identifier);

            if (!isBrowserRequest) {
                Map<String, Object> response = new HashMap<>();
                response.put("statusCode", 200);
                response.put("status", true);
                response.put("message", result.getMessage());
                response.put("alreadyActivated", !result.isNewlyActivated());
                response.put("data", result.getCompany());
                return ResponseEntity.ok(response);
            }

            // HTML page for browser clicks from email button
            String title = result.isNewlyActivated() ? "Account Activated Successfully!" : "Account Already Activated!";
            String subtitle = result.isNewlyActivated()
                    ? "Congratulations! Your company account has been successfully activated (status = 1)."
                    : "Your company account is already active and verified.";
            String badgeColor = result.isNewlyActivated() ? "#28a745" : "#17a2b8";
            String icon = result.isNewlyActivated() ? "&#10004;" : "&#9432;";

            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>" + title + "</title>"
                    + "<style>"
                    + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f4f6f9; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }"
                    + ".card { background: #ffffff; padding: 45px 35px; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.08); text-align: center; max-width: 480px; width: 90%; }"
                    + ".icon { width: 75px; height: 75px; border-radius: 50%; background: " + badgeColor + "; color: #ffffff; font-size: 38px; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; }"
                    + "h1 { color: #2c3e50; font-size: 24px; margin-bottom: 12px; font-weight: 600; }"
                    + "p { color: #6c757d; font-size: 15px; line-height: 1.6; margin-bottom: 25px; }"
                    + ".status-badge { display: inline-block; padding: 6px 16px; background: " + (result.isNewlyActivated() ? "#e8f5e9" : "#e1f5fe") + "; color: " + badgeColor + "; border-radius: 20px; font-size: 13px; font-weight: 600; text-transform: uppercase; letter-spacing: 0.5px; }"
                    + "</style></head><body>"
                    + "<div class='card'>"
                    + "<div class='icon'>" + icon + "</div>"
                    + "<h1>" + title + "</h1>"
                    + "<p>" + subtitle + "</p>"
                    + "<span class='status-badge'>" + result.getMessage() + "</span>"
                    + "</div>"
                    + "</body></html>";

            return ResponseEntity.ok().contentType(org.springframework.http.MediaType.TEXT_HTML).body(html);

        } catch (ValidationError e) {
            if (!isBrowserRequest) {
                Map<String, Object> response = new HashMap<>();
                response.put("statusCode", 422);
                response.put("status", false);
                response.put("message", e.getMessage());
                return ResponseEntity.status(422).body(response);
            }
            String html = "<!DOCTYPE html><html><head><meta charset='UTF-8'><title>Activation Failed</title>"
                    + "<style>"
                    + "body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; background: #f4f6f9; display: flex; justify-content: center; align-items: center; min-height: 100vh; margin: 0; }"
                    + ".card { background: #ffffff; padding: 45px 35px; border-radius: 12px; box-shadow: 0 8px 24px rgba(0,0,0,0.08); text-align: center; max-width: 480px; width: 90%; }"
                    + ".icon { width: 75px; height: 75px; border-radius: 50%; background: #dc3545; color: #ffffff; font-size: 38px; margin: 0 auto 20px; display: flex; align-items: center; justify-content: center; }"
                    + "h1 { color: #dc3545; font-size: 24px; margin-bottom: 12px; font-weight: 600; }"
                    + "p { color: #6c757d; font-size: 15px; line-height: 1.6; }"
                    + "</style></head><body>"
                    + "<div class='card'>"
                    + "<div class='icon'>&#10006;</div>"
                    + "<h1>Activation Failed</h1>"
                    + "<p>" + e.getMessage() + "</p>"
                    + "</div>"
                    + "</body></html>";
            return ResponseEntity.status(422).contentType(org.springframework.http.MediaType.TEXT_HTML).body(html);

        } catch (Exception e) {
            if (!isBrowserRequest) {
                Map<String, Object> response = new HashMap<>();
                response.put("statusCode", 400);
                response.put("status", false);
                response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred during activation");
                return ResponseEntity.status(400).body(response);
            }
            String html = "<html><body style='text-align:center; padding: 50px; font-family: sans-serif;'><h1 style='color: #dc3545;'>An error occurred during activation</h1></body></html>";
            return ResponseEntity.status(400).contentType(org.springframework.http.MediaType.TEXT_HTML).body(html);
        }
    }

    @PostMapping("/send-activation-email")
    public ResponseEntity<?> sendActivationEmail(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        if (email == null || email.trim().isEmpty()) {
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Email is required");
            return ResponseEntity.status(422).body(response);
        }
        try {
            companyService.sendActivationEmailByEmail(email);
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Activation email with button sent successfully.");
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(400).body(response);
        }
    }
}
