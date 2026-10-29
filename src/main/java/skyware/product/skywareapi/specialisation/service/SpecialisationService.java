package skyware.product.skywareapi.specialisation.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.specialisation.dto.SpecialisationRequestDto;
import skyware.product.skywareapi.specialisation.dto.SpecialisationResponseDto;
import skyware.product.skywareapi.specialisation.entity.SpecialisationEntity;
import skyware.product.skywareapi.specialisation.repository.SpecialisationRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SpecialisationService {

    private final SpecialisationRepository specialisationRepository;

    @Transactional
    public SpecialisationResponseDto addSpecialisation(SpecialisationRequestDto dto) {
        String name = dto.getName() != null ? dto.getName().trim() : "";
        String shortName = dto.getShortName() != null ? dto.getShortName().trim() : "";

        if (name.isEmpty()) {
            throw new ValidationError("Name is required");
        }
        if (shortName.isEmpty()) {
            throw new ValidationError("Short name is required");
        }

        // Check name and short_name are not identical
        if (name.equalsIgnoreCase(shortName)) {
            throw new ValidationError("Name and short name cannot be the same");
        }

        // Check if name already exists
        if (specialisationRepository.existsByName(name)) {
            throw new ValidationError("Specialisation with name '" + name + "' already exists");
        }

        // Check if short_name already exists
        if (specialisationRepository.existsByShortName(shortName)) {
            throw new ValidationError("Specialisation with short name '" + shortName + "' already exists");
        }

        SpecialisationEntity entity = SpecialisationEntity.builder()
                .name(name)
                .shortName(shortName)
                .status(dto.getStatus() != null ? dto.getStatus() : 1)
                .build();

        SpecialisationEntity saved = specialisationRepository.save(entity);
        log.info("Specialisation created: ID [{}] Name [{}] ShortName [{}]", saved.getId(), saved.getName(), saved.getShortName());

        return mapToDto(saved);
    }

    public List<SpecialisationResponseDto> getAllSpecialisations() {
        return specialisationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"))
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    public SpecialisationResponseDto getSpecialisationById(Long id) {
        if (id == null) {
            throw new ValidationError("Specialisation ID is required");
        }
        SpecialisationEntity entity = specialisationRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Specialisation not found with id: " + id));
        return mapToDto(entity);
    }

    @Transactional
    public SpecialisationResponseDto updateSpecialisation(Long id, SpecialisationRequestDto dto) {
        if (id == null) {
            throw new ValidationError("Specialisation ID is required");
        }

        SpecialisationEntity existing = specialisationRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Specialisation not found with id: " + id));

        String name = dto.getName() != null ? dto.getName().trim() : "";
        String shortName = dto.getShortName() != null ? dto.getShortName().trim() : "";

        if (name.isEmpty()) {
            throw new ValidationError("Name is required");
        }
        if (shortName.isEmpty()) {
            throw new ValidationError("Short name is required");
        }

        // Check name and short_name are not identical
        if (name.equalsIgnoreCase(shortName)) {
            throw new ValidationError("Name and short name cannot be the same");
        }

        // Check if name already exists on another record
        if (specialisationRepository.existsByNameAndIdNot(name, id)) {
            throw new ValidationError("Specialisation with name '" + name + "' already exists");
        }

        // Check if short_name already exists on another record
        if (specialisationRepository.existsByShortNameAndIdNot(shortName, id)) {
            throw new ValidationError("Specialisation with short name '" + shortName + "' already exists");
        }

        existing.setName(name);
        existing.setShortName(shortName);
        if (dto.getStatus() != null) {
            existing.setStatus(dto.getStatus());
        }

        SpecialisationEntity updated = specialisationRepository.save(existing);
        log.info("Specialisation updated: ID [{}] Name [{}] ShortName [{}]", updated.getId(), updated.getName(), updated.getShortName());

        return mapToDto(updated);
    }

    @Transactional
    public void deleteSpecialisation(Long id) {
        if (id == null) {
            throw new ValidationError("Specialisation ID is required");
        }

        SpecialisationEntity existing = specialisationRepository.findById(id)
                .orElseThrow(() -> new ValidationError("Specialisation not found with id: " + id));

        specialisationRepository.delete(existing);
        log.info("Specialisation deleted: ID [{}] Name [{}]", id, existing.getName());
    }

    public SpecialisationResponseDto mapToDto(SpecialisationEntity entity) {
        return SpecialisationResponseDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .shortName(entity.getShortName())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
