package vn.org.com.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.org.com.entity.Product;

import java.util.Optional;

public interface ProductService {
	
	Page<Product> search(String q, Long categoryId, Pageable pageable);
	Page<Product> search(String q, Pageable pageable);
	Optional<Product> findById(Long id);

	Product save(Product p);

	void deleteById(Long id);

	Page<Product> findAll(String q, Long categoryId, Pageable pageable);
	
        boolean existsByTitle(String title);
	Page<Product> search(String q, Long categoryId, int page, int size);
}
