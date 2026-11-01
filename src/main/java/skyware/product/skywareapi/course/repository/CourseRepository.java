package skyware.product.skywareapi.course.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import skyware.product.skywareapi.course.entity.CourseEntity;
import skyware.product.skywareapi.course.entity.RelatedType;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<CourseEntity, Long> {
    List<CourseEntity> findByRelatedIdAndRelatedType(Long relatedId, RelatedType relatedType);
    List<CourseEntity> findByStatus(Integer status);
}

