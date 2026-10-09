package skyware.product.skywareapi.mapsidebar.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "role_sidebar_maps")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleSidebarMapperEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, updatable = false)
    private Long id;

    @Column(name = "sidebar_id")
    private Long sidebarId;

    @Column(name = "role_id")
    private Long roleId;

    @Column(name = "status_id")
    private Long statusId;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
