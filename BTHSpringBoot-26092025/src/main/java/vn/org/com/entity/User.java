package vn.org.com.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.io.Serializable;
import java.util.Set;

import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "NVARCHAR(255)")
    @NotBlank(message = "Vui lòng điền đầy đủ họ tên")
    private String fullname;

    @Column(nullable = false, unique = true)
    @Email(message = "Bạn chưa điền địa chỉ Email")
    private String email;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    @Size(min = 8, message = "Mật khẩu phải có ít nhất 8 kí tự")
	@NotBlank(message = "Vui lòng điền mật khẩu")
    private String password;

    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    @Pattern(regexp = "0\\d{9}", message = "Số điện thoại phải bắt đầu bằng '0' và có 10 chữ số")
    @NotBlank(message = "Vui lòng điền số điện thoại")
    private String phone;
    
    //Phân quyền

    public enum Role { ADMIN, USER }
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "VARCHAR(10)")
    private Role role; // ADMIN, USER

    // Many-to-many with Category
    @ManyToMany
    @JoinTable(
        name = "user_categories",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories;

    // One-to-many with Product
    @OneToMany(mappedBy = "user")
    private Set<Product> products;
    

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
