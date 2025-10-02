package vn.org.com.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import jakarta.validation.Valid;
import vn.org.com.entity.Category;
import vn.org.com.models.CategoryModel;


public interface CategoryService {
	Category save(Category c);
    List<Category> findAll();
    Page<Category> findAll(Pageable pageable);  
    Optional<Category> findById(Long id);
    
    void deleteById(Long id);

    // search
    Page<Category> search(String name, Pageable pageable);
    
    boolean nameExists(String name, Long excludeId);
	Object saveDto(@Valid CategoryModel dto);
	long countAll();
	List<Category> findTop5ByMostProducts();
}
