package skyware.product.skywareapi.specialisation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skyware.product.skywareapi.specialisation.entity.SpecialisationEntity;

import java.util.Optional;

@Repository
public interface SpecialisationRepository extends JpaRepository<SpecialisationEntity, Long> {
    Optional<SpecialisationEntity> findByName(String name);
    Optional<SpecialisationEntity> findByShortName(String shortName);
    boolean existsByName(String name);
    boolean existsByShortName(String shortName);
    boolean existsByNameAndIdNot(String name, Long id);
    boolean existsByShortNameAndIdNot(String shortName, Long id);
}
