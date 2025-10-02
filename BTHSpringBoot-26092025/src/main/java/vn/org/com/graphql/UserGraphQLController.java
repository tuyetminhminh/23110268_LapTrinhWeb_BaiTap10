package vn.org.com.graphql;

import lombok.RequiredArgsConstructor;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import vn.org.com.entity.User;
import vn.org.com.entity.Category;
import vn.org.com.repository.UserRepository;
import vn.org.com.repository.CategoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class UserGraphQLController {
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @QueryMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User getUser(@Argument Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(@Argument String fullname,
                           @Argument String email,
                           @Argument String password,
                           @Argument String phone,
                           @Argument List<Long> categoryIds) {
        User user = User.builder()
                .fullname(fullname)
                .email(email)
                .password(password)
                .phoneVN(phone)
                .build();
        if (categoryIds != null && !categoryIds.isEmpty()) {
            Set<Category> categories = categoryRepository.findAllById(categoryIds)
                    .stream().collect(Collectors.toSet());
            user.setCategories(categories);
        }
        return userRepository.save(user);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public User updateUser(@Argument Long id,
                           @Argument String fullname,
                           @Argument String email,
                           @Argument String password,
                           @Argument String phone,
                           @Argument List<Long> categoryIds) {
        Optional<User> opt = userRepository.findById(id);
        if (opt.isEmpty()) return null;
        User user = opt.get();
        if (fullname != null) user.setFullname(fullname);
        if (email != null) user.setEmail(email);
        if (password != null) user.setPassword(password);
        if (phone != null) user.setPhoneVN(phone);
        if (categoryIds != null) {
            Set<Category> categories = categoryRepository.findAllById(categoryIds)
                    .stream().collect(Collectors.toSet());
            user.setCategories(categories);
        }
        return userRepository.save(user);
    }

    @MutationMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Boolean deleteUser(@Argument Long id) {
        if (!userRepository.existsById(id)) return false;
        userRepository.deleteById(id);
        return true;
    }
}
