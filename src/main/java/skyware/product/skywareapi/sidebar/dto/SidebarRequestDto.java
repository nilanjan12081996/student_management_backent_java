package skyware.product.skywareapi.sidebar.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SidebarRequestDto {

    @NotBlank(message = "name required")
    private String name;

    @NotBlank(message = "name required")
    private String shortName;

    @NotNull(message="parentId required")
    private Long parentId;
}
