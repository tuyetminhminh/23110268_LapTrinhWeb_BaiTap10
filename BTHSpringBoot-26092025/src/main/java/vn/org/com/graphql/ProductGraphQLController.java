package vn.org.com.graphql;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import vn.org.com.entity.Product;
import vn.org.com.entity.Category;
import vn.org.com.entity.User;
import vn.org.com.repository.ProductRepository;
import vn.org.com.repository.CategoryRepository;
import vn.org.com.repository.UserRepository;

import java.util.List;
import java.util.Optional;

@Controller
@RequiredArgsConstructor
public class ProductGraphQLController {
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;

    @QueryMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Product> allProductsSortedByPrice() {
        return productRepository.findAll()
                .stream()
                .sorted((a, b) -> a.getPrice().compareTo(b.getPrice()))
                .toList();
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public List<Product> productsByCategory(@Argument Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        return category.map(c -> c.getProducts().stream().toList()).orElse(List.of());
    }

    @QueryMapping
    @PreAuthorize("hasAnyRole('USER','ADMIN')")
    public Product getProduct(@Argument Long productId) {
        return productRepository.findById(productId).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product createProduct(@Argument String title,
                                 @Argument Integer quantity,
                                 @Argument String description,
                                 @Argument Double price,
                                 @Argument Long categoryId,
                                 @Argument Long userId) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        User user = userRepository.findById(userId).orElse(null);
        if (category == null || user == null || price == null) return null;
        Product product = Product.builder()
                .title(title)
                .quantity(quantity != null ? quantity : 0)
                .description(description)
                .price(java.math.BigDecimal.valueOf(price))
                .category(category)
                .user(user)
                .build();
        return productRepository.save(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Product updateProduct(@Argument Long productId,
                                 @Argument String title,
                                 @Argument Integer quantity,
                                 @Argument String description,
                                 @Argument Double price,
                                 @Argument Long categoryId,
                                 @Argument Long userId) {
        Optional<Product> opt = productRepository.findById(productId);
        if (opt.isEmpty()) return null;
        Product product = opt.get();
        if (title != null) product.setTitle(title);
        if (quantity != null) product.setQuantity(quantity);
        if (description != null) product.setDescription(description);
        if (price != null) product.setPrice(java.math.BigDecimal.valueOf(price));
        if (categoryId != null) {
            Category category = categoryRepository.findById(categoryId).orElse(null);
            if (category != null) product.setCategory(category);
        }
        if (userId != null) {
            User user = userRepository.findById(userId).orElse(null);
            if (user != null) product.setUser(user);
        }
        return productRepository.save(product);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteProduct(@Argument Long productId) {
        if (!productRepository.existsById(productId)) return false;
        productRepository.deleteById(productId);
        return true;
    }
}
