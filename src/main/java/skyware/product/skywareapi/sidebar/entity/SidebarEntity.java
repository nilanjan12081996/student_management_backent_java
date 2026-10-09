package skyware.product.skywareapi.sidebar.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "sidebars")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SidebarEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 255)
    private String name;

    @Column(name = "short_name", length = 255, nullable = false, unique = true)
    private String shortName;

    @Builder.Default
    @Column(name = "parent_id")
    private Long parentId = 0L;

    @Column(name = "status_id")
    private Long statusId;

    @Builder.Default
    @Column(name = "is_active")
    private Boolean isActive = true;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
