package skyware.product.skywareapi.sidebar.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import skyware.product.skywareapi.sidebar.entity.SidebarEntity;

public interface SidebarRepository extends JpaRepository<SidebarEntity,Long>, JpaSpecificationExecutor<Long> {
}
