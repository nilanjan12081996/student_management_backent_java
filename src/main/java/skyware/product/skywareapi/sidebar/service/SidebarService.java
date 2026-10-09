package skyware.product.skywareapi.sidebar.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import skyware.product.skywareapi.sidebar.dto.SidebarRequestDto;
import skyware.product.skywareapi.sidebar.entity.SidebarEntity;
import skyware.product.skywareapi.sidebar.repository.SidebarRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SidebarService {

    private final SidebarRepository sidebarRepository;

    public void saveSidebar(SidebarRequestDto sidebarRequestDto){
        SidebarEntity sidebarEntity = SidebarEntity.builder()
                .name(sidebarRequestDto.getName())
                .shortName(sidebarRequestDto.getShortName())
                .parentId(sidebarRequestDto.getParentId())
                .statusId(1L)
                .build();
        sidebarRepository.save(sidebarEntity);
    }

    public List<SidebarEntity> getAllSidebars() {
        return sidebarRepository.findAll();
    }
}
