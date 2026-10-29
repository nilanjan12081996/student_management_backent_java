package skyware.product.skywareapi.teacher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.teacher.dto.TeacherCompanyMapRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherCompanyMapResponseDto;
import skyware.product.skywareapi.teacher.service.TeacherCompanyMapService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/skyware/api/teacher/company", "/skyware/api/teacher-company"})
@RequiredArgsConstructor
public class TeacherCompanyMapController {

    private final TeacherCompanyMapService teacherCompanyMapService;

    /**
     * Map a teacher to a company.
     * Constraint: A teacher can only be mapped to 1 active company at a time.
     * Status is set to 1.
     */
    @PostMapping({"/map", "/add", ""})
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

    /**
     * Get company mapping for a specific teacher.
     */
    @GetMapping({"/teacher/{teacherId}", "/{teacherId}"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getTeacherCompany(@PathVariable("teacherId") Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeacherCompanyMapResponseDto> data = teacherCompanyMapService.getTeacherCompanyMappings(teacherId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher company mappings fetched successfully");
            response.put("totalRecords", data.size());
            response.put("data", data);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Get all teacher-company mappings.
     */
    @GetMapping({"", "/list", "/all"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getAllTeacherCompanyMappings() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeacherCompanyMapResponseDto> list = teacherCompanyMapService.getAllTeacherCompanyMappings();
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "All teacher-company mappings fetched successfully");
            response.put("totalRecords", list.size());
            response.put("data", list);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Toggle status (1 -> 0 or 0 -> 1).
     * Can be called via query params, path params, or request body.
     */
    @RequestMapping(
            value = {"/toggle-status", "/status"},
            method = {RequestMethod.PATCH, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> toggleStatus(
            @RequestParam(value = "teacherId", required = false) Long queryTeacherId,
            @RequestParam(value = "teacher_id", required = false) Long queryTeacherIdSnake,
            @RequestParam(value = "companyId", required = false) Long queryCompanyId,
            @RequestParam(value = "company_id", required = false) Long queryCompanyIdSnake,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();

        Long teacherId = (queryTeacherId != null) ? queryTeacherId : queryTeacherIdSnake;
        Long companyId = (queryCompanyId != null) ? queryCompanyId : queryCompanyIdSnake;

        if (body != null) {
            if (teacherId == null) {
                Object t = body.get("teacherId") != null ? body.get("teacherId") : body.get("teacher_id");
                if (t != null) teacherId = parseLong(t.toString());
            }
            if (companyId == null) {
                Object c = body.get("companyId") != null ? body.get("companyId") : body.get("company_id");
                if (c != null) companyId = parseLong(c.toString());
            }
        }

        if (teacherId == null || companyId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Both teacher_id and company_id are required to toggle status");
            return ResponseEntity.status(422).body(response);
        }

        try {
            TeacherCompanyMapResponseDto result = teacherCompanyMapService.toggleStatus(teacherId, companyId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Mapping status toggled successfully to " + result.getStatus());
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Deactivate mapping (explicitly sets status = 0).
     */
    @RequestMapping(
            value = {"/deactivate", "/set-inactive"},
            method = {RequestMethod.PATCH, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> deactivateMapping(
            @RequestParam(value = "teacherId", required = false) Long queryTeacherId,
            @RequestParam(value = "teacher_id", required = false) Long queryTeacherIdSnake,
            @RequestParam(value = "companyId", required = false) Long queryCompanyId,
            @RequestParam(value = "company_id", required = false) Long queryCompanyIdSnake,
            @RequestBody(required = false) Map<String, Object> body) {
        Map<String, Object> response = new HashMap<>();

        Long teacherId = (queryTeacherId != null) ? queryTeacherId : queryTeacherIdSnake;
        Long companyId = (queryCompanyId != null) ? queryCompanyId : queryCompanyIdSnake;

        if (body != null) {
            if (teacherId == null) {
                Object t = body.get("teacherId") != null ? body.get("teacherId") : body.get("teacher_id");
                if (t != null) teacherId = parseLong(t.toString());
            }
            if (companyId == null) {
                Object c = body.get("companyId") != null ? body.get("companyId") : body.get("company_id");
                if (c != null) companyId = parseLong(c.toString());
            }
        }

        if (teacherId == null || companyId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Both teacher_id and company_id are required to deactivate mapping");
            return ResponseEntity.status(422).body(response);
        }

        try {
            TeacherCompanyMapResponseDto result = teacherCompanyMapService.deactivateMapping(teacherId, companyId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Mapping deactivated successfully (status = 0). Teacher can now be mapped to another company.");
            response.put("data", result);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Delete / unmap mapping via query params or body.
     */
    @RequestMapping(
            value = {"/unmap", "/delete"},
            method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unmapByQueryOrBody(
            @RequestParam(value = "teacherId", required = false) Long queryTeacherId,
            @RequestParam(value = "teacher_id", required = false) Long queryTeacherIdSnake,
            @RequestParam(value = "companyId", required = false) Long queryCompanyId,
            @RequestParam(value = "company_id", required = false) Long queryCompanyIdSnake,
            @RequestBody(required = false) Map<String, Object> body) {

        Long targetTeacherId = (queryTeacherId != null) ? queryTeacherId : queryTeacherIdSnake;
        Long targetCompanyId = (queryCompanyId != null) ? queryCompanyId : queryCompanyIdSnake;

        if (body != null) {
            if (targetTeacherId == null) {
                Object t = body.get("teacherId") != null ? body.get("teacherId") : body.get("teacher_id");
                if (t != null) targetTeacherId = parseLong(t.toString());
            }
            if (targetCompanyId == null) {
                Object c = body.get("companyId") != null ? body.get("companyId") : body.get("company_id");
                if (c != null) targetCompanyId = parseLong(c.toString());
            }
        }

        return performUnmap(targetTeacherId, targetCompanyId);
    }

    /**
     * Delete / unmap via 2 path variables (/delete/1/2).
     */
    @RequestMapping(
            value = {"/unmap/{teacherId}/{companyId}", "/delete/{teacherId}/{companyId}"},
            method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unmapByPath(
            @PathVariable("teacherId") Long teacherId,
            @PathVariable("companyId") Long companyId) {
        return performUnmap(teacherId, companyId);
    }

    /**
     * Delete / unmap via 1 path variable string (/delete/teacher_id=1&company_id=2).
     */
    @RequestMapping(
            value = {"/unmap/{paramString}", "/delete/{paramString}"},
            method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unmapByPathString(@PathVariable("paramString") String paramString) {
        Long targetTeacherId = null;
        Long targetCompanyId = null;

        if (paramString != null) {
            String[] pairs = paramString.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String val = kv[1].trim();
                    if ("teacherId".equalsIgnoreCase(key) || "teacher_id".equalsIgnoreCase(key)) {
                        targetTeacherId = parseLong(val);
                    } else if ("companyId".equalsIgnoreCase(key) || "company_id".equalsIgnoreCase(key) || "company".equalsIgnoreCase(key)) {
                        targetCompanyId = parseLong(val);
                    }
                }
            }
        }

        return performUnmap(targetTeacherId, targetCompanyId);
    }

    private ResponseEntity<?> performUnmap(Long teacherId, Long companyId) {
        Map<String, Object> response = new HashMap<>();

        if (teacherId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "teacherId (or teacher_id) is required to unmap company");
            return ResponseEntity.status(422).body(response);
        }

        if (companyId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "companyId (or company_id) is required to unmap company");
            return ResponseEntity.status(422).body(response);
        }

        try {
            teacherCompanyMapService.unmapTeacherCompany(teacherId, companyId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher-Company mapping deleted successfully");
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred");
            return ResponseEntity.status(400).body(response);
        }
    }

    private Long parseLong(String val) {
        if (val == null || val.trim().isEmpty()) {
            return null;
        }
        try {
            return Long.valueOf(val.trim());
        } catch (NumberFormatException ignored) {
            return null;
        }
    }
}

