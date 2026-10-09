package skyware.product.skywareapi.superadmin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import skyware.product.skywareapi.superadmin.entity.SuperAdminEntity;

import java.util.Optional;

@Repository
public interface SuperAdminRepository extends JpaRepository<SuperAdminEntity,Long>, JpaSpecificationExecutor<Long> {

    Optional<SuperAdminEntity> findByEmail(String email);
}
