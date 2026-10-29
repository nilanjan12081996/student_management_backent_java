package skyware.product.skywareapi.teacher.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.teacher.dto.TeacherSpecialisationMapRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherSpecialisationResponseDto;
import skyware.product.skywareapi.teacher.service.TeacherSpecialisationMapService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping({"/skyware/api/teacher/specialisation", "/skyware/api/teacher-specialisation"})
@RequiredArgsConstructor
public class TeacherSpecialisationMapController {

    private final TeacherSpecialisationMapService teacherSpecialisationMapService;

    /**
     * Map one or multiple specialisations to a teacher.
     * Accessible by SUPERADMIN.
     */
    @PostMapping({"/map", "/add", ""})
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

    /**
     * Get all specialisations mapped to a specific teacher.
     */
    @GetMapping({"/teacher/{teacherId}", "/{teacherId}"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getSpecialisationsByTeacherId(@PathVariable("teacherId") Long teacherId) {
        Map<String, Object> response = new HashMap<>();
        try {
            TeacherSpecialisationResponseDto data = teacherSpecialisationMapService.getSpecialisationsByTeacherId(teacherId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher specialisations fetched successfully");
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
     * Get all teacher-specialisation mappings in the system.
     */
    @GetMapping({"", "/list", "/all"})
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> getAllTeacherSpecialisations() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<TeacherSpecialisationResponseDto> list = teacherSpecialisationMapService.getAllTeacherSpecialisations();
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "All teacher specialisations fetched successfully");
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
     * Unmap / remove a specialisation from a teacher via query params (?teacher_id=1&sp_id=3) or Request Body.
     */
    @RequestMapping(
            value = {"/unmap", "/delete"},
            method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unmapByQueryOrBody(
            @RequestParam(value = "teacherId", required = false) Long queryTeacherId,
            @RequestParam(value = "teacher_id", required = false) Long queryTeacherIdSnake,
            @RequestParam(value = "spId", required = false) Long querySpId,
            @RequestParam(value = "sp_id", required = false) Long querySpIdSnake,
            @RequestBody(required = false) Map<String, Object> body) {

        Long targetTeacherId = (queryTeacherId != null) ? queryTeacherId : queryTeacherIdSnake;
        Long targetSpId = (querySpId != null) ? querySpId : querySpIdSnake;

        if (body != null) {
            if (targetTeacherId == null) {
                Object t = body.get("teacherId") != null ? body.get("teacherId") : body.get("teacher_id");
                if (t != null) targetTeacherId = parseLong(t.toString());
            }
            if (targetSpId == null) {
                Object s = body.get("spId") != null ? body.get("spId") : body.get("sp_id");
                if (s == null) s = body.get("specialisationId");
                if (s != null) targetSpId = parseLong(s.toString());
            }
        }

        return performUnmap(targetTeacherId, targetSpId);
    }

    /**
     * Unmap / remove a specialisation from a teacher via 2 path variables (/delete/1/3).
     */
    @RequestMapping(
            value = {"/unmap/{teacherId}/{spId}", "/delete/{teacherId}/{spId}"},
            method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unmapByPath(
            @PathVariable("teacherId") Long teacherId,
            @PathVariable("spId") Long spId) {
        return performUnmap(teacherId, spId);
    }

    /**
     * Unmap / remove a specialisation from a teacher via 1 path variable string (/delete/teacher_id=1&sp_id=3).
     */
    @RequestMapping(
            value = {"/unmap/{paramString}", "/delete/{paramString}"},
            method = {RequestMethod.DELETE, RequestMethod.POST}
    )
    @PreAuthorize("hasRole('SUPERADMIN')")
    public ResponseEntity<?> unmapByPathString(
            @PathVariable("paramString") String paramString) {

        Long targetTeacherId = null;
        Long targetSpId = null;

        if (paramString != null) {
            String[] pairs = paramString.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=");
                if (kv.length == 2) {
                    String key = kv[0].trim();
                    String val = kv[1].trim();
                    if ("teacherId".equalsIgnoreCase(key) || "teacher_id".equalsIgnoreCase(key)) {
                        targetTeacherId = parseLong(val);
                    } else if ("spId".equalsIgnoreCase(key) || "sp_id".equalsIgnoreCase(key) || "sp".equalsIgnoreCase(key)) {
                        targetSpId = parseLong(val);
                    }
                }
            }
        }

        return performUnmap(targetTeacherId, targetSpId);
    }

    private ResponseEntity<?> performUnmap(Long teacherId, Long spId) {
        Map<String, Object> response = new HashMap<>();

        if (teacherId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "teacherId (or teacher_id) is required to unmap specialisation");
            return ResponseEntity.status(422).body(response);
        }

        if (spId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "spId (or sp_id) is required to unmap specialisation");
            return ResponseEntity.status(422).body(response);
        }

        try {
            teacherSpecialisationMapService.unmapSpecialisation(teacherId, spId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Teacher specialisation mapping removed successfully");
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

