package skyware.product.skywareapi.course.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CourseResponseDto {

    private Long id;
    private String name;
    private String subject;
    private String shortName;
    private Integer status;
    private Long relatedId;
    private String relatedType;
    private String relatedName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

