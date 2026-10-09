package skyware.product.skywareapi.mapsidebar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import skyware.product.skywareapi.mapsidebar.entity.RoleSidebarMapperEntity;

import java.util.Optional;

public interface RoleSidebarMapRepository extends JpaRepository<RoleSidebarMapperEntity,Long>, JpaSpecificationExecutor<Long> {
    Optional<RoleSidebarMapperEntity>  findBySidebarIdAndRoleId(Long sidebarId,Long roleId);
}
