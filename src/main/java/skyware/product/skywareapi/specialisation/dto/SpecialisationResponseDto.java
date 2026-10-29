package skyware.product.skywareapi.specialisation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationResponseDto {
    private Long id;
    private String name;
    private String shortName;
    private Integer status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
