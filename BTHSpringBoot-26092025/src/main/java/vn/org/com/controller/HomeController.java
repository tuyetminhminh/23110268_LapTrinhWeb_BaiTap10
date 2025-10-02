// src/main/java/vn/org/com/controller/HomeController.java
package vn.org.com.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import vn.org.com.service.CategoryService;
import vn.org.com.service.ProductService;
import vn.org.com.service.UserService;

import java.util.Set;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class HomeController {

  private final UserService userService;
  private final ProductService productService;
  private final CategoryService categoryService;

  @GetMapping({"/", "/home"})
  public String home(Authentication auth, Model model) {
    long totalUsers = userService.countAll();
    long totalProducts = productService.countAll();
    long totalCategories = categoryService.countAll();

    model.addAttribute("totalUsers", totalUsers);
    model.addAttribute("totalProducts", totalProducts);
    model.addAttribute("totalCategories", totalCategories);
    model.addAttribute("recentProducts", productService.findTop5ByOrderByIdDesc());
    model.addAttribute("hotCategories", categoryService.findTop5ByMostProducts());

    if (auth == null) {
      // khách chưa đăng nhập → có thể đưa về user home dạng giới thiệu, hoặc login
      return "user/home";
    }
    Set<String> roles = auth.getAuthorities().stream()
        .map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
    if (roles.contains("ROLE_ADMIN")) return "admin/home";
    return "user/home";
  }
}
