package vn.org.com.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.web.bind.annotation.*;
import vn.org.com.entity.Category;
import vn.org.com.entity.Product;
import vn.org.com.entity.User;
import vn.org.com.repository.UserRepository;
import vn.org.com.service.CategoryService;
import vn.org.com.service.ProductService;

import java.util.Map;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductApi {
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository; 

    @GetMapping
    public Page<Product> list(@RequestParam(defaultValue="") String q,
                              @RequestParam(required=false) Long categoryId,
                              @RequestParam(defaultValue="0") int page,
                              @RequestParam(defaultValue="10") int size){
        return productService.search(q, categoryId, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @GetMapping("/{id}")
    public Product detail(@PathVariable Long id){
        return productService.findById(id).orElseThrow();
    }

    @PostMapping
    public Product create(@Valid @RequestBody Product p){
    	if (p.getCategory() != null && p.getCategory().getId() != null){
            Category c = categoryService.findById(p.getCategory().getId()).orElseThrow();
            p.setCategory(c);
        }
        if (p.getUser() != null && p.getUser().getId() != null) {
            User u = userRepository.findById(p.getUser().getId()).orElseThrow();
            p.setUser(u);
        }

        p.setId(null);
        return productService.save(p);
    }

    @PutMapping("/{id}")
    public Product update(@PathVariable Long id, @Valid @RequestBody Product p){
    	Product old = productService.findById(id).orElseThrow();
        old.setTitle(p.getTitle());
        old.setQuantity(p.getQuantity());
        old.setDescription(p.getDescription());
        old.setPrice(p.getPrice());

        if (p.getCategory() != null && p.getCategory().getId() != null){
            Category c = categoryService.findById(p.getCategory().getId()).orElseThrow();
            old.setCategory(c);
        } else {
            old.setCategory(null);
        }

        if (p.getUser() != null && p.getUser().getId() != null) {
            User u = userRepository.findById(p.getUser().getId()).orElseThrow();
            old.setUser(u);
        } else {
            old.setUser(null);
        }

        return productService.save(old);
    }

    @DeleteMapping("/{id}")
    public Map<String,String> delete(@PathVariable Long id){
        productService.deleteById(id);
        return Map.of("message","deleted");
    }
}
