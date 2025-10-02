package vn.org.com.service.impl;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import vn.org.com.entity.Category;
import vn.org.com.entity.Product;
import vn.org.com.repository.ProductRepository;
import vn.org.com.service.ProductService;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

        private final ProductRepository repo;
	
	@Override
    public Page<Product> findAll(String q, Long categoryId, Pageable pageable) {
        String keyword = (q == null) ? "" : q.trim();
        if (categoryId != null) {
            return repo.findByTitleContainingIgnoreCaseAndCategory_Id(keyword, categoryId, pageable);
        }
        return keyword.isEmpty() ? repo.findAll(pageable)
                                 : repo.findByTitleContainingIgnoreCase(keyword, pageable);
    }

	@Override
	public Optional<Product> findById(Long id) {
		return repo.findById(id);
	}

	@Override
	public Product save(Product p) {
		return repo.save(p);
	}

	@Override
	public void deleteById(Long id) {
		repo.deleteById(id);
	}

	@Override
    public Page<Product> search(String q, Pageable pageable) {
        String keyword = (q == null) ? "" : q.trim();
        if (keyword.isEmpty()) return repo.findAll(pageable);
        return repo.findByTitleContainingIgnoreCase(keyword, pageable);
    }

        @Override

        public boolean existsByTitle(String title) {
                return repo.existsByTitle(title);
        }

	@Override
	public Page<Product> search(String q, Long categoryId, Pageable pageable) {
	    Product probe = new Product();

            if (q != null && !q.trim().isEmpty()) {
                probe.setTitle(q.trim());
            }
            if (categoryId != null) {
                Category c = new Category();
                c.setId(categoryId);
                probe.setCategory(c);
            }

	    ExampleMatcher matcher = ExampleMatcher.matchingAll()
	            .withIgnoreCase()
	            .withStringMatcher(ExampleMatcher.StringMatcher.CONTAINING);

	    Example<Product> example = Example.of(probe, matcher);
	    return repo.findAll(example, pageable); // OK với QueryByExampleExecutor
	}

	@Override
	public Page<Product> search(String q, Long categoryId, int page, int size) {
		int p = Math.max(0, page - 1);
        int s = Math.max(1, size);
        Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));
        return search(q, categoryId, pageable);
        }

}
