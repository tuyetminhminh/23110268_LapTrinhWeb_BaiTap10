package vn.org.com.entity;

import java.io.Serializable;
import java.util.Set;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", nullable = false, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Tên danh mục không được trống")
    private String name;

    @Column(name = "images", nullable = false, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Hình ảnh không được trống")
    private String images;

    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Product> products;

    @ManyToMany(mappedBy = "categories")
    private Set<User> users;
}
