package skyware.product.skywareapi.teacher.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.specialisation.entity.SpecialisationEntity;
import skyware.product.skywareapi.specialisation.repository.SpecialisationRepository;
import skyware.product.skywareapi.teacher.dto.TeacherSpecialisationMapRequestDto;
import skyware.product.skywareapi.teacher.dto.TeacherSpecialisationResponseDto;
import skyware.product.skywareapi.teacher.entity.TeacherEntity;
import skyware.product.skywareapi.teacher.entity.TeacherSpecialisationMapEntity;
import skyware.product.skywareapi.teacher.repository.TeacherRepository;
import skyware.product.skywareapi.teacher.repository.TeacherSpecialisationMapRepository;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TeacherSpecialisationMapService {

    private final TeacherSpecialisationMapRepository teacherSpecialisationMapRepository;
    private final TeacherRepository teacherRepository;
    private final SpecialisationRepository specialisationRepository;

    /**
     * Maps one or multiple specialisations to a teacher.
     */
    @Transactional
    public TeacherSpecialisationResponseDto mapSpecialisations(TeacherSpecialisationMapRequestDto dto) {
        Long teacherId = dto.getTeacherId();
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }

        // 1. Verify Teacher exists
        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + teacherId));

        // 2. Resolve specialisation IDs
        List<Long> spIds = dto.resolveSpIds();
        if (spIds.isEmpty()) {
            throw new ValidationError("At least one specialisation ID must be provided");
        }

        int targetStatus = (dto.getStatus() != null) ? dto.getStatus() : 1;

        // 3. Validate all specialisation IDs exist
        Map<Long, SpecialisationEntity> specialisationMap = new HashMap<>();
        for (Long spId : spIds) {
            SpecialisationEntity sp = specialisationRepository.findById(spId)
                    .orElseThrow(() -> new ValidationError("Specialisation not found with id: " + spId));
            specialisationMap.put(spId, sp);
        }

        // 4. Save or reactivate mappings
        for (Long spId : spIds) {
            Optional<TeacherSpecialisationMapEntity> existingMap = teacherSpecialisationMapRepository.findByTeacherIdAndSpId(teacherId, spId);

            if (existingMap.isPresent()) {
                TeacherSpecialisationMapEntity mapEntity = existingMap.get();
                if (!Objects.equals(mapEntity.getStatus(), targetStatus)) {
                    mapEntity.setStatus(targetStatus);
                    teacherSpecialisationMapRepository.save(mapEntity);
                    log.info("Teacher [{}] and Specialisation [{}] map status updated to [{}]", teacherId, spId, targetStatus);
                }
            } else {
                TeacherSpecialisationMapEntity newMap = TeacherSpecialisationMapEntity.builder()
                        .teacherId(teacherId)
                        .spId(spId)
                        .status(targetStatus)
                        .build();
                teacherSpecialisationMapRepository.save(newMap);
                log.info("Mapped Teacher [{}] to Specialisation [{}]", teacherId, spId);
            }
        }

        // 5. Return updated teacher details with all mapped specialisations
        return getSpecialisationsByTeacherId(teacherId);
    }

    /**
     * Get all specialisations mapped to a particular teacher.
     */
    public TeacherSpecialisationResponseDto getSpecialisationsByTeacherId(Long teacherId) {
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }

        TeacherEntity teacher = teacherRepository.findById(teacherId)
                .orElseThrow(() -> new ValidationError("Teacher not found with id: " + teacherId));

        List<TeacherSpecialisationMapEntity> mappings = teacherSpecialisationMapRepository.findByTeacherId(teacherId);

        List<TeacherSpecialisationResponseDto.SpecialisationItemDto> specialisationItems = mappings.stream()
                .map(map -> {
                    SpecialisationEntity sp = specialisationRepository.findById(map.getSpId()).orElse(null);
                    return TeacherSpecialisationResponseDto.SpecialisationItemDto.builder()
                            .mapId(map.getId())
                            .spId(map.getSpId())
                            .spName(sp != null ? sp.getName() : null)
                            .shortName(sp != null ? sp.getShortName() : null)
                            .status(map.getStatus())
                            .createdAt(map.getCreatedAt())
                            .updatedAt(map.getUpdatedAt())
                            .build();
                })
                .collect(Collectors.toList());

        return TeacherSpecialisationResponseDto.builder()
                .teacherId(teacher.getId())
                .teacherName(teacher.getName())
                .teacherEmail(teacher.getEmail())
                .teacherPhone(teacher.getPhone())
                .totalSpecialisations(specialisationItems.size())
                .specialisations(specialisationItems)
                .build();
    }

    /**
     * Get all teacher-specialisation mappings in the system.
     */
    public List<TeacherSpecialisationResponseDto> getAllTeacherSpecialisations() {
        List<TeacherSpecialisationMapEntity> allMaps = teacherSpecialisationMapRepository.findAll();
        Map<Long, List<TeacherSpecialisationMapEntity>> groupedByTeacher = allMaps.stream()
                .collect(Collectors.groupingBy(TeacherSpecialisationMapEntity::getTeacherId));

        List<TeacherSpecialisationResponseDto> result = new ArrayList<>();
        for (Map.Entry<Long, List<TeacherSpecialisationMapEntity>> entry : groupedByTeacher.entrySet()) {
            Long teacherId = entry.getKey();
            TeacherEntity teacher = teacherRepository.findById(teacherId).orElse(null);
            if (teacher == null) continue;

            List<TeacherSpecialisationResponseDto.SpecialisationItemDto> items = entry.getValue().stream()
                    .map(map -> {
                        SpecialisationEntity sp = specialisationRepository.findById(map.getSpId()).orElse(null);
                        return TeacherSpecialisationResponseDto.SpecialisationItemDto.builder()
                                .mapId(map.getId())
                                .spId(map.getSpId())
                                .spName(sp != null ? sp.getName() : null)
                                .shortName(sp != null ? sp.getShortName() : null)
                                .status(map.getStatus())
                                .createdAt(map.getCreatedAt())
                                .updatedAt(map.getUpdatedAt())
                                .build();
                    })
                    .collect(Collectors.toList());

            result.add(TeacherSpecialisationResponseDto.builder()
                    .teacherId(teacher.getId())
                    .teacherName(teacher.getName())
                    .teacherEmail(teacher.getEmail())
                    .teacherPhone(teacher.getPhone())
                    .totalSpecialisations(items.size())
                    .specialisations(items)
                    .build());
        }

        return result;
    }

    /**
     * Unmap / remove a specialisation from a teacher.
     */
    @Transactional
    public void unmapSpecialisation(Long teacherId, Long spId) {
        if (teacherId == null) {
            throw new ValidationError("Teacher ID is required");
        }
        if (spId == null) {
            throw new ValidationError("Specialisation ID is required");
        }

        TeacherSpecialisationMapEntity mapEntity = teacherSpecialisationMapRepository.findByTeacherIdAndSpId(teacherId, spId)
                .orElseThrow(() -> new ValidationError("Mapping not found for teacher id " + teacherId + " and specialisation id " + spId));

        teacherSpecialisationMapRepository.delete(mapEntity);
        log.info("Removed mapping between Teacher [{}] and Specialisation [{}]", teacherId, spId);
    }
}

