package skyware.product.skywareapi.teacher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.company.entity.CompanyEntity;
import skyware.product.skywareapi.company.repository.CompanyRepository;
import skyware.product.skywareapi.teacher.dto.TeacherCompanyMapRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherCompanyMapResponseDto;
import skyware.product.skywareapi.teacher.entity.TeacherCompanyMapEntity;
import skyware.product.skywareapi.teacher.entity.TeacherEntity;
import skyware.product.skywareapi.teacher.repository.TeacherCompanyMapRepository;
import skyware.product.skywareapi.teacher.repository.TeacherRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherCompanyMapService {

    private final TeacherCompanyMapRepository teacherCompanyMapRepository;
    private final TeacherRepository teacherRepository;
    private final CompanyRepository companyRepository;

    /**
     * Map a teacher to a company.
     * Constraint: A teacher can only be mapped to ONE company with active status (1) at a time.
     * If an active mapping already exists, an error is thrown until the status is set to 0.
     */
    @Transactional
    public TeacherCompanyMapResponseDto mapTeacherToCompany(TeacherCompanyMapRequestDto dto) {
        Long teacherId = dto.getTeacherId();
        Long companyId = dto.getCompanyId();

        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }
        if (companyId == null) {
            throw new ValidationError("Company ID is required");
        }

        // 1. Verify Teacher exists
        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + teacherId));

        // 2. Verify Company exists
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ValidationError("Company not found with id: " + companyId));

        // 3. Check if teacher already has any active mapping (status == 1)
        Optional<TeacherCompanyMapEntity> activeMappingOpt = teacherCompanyMapRepository.findByTeacherIdAndStatus(teacherId, 1);
        if (activeMappingOpt.isPresent()) {
            TeacherCompanyMapEntity activeMap = activeMappingOpt.get();
            if (activeMap.getCompanyId().equals(companyId)) {
                throw new ValidationError("Teacher '" + teacher.getName() + "' is already actively mapped to this company ('" + company.getName() + "').");
            } else {
                CompanyEntity currentCompany = companyRepository.findById(activeMap.getCompanyId()).orElse(null);
                String currentCompanyName = currentCompany != null ? currentCompany.getName() : "ID " + activeMap.getCompanyId();
                throw new ValidationError("This teacher is already actively mapped to company '" + currentCompanyName + "'. A teacher can only be mapped to 1 company at a time. Please set the existing mapping status to 0 before mapping to a new company.");
            }
        }

        // 4. No active mapping exists: check if this (teacherId, companyId) mapping existed before with status 0
        Optional<TeacherCompanyMapEntity> existingMapOpt = teacherCompanyMapRepository.findByTeacherIdAndCompanyId(teacherId, companyId);
        TeacherCompanyMapEntity saved;

        if (existingMapOpt.isPresent()) {
            TeacherCompanyMapEntity existing = existingMapOpt.get();
            existing.setStatus(1);
            saved = teacherCompanyMapRepository.save(existing);
            log.info("Reactivated Teacher [{}] mapping to Company [{}] with status 1", teacherId, companyId);
        } else {
            TeacherCompanyMapEntity newMap = TeacherCompanyMapEntity.builder()
                    .teacherId(teacherId)
                    .companyId(companyId)
                    .status(1)
                    .build();
            saved = teacherCompanyMapRepository.save(newMap);
            log.info("Mapped Teacher [{}] to Company [{}] with status 1", teacherId, companyId);
        }

        return mapToDto(saved, teacher, company);
    }

    /**
     * Get active or all company mappings for a specific teacher.
     */
    public List<TeacherCompanyMapResponseDto> getTeacherCompanyMappings(Long teacherId) {
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }
        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + teacherId));

        List<TeacherCompanyMapEntity> mappings = teacherCompanyMapRepository.findByTeacherId(teacherId);
        List<TeacherCompanyMapResponseDto> result = new ArrayList<>();

        for (TeacherCompanyMapEntity map : mappings) {
            CompanyEntity company = companyRepository.findById(map.getCompanyId()).orElse(null);
            result.add(mapToDto(map, teacher, company));
        }

        return result;
    }

    /**
     * Get all teacher-company mappings in the system.
     */
    public List<TeacherCompanyMapResponseDto> getAllTeacherCompanyMappings() {
        List<TeacherCompanyMapEntity> mappings = teacherCompanyMapRepository.findAll();
        List<TeacherCompanyMapResponseDto> result = new ArrayList<>();

        for (TeacherCompanyMapEntity map : mappings) {
            TeacherEntity teacher = teacherRepository.findById(map.getTeacherId()).orElse(null);
            CompanyEntity company = companyRepository.findById(map.getCompanyId()).orElse(null);
            result.add(mapToDto(map, teacher, company));
        }

        return result;
    }

    /**
     * Toggle or set status (e.g. 1 -> 0 or 0 -> 1).
     * If activating (0 -> 1), verifies teacher has no other active mapping.
     */
    @Transactional
    public TeacherCompanyMapResponseDto toggleStatus(Long teacherId, Long companyId) {
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }
        if (companyId == null) {
            throw new ValidationError("Company ID is required");
        }

        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + teacherId));
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ValidationError("Company not found with id: " + companyId));

        TeacherCompanyMapEntity mapEntity = teacherCompanyMapRepository.findByTeacherIdAndCompanyId(teacherId, companyId)
                .orElseThrow(() -> new ValidationError("Mapping not found between Teacher ID " + teacherId + " and Company ID " + companyId));

        int currentStatus = (mapEntity.getStatus() != null) ? mapEntity.getStatus() : 0;
        int newStatus = (currentStatus == 1) ? 0 : 1;

        // If reactivating to 1, ensure teacher doesn't have another company actively mapped
        if (newStatus == 1) {
            Optional<TeacherCompanyMapEntity> activeMapOpt = teacherCompanyMapRepository.findByTeacherIdAndStatus(teacherId, 1);
            if (activeMapOpt.isPresent() && !activeMapOpt.get().getId().equals(mapEntity.getId())) {
                throw new ValidationError("Cannot activate mapping: Teacher is already actively mapped to another company (Company ID: " + activeMapOpt.get().getCompanyId() + "). Please set that mapping's status to 0 first.");
            }
        }

        mapEntity.setStatus(newStatus);
        TeacherCompanyMapEntity updated = teacherCompanyMapRepository.save(mapEntity);
        log.info("Teacher [{}] and Company [{}] mapping status toggled to [{}]", teacherId, companyId, newStatus);

        return mapToDto(updated, teacher, company);
    }

    /**
     * Deactivates a teacher-company mapping by setting status to 0.
     */
    @Transactional
    public TeacherCompanyMapResponseDto deactivateMapping(Long teacherId, Long companyId) {
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }
        if (companyId == null) {
            throw new ValidationError("Company ID is required");
        }

        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + teacherId));
        CompanyEntity company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ValidationError("Company not found with id: " + companyId));

        TeacherCompanyMapEntity mapEntity = teacherCompanyMapRepository.findByTeacherIdAndCompanyId(teacherId, companyId)
                .orElseThrow(() -> new ValidationError("Mapping not found between Teacher ID " + teacherId + " and Company ID " + companyId));

        mapEntity.setStatus(0);
        TeacherCompanyMapEntity updated = teacherCompanyMapRepository.save(mapEntity);
        log.info("Teacher [{}] and Company [{}] mapping deactivated (status = 0)", teacherId, companyId);

        return mapToDto(updated, teacher, company);
    }

    /**
     * Unmap / permanently delete mapping record between teacher and company.
     */
    @Transactional
    public void unmapTeacherCompany(Long teacherId, Long companyId) {
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }
        if (companyId == null) {
            throw new ValidationError("Company ID is required");
        }

        TeacherCompanyMapEntity mapEntity = teacherCompanyMapRepository.findByTeacherIdAndCompanyId(teacherId, companyId)
                .orElseThrow(() -> new ValidationError("Mapping not found between Teacher ID " + teacherId + " and Company ID " + companyId));

        teacherCompanyMapRepository.delete(mapEntity);
        log.info("Permanently deleted mapping between Teacher [{}] and Company [{}]", teacherId, companyId);
    }

    private TeacherCompanyMapResponseDto mapToDto(TeacherCompanyMapEntity map, TeacherEntity teacher, CompanyEntity company) {
        return TeacherCompanyMapResponseDto.builder()
                .mapId(map.getId())
                .teacherId(map.getTeacherId())
                .teacherName(teacher != null ? teacher.getName() : null)
                .teacherEmail(teacher != null ? teacher.getEmail() : null)
                .teacherPhone(teacher != null ? teacher.getPhone() : null)
                .companyId(map.getCompanyId())
                .companyName(company != null ? company.getName() : null)
                .companyEmail(company != null ? company.getEmail() : null)
                .companyPhone(company != null ? company.getPhone() : null)
                .companyDomain(company != null ? company.getDomain() : null)
                .status(map.getStatus())
                .createdAt(map.getCreatedAt())
                .updatedAt(map.getUpdatedAt())
                .build();
    }
}

