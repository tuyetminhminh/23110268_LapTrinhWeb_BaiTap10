package vn.org.com.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import vn.org.com.entity.Category;
import vn.org.com.models.CategoryModel;
import vn.org.com.service.CategoryService;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryApi {
    private final CategoryService categoryService;

    @GetMapping
    public Page<Category> list(@RequestParam(defaultValue="") String q,
                               @RequestParam(defaultValue="0") int page,
                               @RequestParam(defaultValue="10") int size){
        return categoryService.search(q, PageRequest.of(page, size, Sort.by("id").descending()));
    }

    @GetMapping("/{id}")
    public Category detail(@PathVariable Long id){
        return categoryService.findById(id).orElseThrow();
    }

    @PostMapping
    public Category create(@Valid @RequestBody Category c){
        c.setId(null);
        return categoryService.save(c);
    }

    @PutMapping("/{id}")
    public Category update(@PathVariable Long id, @Valid @RequestBody Category c){
        Category old = categoryService.findById(id).orElseThrow();
        old.setName(c.getName());
        old.setImages(c.getImages());
        return categoryService.save(old);
    }

    @DeleteMapping("/{id}")
    public Map<String,String> delete(@PathVariable Long id){
        categoryService.deleteById(id);
        return Map.of("message","deleted");
    }
    @PostMapping("/api/admin/categories")
    public ResponseEntity<?> create(@Valid @RequestBody CategoryModel dto, BindingResult result) {
      if (result.hasErrors()) {
        Map<String, String> errors = result.getFieldErrors().stream()
          .collect(Collectors.toMap(FieldError::getField, DefaultMessageSourceResolvable::getDefaultMessage));
        return ResponseEntity.badRequest().body(errors);
      }
      var saved = categoryService.saveDto(dto);
      return ResponseEntity.ok(saved);
    }
}
