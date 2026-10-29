package skyware.product.skywareapi.teacher.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherCompanyMapRequestDto {

    @NotNull(message = "Teacher ID is required")
    @JsonAlias({"teacher_id", "teacherId"})
    private Long teacherId;

    @NotNull(message = "Company ID is required")
    @JsonAlias({"company_id", "companyId"})
    private Long companyId;

    @Builder.Default
    private Integer status = 1;
}

