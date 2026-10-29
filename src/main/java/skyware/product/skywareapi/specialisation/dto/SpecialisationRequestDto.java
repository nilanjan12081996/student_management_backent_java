package skyware.product.skywareapi.specialisation.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialisationRequestDto {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Short name is required")
    private String shortName;

    private Integer status;
}
