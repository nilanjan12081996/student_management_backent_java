package skyware.product.skywareapi.superadmin.mapper;

import skyware.product.skywareapi.superadmin.dto.SuperAdminDto;
import skyware.product.skywareapi.superadmin.entity.SuperAdminEntity;

public class SuperAdminMapper {

    public static SuperAdminEntity superAdmin(SuperAdminDto superAdminDto){
        SuperAdminEntity superAdminEntity = SuperAdminEntity.builder()
                .email(superAdminDto.getEmail())
                .createdAt(superAdminDto.getCreatedAt())
                .isActive(superAdminDto.getIsActive())
                .updatedAt(superAdminDto.getUpdatedAt())
                .id(superAdminDto.getId())
                .oAuth(superAdminDto.getOAuth())
                .mobile(superAdminDto.getMobile())
                .fullName(superAdminDto.getFullName())
                .avatar(superAdminDto.getAvatar())
                .build();

        return superAdminEntity;

    }
    public static SuperAdminDto superAdminDto(SuperAdminEntity superAdminEntity){
                 return SuperAdminDto.builder()
                .email(superAdminEntity.getEmail())
                .createdAt(superAdminEntity.getCreatedAt())
                .isActive(superAdminEntity.getIsActive())
                .updatedAt(superAdminEntity.getUpdatedAt())
                .id(superAdminEntity.getId())
                .oAuth(superAdminEntity.getOAuth())
                .mobile(superAdminEntity.getMobile())
                .fullName(superAdminEntity.getFullName())
                .avatar(superAdminEntity.getAvatar())
                .build();

    }
}
