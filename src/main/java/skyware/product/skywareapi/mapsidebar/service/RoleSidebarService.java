package skyware.product.skywareapi.mapsidebar.service;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import skyware.product.error.ValidationError;
import skyware.product.skywareapi.mapsidebar.dto.RoleSidebarMapDto;
import skyware.product.skywareapi.mapsidebar.entity.RoleSidebarMapperEntity;
import skyware.product.skywareapi.mapsidebar.repository.RoleSidebarMapRepository;
import skyware.product.skywareapi.role.entity.RoleEntity;
import skyware.product.skywareapi.role.repository.RoleRepository;
import skyware.product.skywareapi.sidebar.entity.SidebarEntity;
import skyware.product.skywareapi.sidebar.repository.SidebarRepository;

import skyware.product.skywareapi.mapsidebar.dto.RoleSidebarResponseDto;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
public class RoleSidebarService {

    private final RoleSidebarMapRepository roleSidebarMapRepository;
    private final SidebarRepository sidebarRepository;
    private final RoleRepository roleRepository;
    private final Executor dbExecutor;

    public RoleSidebarMapDto roleMap(RoleSidebarMapDto roleSidebarMapDto){
            CompletableFuture<Optional<RoleEntity>> role = CompletableFuture.supplyAsync(()->{
                    return  roleRepository.findById(roleSidebarMapDto.getRoleId());
            },dbExecutor);
            CompletableFuture<Optional<SidebarEntity>> sidebar = CompletableFuture.supplyAsync(()->{
                    return sidebarRepository.findById(roleSidebarMapDto.getSidebarId());
            },dbExecutor);
            CompletableFuture<Optional<RoleSidebarMapperEntity>> roleSidebarMap = CompletableFuture.supplyAsync(()->{
                    return roleSidebarMapRepository.findBySidebarIdAndRoleId(roleSidebarMapDto.getSidebarId(),roleSidebarMapDto.getRoleId());
            },dbExecutor);
            CompletableFuture<RoleSidebarMapDto> resultFuture  = CompletableFuture.allOf(role,sidebar,roleSidebarMap).thenApply(voidResult->{
                Optional<RoleEntity> roleOption = role.join();
                Optional<SidebarEntity> sidebarOption = sidebar.join();
                Optional<RoleSidebarMapperEntity> roleSidebarMapOption = roleSidebarMap.join();
                if(roleOption.isEmpty()){
                    throw new ValidationError("role not found ");
                }
                if(sidebarOption.isEmpty()){
                    throw new ValidationError("sidebar not found ");
                }
                if(roleSidebarMapOption.isPresent()){
                    throw new ValidationError("role and sidebar already mapped");
                }

                RoleSidebarMapperEntity roleSideMap = RoleSidebarMapperEntity.builder()
                        .roleId(roleSidebarMapDto.getRoleId())
                        .sidebarId(roleSidebarMapDto.getSidebarId())
                        .statusId(1L)
                        .build();
                RoleSidebarMapperEntity roleSideMapSave = roleSidebarMapRepository.save(roleSideMap);

                return RoleSidebarMapDto.builder()
                                .id(roleSideMap.getId())
                                .sidebarId(roleSideMap.getSidebarId())
                                .roleId(roleSideMap.getRoleId())
                                .statusId(roleSideMap.getStatusId())
                                .build();
            });

        return resultFuture.join();
    }

    public List<RoleSidebarResponseDto> getAllMappedSidebars() {
        List<RoleSidebarMapperEntity> maps = roleSidebarMapRepository.findAll();
        List<RoleSidebarResponseDto> responseList = new ArrayList<>();
        
        for (RoleSidebarMapperEntity map : maps) {
            RoleEntity role = roleRepository.findById(map.getRoleId()).orElse(null);
            SidebarEntity sidebar = sidebarRepository.findById(map.getSidebarId()).orElse(null);
            
            responseList.add(RoleSidebarResponseDto.builder()
                .mapDetails(map)
                .role(role)
                .sidebar(sidebar)
                .build());
        }
        
        return responseList;
    }
}
