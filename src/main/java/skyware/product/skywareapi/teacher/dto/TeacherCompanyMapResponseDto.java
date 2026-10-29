package skyware.product.skywareapi.teacher.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TeacherCompanyMapResponseDto {

    private Long mapId;

    private Long teacherId;
    private String teacherName;
    private String teacherEmail;
    private String teacherPhone;

    private Long companyId;
    private String companyName;
    private String companyEmail;
    private String companyPhone;
    private String companyDomain;

    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

