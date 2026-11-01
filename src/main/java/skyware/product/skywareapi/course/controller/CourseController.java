package skyware.product.skywareapi.course.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.course.dto.CourseRequestDto;
import skyware.product.skywareapi.course.dto.CourseResponseDto;
import skyware.product.skywareapi.course.service.CourseService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/skyware/api/course")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    /**
     * Single API for both Create and Update:
     * - If 'id' is provided in request body / path / query param -> Updates existing course
     * - If 'id' is omitted / null -> Creates new course
     */
    @RequestMapping(
            value = {"", "/save", "/create-or-update", "/add", "/update", "/update/{id}"},
            method = {RequestMethod.POST, RequestMethod.PUT, RequestMethod.PATCH}
    )
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> saveOrUpdateCourse(
            @PathVariable(value = "id", required = false) Long pathId,
            @RequestParam(value = "id", required = false) Long queryId,
            @Valid @RequestBody CourseRequestDto requestDto,
            BindingResult bindingResult) {

        if (requestDto.getId() == null) {
            if (pathId != null) {
                requestDto.setId(pathId);
            } else if (queryId != null) {
                requestDto.setId(queryId);
            }
        }
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
            boolean isUpdate = requestDto.getId() != null;
            CourseResponseDto courseResponseDto = courseService.saveOrUpdateCourse(requestDto);

            String message = isUpdate ? "Course updated successfully" : "Course created successfully";
            int statusCode = isUpdate ? 200 : 201;

            response.put("statusCode", statusCode);
            response.put("status", true);
            response.put("message", message);
            response.put("data", courseResponseDto);
            return ResponseEntity.status(statusCode).body(response);

        } catch (ValidationError e) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", e.getMessage());
            return ResponseEntity.status(422).body(response);
        } catch (Exception e) {
            response.put("statusCode", 400);
            response.put("status", false);
            response.put("message", e.getMessage() != null ? e.getMessage() : "An unexpected error occurred while saving course");
            return ResponseEntity.status(400).body(response);
        }
    }

    /**
     * Get all courses with their respective related entity names.
     */
    @GetMapping({"", "/list", "/all"})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> getAllCourses() {
        Map<String, Object> response = new HashMap<>();
        try {
            List<CourseResponseDto> list = courseService.getAllCourses();
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Courses fetched successfully");
            response.put("totalCourses", list.size());
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
     * Get single course by ID.
     */
    @GetMapping({"/{id}", "/details/{id}"})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> getCourseById(@PathVariable("id") Long id) {
        Map<String, Object> response = new HashMap<>();
        try {
            CourseResponseDto course = courseService.getCourseById(id);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Course details fetched successfully");
            response.put("data", course);
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
     * Delete course by ID.
     */
    @DeleteMapping({"/{id}", "/delete/{id}", "/delete"})
    @PreAuthorize("hasAnyRole('SUPERADMIN', 'ADMIN')")
    public ResponseEntity<?> deleteCourse(
            @PathVariable(value = "id", required = false) Long pathId,
            @RequestParam(value = "id", required = false) Long queryId) {
        Long targetId = pathId != null ? pathId : queryId;
        Map<String, Object> response = new HashMap<>();

        if (targetId == null) {
            response.put("statusCode", 422);
            response.put("status", false);
            response.put("message", "Course ID is required for deletion");
            return ResponseEntity.status(422).body(response);
        }

        try {
            courseService.deleteCourse(targetId);
            response.put("statusCode", 200);
            response.put("status", true);
            response.put("message", "Course deleted successfully with id: " + targetId);
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
}

