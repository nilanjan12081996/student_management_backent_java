package skyware.product.skywareapi.mapsidebar.mapper;

import skyware.product.skywareapi.mapsidebar.dto.RoleSidebarMapDto;
import skyware.product.skywareapi.mapsidebar.entity.RoleSidebarMapperEntity;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public final class RoleSidebarMapper {

    private RoleSidebarMapper() {
        // Utility class; prevent instantiation
    }

    public static RoleSidebarMapDto toDto(RoleSidebarMapperEntity entity) {
        if (entity == null) {
            return null;
        }

        return RoleSidebarMapDto.builder()
                .id(entity.getId())
                .sidebarId(entity.getSidebarId())
                .roleId(entity.getRoleId())
                .statusId(entity.getStatusId())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static RoleSidebarMapperEntity toEntity(RoleSidebarMapDto dto) {
        if (dto == null) {
            return null;
        }

        return RoleSidebarMapperEntity.builder()
                .id(dto.getId())
                .sidebarId(dto.getSidebarId())
                .roleId(dto.getRoleId())
                .statusId(dto.getStatusId())
                .isActive(dto.getIsActive())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static List<RoleSidebarMapDto> toDtoList(List<RoleSidebarMapperEntity> entities) {
        if (entities == null || entities.isEmpty()) {
            return Collections.emptyList();
        }

        return entities.stream()
                .map(RoleSidebarMapper::toDto)
                .collect(Collectors.toList());
    }

    public static List<RoleSidebarMapperEntity> toEntityList(List<RoleSidebarMapDto> dtos) {
        if (dtos == null || dtos.isEmpty()) {
            return Collections.emptyList();
        }

        return dtos.stream()
                .map(RoleSidebarMapper::toEntity)
                .collect(Collectors.toList());
    }
}