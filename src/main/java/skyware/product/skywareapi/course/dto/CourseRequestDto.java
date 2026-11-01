package skyware.product.skywareapi.course.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CourseRequestDto {

    // If id is provided -> update; if omitted / null -> create
    private Long id;

    @NotBlank(message = "Course name is required")
    private String name;

    private String subject;

    @JsonAlias({"short_name", "shortName"})
    private String shortName;

    @Builder.Default
    private Integer status = 0;

    @NotNull(message = "Related ID is required")
    @JsonAlias({"related_id", "relatedId"})
    private Long relatedId;

    @NotBlank(message = "Related type is required (superadmin, company, teacher)")
    @JsonAlias({"related_type", "relatedType"})
    private String relatedType;
}

