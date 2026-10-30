package skyware.product.skywareapi.company.repository;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skyware.product.skywareapi.company.entity.CompanyEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<CompanyEntity, Long> {
    Optional<CompanyEntity> findByEmail(String email);
    Optional<CompanyEntity> findByPhone(String phone);
    List<CompanyEntity> findByAdminApprove(Integer adminApprove, Sort sort);
    List<CompanyEntity> findByAdminApprove(Integer adminApprove);
}
