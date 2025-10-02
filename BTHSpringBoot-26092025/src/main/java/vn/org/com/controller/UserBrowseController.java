// src/main/java/vn/org/com/controller/UserBrowseController.java
package vn.org.com.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import vn.org.com.entity.Category;
import vn.org.com.entity.Product;
import vn.org.com.service.CategoryService;
import vn.org.com.service.ProductService;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class UserBrowseController {

  private final CategoryService categoryService;
  private final ProductService productService;

  // ----- CATEGORIES (VIEW ONLY) -----
  @GetMapping("/categories")
  public String categories(@RequestParam(defaultValue = "1") int page,
                           @RequestParam(defaultValue = "10") int size,
                           @RequestParam(required = false) String kw,
                           Model model) {
    int pageIndex = Math.max(page - 1, 0);
    Page<Category> categoryPage = categoryService.search(kw, PageRequest.of(pageIndex, size, Sort.by("id").descending()));

    List<Integer> pageNumbers = IntStream.rangeClosed(1, categoryPage.getTotalPages())
        .boxed().toList();

    model.addAttribute("kw", kw);
    model.addAttribute("size", size);
    model.addAttribute("current", page);
    model.addAttribute("categoryPage", categoryPage);
    model.addAttribute("pageNumbers", pageNumbers);

    return "user/categories/list";
  }

  // ----- PRODUCTS (VIEW ONLY) -----
  @GetMapping("/products")
  public String products(@RequestParam(defaultValue = "1") int page,
                         @RequestParam(defaultValue = "10") int size,
                         @RequestParam(required = false) String q,
                         @RequestParam(required = false) Long categoryId,
                         Model model) {
    int pageIndex = Math.max(page - 1, 0);
    Page<Product> pageData = productService.search(q, categoryId,
        PageRequest.of(pageIndex, size, Sort.by("id").descending()));

    List<Integer> pageNumbers = IntStream.rangeClosed(1, pageData.getTotalPages())
        .boxed().toList();

    model.addAttribute("q", q);
    model.addAttribute("categoryId", categoryId);
    model.addAttribute("size", size);
    model.addAttribute("current", page);
    model.addAttribute("pageData", pageData);
    model.addAttribute("pageNumbers", pageNumbers);
    model.addAttribute("categories", categoryService.findAll()); // để fill dropdown

    return "user/products/list";
  }
}
