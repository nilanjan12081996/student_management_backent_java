package skyware.product.skywareapi.teacher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skyware.product.skywareapi.teacher.entity.TeacherCompanyMapEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherCompanyMapRepository extends JpaRepository<TeacherCompanyMapEntity, Long> {
    List<TeacherCompanyMapEntity> findByTeacherId(Long teacherId);
    List<TeacherCompanyMapEntity> findByCompanyId(Long companyId);
    Optional<TeacherCompanyMapEntity> findByTeacherIdAndCompanyId(Long teacherId, Long companyId);
    Optional<TeacherCompanyMapEntity> findByTeacherIdAndStatus(Long teacherId, Integer status);
    boolean existsByTeacherIdAndStatus(Long teacherId, Integer status);
    void deleteByTeacherIdAndCompanyId(Long teacherId, Long companyId);
}
