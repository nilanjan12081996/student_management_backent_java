package skyware.product.skywareapi.sidebar.mapper;


import skyware.product.skywareapi.sidebar.dto.SidebarDto;
import skyware.product.skywareapi.sidebar.entity.SidebarEntity;

public final class SidebarMapper {

    private SidebarMapper() {
        // Private constructor to prevent instantiation
    }

    public static SidebarDto toDto(SidebarEntity entity) {
        if (entity == null) {
            return null;
        }

        return SidebarDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .shortName(entity.getShortName())
                .parentId(entity.getParentId())
                .statusId(entity.getStatusId())
                .isActive(entity.getIsActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public static SidebarEntity toEntity(SidebarDto dto) {
        if (dto == null) {
            return null;
        }

        return SidebarEntity.builder()
                .id(dto.getId())
                .name(dto.getName())
                .shortName(dto.getShortName())
                .parentId(dto.getParentId() != null ? dto.getParentId() : 0L)
                .statusId(dto.getStatusId())
                .isActive(dto.getIsActive() != null ? dto.getIsActive() : true)
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .build();
    }

    public static void updateEntityFromDto(SidebarDto dto, SidebarEntity entity) {
        if (dto == null || entity == null) {
            return;
        }

        if (dto.getName() != null) {
            entity.setName(dto.getName());
        }
        if (dto.getShortName() != null) {
            entity.setShortName(dto.getShortName());
        }
        if (dto.getParentId() != null) {
            entity.setParentId(dto.getParentId());
        }
        if (dto.getStatusId() != null) {
            entity.setStatusId(dto.getStatusId());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        }
    }
}