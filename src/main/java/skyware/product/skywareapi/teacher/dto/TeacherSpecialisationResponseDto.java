package skyware.product.skywareapi.teacher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherSpecialisationResponseDto {

    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherPhone;
    private Integer totalSpecialisations;
    private List<SpecialisationItemDto> specialisations;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class SpecialisationItemDto {
        private Long mapId;
        private Long spId;
        private String spName;
        private String shortName;
        private Integer status;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}

