package vn.org.com.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Table(name = "products", indexes = @Index(name = "idx_products_category", columnList = "category_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@ToString(exclude = {"category", "user"})
public class Product implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    @EqualsAndHashCode.Include
    private Long id;

    @Column(name = "title", columnDefinition = "NVARCHAR(255)", nullable = false)
    @NotBlank(message = "Tiêu đề sản phẩm không được trống")
    private String title;

    @Column(name = "quantity", nullable = false)
    @PositiveOrZero
    @NotNull(message = "Số lượng không được trống")
    private Integer quantity;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    @DecimalMin(value = "0.0", inclusive = true, message = "Đơn giá phải >= 0")
    @NotNull(message = "Giá bán không được trống")
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;
}
