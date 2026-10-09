package skyware.product.skywareapi.role.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import skyware.product.skywareapi.role.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity,Long>, JpaSpecificationExecutor<Long> {

}
