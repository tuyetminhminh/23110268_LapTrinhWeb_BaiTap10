package vn.org.com.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import vn.org.com.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

	Page<User> findByFullnameContainingIgnoreCaseOrEmailContainingIgnoreCase(String fullname, String email,
			Pageable pageable);

	Optional<User> findByEmail(String email);

	boolean existsByEmail(String email);

	boolean existsByEmailIgnoreCase(String email);

	boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}
