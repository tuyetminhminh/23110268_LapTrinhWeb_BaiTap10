package vn.org.com.controller.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import vn.org.com.entity.Category;
import vn.org.com.entity.Product;
import vn.org.com.service.CategoryService;
import vn.org.com.service.ProductService;
import vn.org.com.service.UserService;
import vn.org.com.entity.User;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/products")
@RequiredArgsConstructor
public class AdminProductController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;

    @GetMapping
    public String list(@RequestParam(name = "q", defaultValue = "") String q,
                       @RequestParam(name = "categoryId", required = false) Long categoryId,
                       @RequestParam(name = "userId", required = false) Long userId,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       Model model) {
        populateListModel(q, categoryId, page, size, model);
        model.addAttribute("current", page);
        return "admin/products/list";
    }

    @GetMapping("/new")
    public String newForm(@RequestParam(name = "q", defaultValue = "") String q,
                          @RequestParam(name = "categoryId", required = false) Long categoryId,
                          @RequestParam(name = "userId", required = false) Long userId,
                          @RequestParam(name = "page", defaultValue = "1") int page,
                          @RequestParam(name = "size", defaultValue = "10") int size,
                          Model model) {
        Product product = new Product();
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("users", userService.findAll());
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/products/addOrEdit";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(name = "q", defaultValue = "") String q,
                           @RequestParam(name = "categoryId", required = false) Long categoryId,
                           @RequestParam(name = "userId", required = false) Long userId,
                           @RequestParam(name = "page", defaultValue = "1") int page,
                           @RequestParam(name = "size", defaultValue = "10") int size,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findById(id);
        if (productOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("message", "Không tìm thấy sản phẩm.");
            return buildRedirectUrl(q, categoryId, page, size);
        }
        model.addAttribute("product", productOpt.get());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("users", userService.findAll()); 
        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("page", page);
        model.addAttribute("size", size);
        return "admin/products/addOrEdit";
    }

    @PostMapping("/saveOrUpdate")
    public String save(@Valid @ModelAttribute("product") Product product,
                       BindingResult result,
                       @RequestParam(name = "categoryId", required = false) Long categoryId,
                       @RequestParam(name = "userId", required = false) Long userId,
                       @RequestParam(name = "q", defaultValue = "") String q,
                       @RequestParam(name = "categoryIdFilter", required = false) Long categoryIdFilter,
                       @RequestParam(name = "page", defaultValue = "1") int page,
                       @RequestParam(name = "size", defaultValue = "10") int size,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        boolean isNew = (product.getId() == null);

        // Đảm bảo tiêu đề không trùng khi tạo mới
        if (isNew && productService.existsByTitle(product.getTitle())) {
            result.rejectValue("title", "product.title.duplicate", "Tên sản phẩm đã tồn tại");
        }

        if (categoryId == null) {
            result.rejectValue("category", "product.category.required", "Vui lòng chọn thể loại");
        }
        
        if (userId == null) {
            result.rejectValue("user", "product.user.required", "Vui lòng chọn người tạo");
        }
        
        if (result.hasErrors()) {
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("users", userService.findAll());
            model.addAttribute("q", q);
            model.addAttribute("categoryId", categoryIdFilter);
            model.addAttribute("page", page);
            model.addAttribute("size", size);
            return "admin/products/addOrEdit";
        }

        Category category = categoryService.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thể loại"));
        product.setCategory(category);
        // set user
        if (userId != null) {
            User user = userService.findById(userId).orElseThrow(() -> new IllegalArgumentException("Không tìm thấy user"));
            product.setUser(user);
        } else {
            product.setUser(null);
        }
        productService.save(product);

        redirectAttributes.addFlashAttribute("message", isNew ? "Thêm sản phẩm thành công" : "Cập nhật sản phẩm thành công");
        return buildRedirectUrl(q, categoryIdFilter, page, size);
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id,
                         @RequestParam(name = "q", defaultValue = "") String q,
                         @RequestParam(name = "categoryId", required = false) Long categoryId,
                         @RequestParam(name = "userId", required = false) Long userId,
                         @RequestParam(name = "page", defaultValue = "1") int page,
                         @RequestParam(name = "size", defaultValue = "10") int size,
                         RedirectAttributes redirectAttributes) {
        productService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa sản phẩm");
        return buildRedirectUrl(q, categoryId, page, size);
    }

    private void populateListModel(String q, Long categoryId, int page, int size, Model model) {
        int pageIndex = Math.max(0, page - 1);
        int pageSize = Math.max(1, size);
        Pageable pageable = PageRequest.of(pageIndex, pageSize, Sort.by(Sort.Direction.DESC, "id"));
        Page<Product> pageData = productService.search(q, categoryId, pageable);
        model.addAttribute("pageData", pageData);
        model.addAttribute("productPage", pageData); // để bảng dùng được

        model.addAttribute("q", q);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("size", pageSize);
        List<Integer> pageNumbers = (pageData.getTotalPages() > 0)
                ? IntStream.rangeClosed(1, pageData.getTotalPages()).boxed().toList()
                : List.of();
        model.addAttribute("pageNumbers", pageNumbers);
        model.addAttribute("categories", categoryService.findAll());
    }

    private String buildRedirectUrl(String q, Long categoryId, int page, int size) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromPath("/admin/products")
                .queryParam("page", page)
                .queryParam("size", size)
                .queryParam("q", q);
        if (categoryId != null) {
            builder.queryParam("categoryId", categoryId);
        }
        return "redirect:" + builder.build().encode().toUriString();
    }
}
