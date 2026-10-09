package skyware.product.skywareapi.sidebar.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SidebarDto {

    private Long id;
    private String name;
    private String shortName;
    private Long parentId;
    private Long statusId;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}