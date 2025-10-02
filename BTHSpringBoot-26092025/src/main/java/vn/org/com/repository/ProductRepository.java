package vn.org.com.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import vn.org.com.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // đảm bảo nạp user + category cùng query để tránh lazy problem
    @EntityGraph(attributePaths = {"user","category"})
    Page<Product> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"user","category"})
    Page<Product> findByTitleContainingIgnoreCase(String q, Pageable pageable);

    @EntityGraph(attributePaths = {"user","category"})
    Page<Product> findByCategory_Id(Long categoryId, Pageable pageable);

    @EntityGraph(attributePaths = {"user","category"})
    Page<Product> findByTitleContainingIgnoreCaseAndCategory_Id(
            String q, Long categoryId, Pageable pageable);

    List<Product> findByTitleContaining(String name);

    Page<Product> findByTitleContaining(String name, Pageable pageable);

    Optional<Product> findByTitle(String name);

    boolean existsByTitle(String title);
    
    List<Product> findTop5ByOrderByIdDesc();

    @EntityGraph(attributePaths = {"user","category"})
    @Query("""
                    SELECT p FROM Product p
                    WHERE (:q IS NULL OR LOWER(p.title) LIKE LOWER(CONCAT('%',:q,'%')))
                      AND (:catId IS NULL OR p.category.id = :catId)
                    """)
    Page<Product> search(@Param("q") String q, @Param("catId") Long catId, Pageable pageable);
}
