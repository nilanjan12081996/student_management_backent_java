package skyware.product.skywareapi.course.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.admin.entity.AdminEntity;
import skyware.product.skywareapi.admin.repository.AdminRepository;
import skyware.product.skywareapi.company.entity.CompanyEntity;
import skyware.product.skywareapi.company.repository.CompanyRepository;
import skyware.product.skywareapi.course.dto.CourseRequestDto;
import skyware.product.skywareapi.course.dto.CourseResponseDto;
import skyware.product.skywareapi.course.entity.CourseEntity;
import skyware.product.skywareapi.course.entity.RelatedType;
import skyware.product.skywareapi.course.repository.CourseRepository;
import skyware.product.skywareapi.superadmin.entity.SuperAdminEntity;
import skyware.product.skywareapi.superadmin.repository.SuperAdminRepository;
import skyware.product.skywareapi.teacher.entity.TeacherEntity;
import skyware.product.skywareapi.teacher.repository.TeacherRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CourseService {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;
    private final CompanyRepository companyRepository;
    private final SuperAdminRepository superAdminRepository;
    private final AdminRepository adminRepository;

    /**
     * Single API method for Create and Update:
     * - If dto.getId() is provided -> Updates the existing course
     * - If dto.getId() is null -> Creates a new course
     */
    @Transactional
    public CourseResponseDto saveOrUpdateCourse(CourseRequestDto dto) {
        String name = dto.getName() != null ? dto.getName().trim() : "";
        if (name.isEmpty()) {
            throw new ValidationError("Course name is required");
        }

        if (dto.getRelatedId() == null) {
            throw new ValidationError("Related ID is required");
        }

        if (dto.getRelatedType() == null || dto.getRelatedType().trim().isEmpty()) {
            throw new ValidationError("Related type is required (superadmin, company, teacher)");
        }

        RelatedType parsedType = parseRelatedType(dto.getRelatedType().trim());
        String relatedName = resolveRelatedName(dto.getRelatedId(), parsedType);

        CourseEntity courseEntity;
        boolean isUpdate = dto.getId() != null;

        if (isUpdate) {
            courseEntity = courseRepository.findById(dto.getId())
                    .orElseThrow(() -> new ValidationError("Course not found with id: " + dto.getId()));

            courseEntity.setName(name);
            courseEntity.setSubject(dto.getSubject() != null ? dto.getSubject().trim() : null);
            courseEntity.setShortName(dto.getShortName() != null ? dto.getShortName().trim() : null);
            if (dto.getStatus() != null) {
                courseEntity.setStatus(dto.getStatus());
            }
            courseEntity.setRelatedId(dto.getRelatedId());
            courseEntity.setRelatedType(parsedType);

            log.info("Course updated successfully: ID [{}] Name [{}] Related [{}:{}]",
                    courseEntity.getId(), courseEntity.getName(), parsedType, relatedName);
        } else {
            courseEntity = CourseEntity.builder()
                    .name(name)
                    .subject(dto.getSubject() != null ? dto.getSubject().trim() : null)
                    .shortName(dto.getShortName() != null ? dto.getShortName().trim() : null)
                    .status(dto.getStatus() != null ? dto.getStatus() : 0)
                    .relatedId(dto.getRelatedId())
                    .relatedType(parsedType)
                    .build();

            log.info("Creating new course: Name [{}] Related [{}:{}]",
                    name, parsedType, relatedName);
        }

        CourseEntity saved = courseRepository.save(courseEntity);
        return mapToDto(saved, relatedName);
    }

    /**
     * Resolves the name of the related entity from teachers, companies, superadmins, or admins table.
     */
    public String resolveRelatedName(Long relatedId, RelatedType type) {
        if (relatedId == null || type == null) {
            return null;
        }

        switch (type) {
            case teacher:
                TeacherEntity teacher = teacherRepository.findById(relatedId)
                        .orElseThrow(() -> new ValidationError("Teacher not found with id: " + relatedId));
                return teacher.getName();

            case company:
                CompanyEntity company = companyRepository.findById(relatedId)
                        .orElseThrow(() -> new ValidationError("Company not found with id: " + relatedId));
                return company.getName();

            case superadmin:
                Optional<SuperAdminEntity> superAdmin = superAdminRepository.findById(relatedId);
                if (superAdmin.isPresent()) {
                    return superAdmin.get().getFullName();
                }
                Optional<AdminEntity> admin = adminRepository.findById(relatedId);
                if (admin.isPresent()) {
                    return admin.get().getFullName();
                }
                throw new ValidationError("Super Admin or Admin not found with id: " + relatedId);

            default:
                throw new ValidationError("Unsupported related type: " + type);
        }
    }

    public List<CourseResponseDto> getAllCourses() {
        return courseRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(course -> {
                    String relatedName = null;
                    try {
                        relatedName = resolveRelatedName(course.getRelatedId(), course.getRelatedType());
                    } catch (Exception ignored) {}
                    return mapToDto(course, relatedName);
                })
                .collect(Collectors.toList());
    }

    public CourseResponseDto getCourseById(Long id) {
        if (id == null) {
            throw new ValidationError("Course ID is required");
        }
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Course not found with id: " + id));

        String relatedName = null;
        try {
            relatedName = resolveRelatedName(entity.getRelatedId(), entity.getRelatedType());
        } catch (Exception ignored) {}

        return mapToDto(entity, relatedName);
    }

    @Transactional
    public void deleteCourse(Long id) {
        if (id == null) {
            throw new ValidationError("Course ID is required");
        }
        CourseEntity entity = courseRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Course not found with id: " + id));
        courseRepository.delete(entity);
        log.info("Course deleted successfully with id: {}", id);
    }

    private RelatedType parseRelatedType(String typeStr) {
        String normalized = typeStr.toLowerCase().replaceAll("[_\\-\\s]", "");
        if (normalized.equals("superadmin") || normalized.equals("admin")) {
            return RelatedType.superadmin;
        } else if (normalized.equals("company")) {
            return RelatedType.company;
        } else if (normalized.equals("teacher")) {
            return RelatedType.teacher;
        } else {
            throw new ValidationError("Invalid related_type: '" + typeStr + "'. Allowed values are: superadmin, company, teacher");
        }
    }

    private CourseResponseDto mapToDto(CourseEntity entity, String relatedName) {
        return CourseResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .subject(entity.getSubject())
                .shortName(entity.getShortName())
                .status(entity.getStatus())
                .relatedId(entity.getRelatedId())
                .relatedType(entity.getRelatedType() != null ? entity.getRelatedType().name() : null)
                .relatedName(relatedName)
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

