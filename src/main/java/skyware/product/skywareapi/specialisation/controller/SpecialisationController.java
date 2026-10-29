package skyware.product.skywareapi.specialisation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.specialisation.dto.SpecialisationRequestDto;
import skyware.product.skywareapi.specialisation.dto.SpecialisationResponseDto;
import skyware.product.skywareapi.specialisation.service.SpecialisationService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skyware/api/specialisation")
@RequiredArgsConstructor
public class SpecialisationController {

    private final SpecialisationService specialisationService;

    // 1. ADD SPECIALISATION
    @PostMapping({"/add", ""})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> addSpecialisation(
            @Valid @RequestBody SpecialisationRequestDto dto,
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
            SpecialisationResponseDto created = specialisationService.addSpecialisation(dto);
            response.put("statusCode", 201);
            response.put("status", true);
            response.put("message", "Specialisation added successfully");
            response.put("data", created);
            return ResponseEntity.status(201).body(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while adding specialisation");
            return ResponseEntity.status(400).body(response);
        }
    }

    // 2. GET ALL SPECIALISATIONS
    @GetMapping({"", "/list", "/all"})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> getAllSpecialisations() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<SpecialisationResponseDto> specialisations = specialisationService.getAllSpecialisations();
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Specialisations fetched successfully");
            response.put("totalSpecialisations", specialisations.size());
            response.put("data", specialisations);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while fetching specialisations");
            return ResponseEntity.status(400).body(response);
        }
    }

    // 3. UPDATE SPECIALISATION (by path variable /update/{id} or /{id})
    @RequestMapping(
            value = {"/update/{id}", "/{id}"},
            method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.POST}
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> updateSpecialisation(
            @PathVariable("id") Long id,
            @Valid @RequestBody SpecialisationRequestDto dto,
            BindingResult bindingResult) {
        return processUpdate(id, dto, bindingResult);
    }

    // 3b. UPDATE SPECIALISATION (by query param ?id=...)
    @RequestMapping(
            value = {"/update"},
            method = {RequestMethod.PUT, RequestMethod.PATCH, RequestMethod.POST}
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> updateSpecialisationByParam(
            @RequestParam("id") Long id,
            @Valid @RequestBody SpecialisationRequestDto dto,
            BindingResult bindingResult) {
        return processUpdate(id, dto, bindingResult);
    }

    private ResponseEntity<?> processUpdate(Long id, SpecialisationRequestDto dto, BindingResult bindingResult) {
        Map<String, Object> response = new HashMap<>();

        if (id == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Specialisation ID is required for update");
            return ResponseEntity.status(422).body(response);
        }

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
            SpecialisationResponseDto updated = specialisationService.updateSpecialisation(id, dto);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Specialisation updated successfully");
            response.put("data", updated);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while updating specialisation");
            return ResponseEntity.status(400).body(response);
        }
    }

    // 4. DELETE SPECIALISATION (by path variable /delete/{id} or /{id})
    @DeleteMapping({"/delete/{id}", "/{id}"})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> deleteSpecialisation(@PathVariable("id") Long id) {
        return processDelete(id);
    }

    // 4b. DELETE SPECIALISATION (by query param ?id=...)
    @DeleteMapping("/delete")
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> deleteSpecialisationByParam(@RequestParam("id") Long id) {
        return processDelete(id);
    }

    private ResponseEntity<?> processDelete(Long id) {
        Map<String, Object> response = new HashMap<>();
        if (id == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Specialisation ID is required for deletion");
            return ResponseEntity.status(422).body(response);
        }
        try {
            specialisationService.deleteSpecialisation(id);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Specialisation deleted successfully with id: " + id);
            return ResponseEntity.ok(response);
        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while deleting specialisation");
            return ResponseEntity.status(400).body(response);
        }
    }
}
