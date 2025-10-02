package vn.org.com.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import vn.org.com.entity.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    List<User> findAll();
    Page<User> findAll(Pageable pageable);
    Optional<User> findById(Long id);
    User save(User u);
    void deleteById(Long id);
    boolean existsByEmail(String email);
    User login(String email, String password);

    // forgot password
    Optional<User> findByEmail(String email); 
    boolean resetToTemporaryPassword(String email);
}
