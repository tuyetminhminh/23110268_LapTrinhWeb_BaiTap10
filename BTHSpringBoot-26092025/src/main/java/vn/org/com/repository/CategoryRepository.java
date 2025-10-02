package vn.org.com.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import vn.org.com.entity.Category;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

        boolean existsByNameIgnoreCase(String name);

        boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);

        Page<Category> findByNameContainingIgnoreCase(String name, Pageable pageable);

        List<Category> findByNameContainingIgnoreCase(String name);

        List<Category> findByNameContaining(String name);

        Page<Category> findByNameContaining(String name, Pageable pageable);

        Optional<Category> findByName(String name);
        
     // fallback: mới nhất
        List<Category> findTop5ByOrderByIdDesc();

        // TOP category theo số sản phẩm (ưu tiên)
        @Query("""
               select c
               from Category c
               left join c.products p
               group by c
               order by count(p) desc, c.id desc
               """)
        List<Category> findTopMostProducts(org.springframework.data.domain.Pageable pageable);
}
