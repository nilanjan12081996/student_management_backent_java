package skyware.product.skywareapi.teacher.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skyware.product.skywareapi.teacher.entity.TeacherSpecialisationMapEntity;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherSpecialisationMapRepository extends JpaRepository<TeacherSpecialisationMapEntity, Long> {
    List<TeacherSpecialisationMapEntity> findByTeacherId(Long teacherId);
    List<TeacherSpecialisationMapEntity> findBySpId(Long spId);
    Optional<TeacherSpecialisationMapEntity> findByTeacherIdAndSpId(Long teacherId, Long spId);
    boolean existsByTeacherIdAndSpId(Long teacherId, Long spId);
    void deleteByTeacherIdAndSpId(Long teacherId, Long spId);
    void deleteByTeacherId(Long teacherId);
}
