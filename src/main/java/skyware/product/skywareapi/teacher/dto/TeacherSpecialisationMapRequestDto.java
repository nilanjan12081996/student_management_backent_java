package skyware.product.skywareapi.teacher.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TeacherSpecialisationMapRequestDto {

    @NotNull(message = "Teacher ID is required")
    private Long teacherId;

    // Supports single spId or list of spIds
    @JsonAlias({"specialisationId", "specializationId", "spId"})
    private Long spId;

    @JsonAlias({"specialisationIds", "specializationIds", "sp_ids"})
    private List<Long> spIds;

    @Builder.Default
    private Integer status = 1;

    /**
     * Resolves all provided specialisation IDs from either spIds list or single spId,
     * removing duplicates and nulls.
     */
    public List<Long> resolveSpIds() {
        List<Long> result = new ArrayList<>();
        if (spIds != null) {
            for (Long id : spIds) {
                if (id != null && !result.contains(id)) {
                    result.add(id);
                }
            }
        }
        if (spId != null && !result.contains(spId)) {
            result.add(spId);
        }
        return result;
    }
}

