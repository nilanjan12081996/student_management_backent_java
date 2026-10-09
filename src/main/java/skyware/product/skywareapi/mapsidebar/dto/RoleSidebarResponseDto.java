package skyware.product.skywareapi.mapsidebar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import skyware.product.skywareapi.mapsidebar.entity.RoleSidebarMapperEntity;
import skyware.product.skywareapi.role.entity.RoleEntity;
import skyware.product.skywareapi.sidebar.entity.SidebarEntity;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSidebarResponseDto {
    private RoleSidebarMapperEntity mapDetails;
    private RoleEntity role;
    private SidebarEntity sidebar;
}
