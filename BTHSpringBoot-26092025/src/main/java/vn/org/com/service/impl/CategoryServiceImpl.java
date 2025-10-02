package vn.org.com.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import jakarta.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import lombok.RequiredArgsConstructor;
import vn.org.com.entity.Category;
import vn.org.com.models.CategoryModel;
import vn.org.com.repository.CategoryRepository;
import vn.org.com.service.CategoryService;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

	private final CategoryRepository repo;

	@Override
	public Category save(Category entity) {
		return repo.save(entity);
	}

	@Override
	public List<Category> findAll() {
		return repo.findAll();
	}


	@Override
	public Optional<Category> findById(Long id) {
		return repo.findById(id);
	}

	@Override
	public void deleteById(Long id) {
		repo.deleteById(id);
	}

	@Override
        public Page<Category> search(String name, Pageable pageable) {
                return (name == null || name.isBlank()) ? repo.findAll(pageable)
                                : repo.findByNameContainingIgnoreCase(name, pageable);
        }



        @Override

        public boolean nameExists(String name, Long excludeId) {
                return (excludeId == null) ? repo.existsByNameIgnoreCase(name)
                                : repo.existsByNameIgnoreCaseAndIdNot(name, excludeId);
        }

	@Override
    public Page<Category> findAll(Pageable pageable) {
        return repo.findAll(pageable);              // << triển khai
    }

	@Override
	public Object saveDto(@Valid CategoryModel dto) {
		// TODO Auto-generated method stub
		return null;
	}

}
