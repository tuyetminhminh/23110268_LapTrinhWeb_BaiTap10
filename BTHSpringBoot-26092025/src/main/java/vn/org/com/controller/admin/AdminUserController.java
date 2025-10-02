package vn.org.com.controller.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.security.crypto.password.PasswordEncoder;

import vn.org.com.entity.Category;
import vn.org.com.entity.User;
import vn.org.com.repository.UserRepository;
import vn.org.com.service.CategoryService;

import java.util.List;
import java.util.stream.IntStream;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

	private final UserRepository userRepo;
	private final CategoryService categoryService;
	private final PasswordEncoder passwordEncoder;

	// LIST + SEARCH + PAGINATION
	@GetMapping
	public String list(@RequestParam(name = "q", defaultValue = "") String q,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, Model model) {
		int p = Math.max(0, page - 1);
		int s = Math.max(1, size);
		Pageable pageable = PageRequest.of(p, s, Sort.by(Sort.Direction.DESC, "id"));

		Page<User> userPage = (q == null || q.isBlank()) ? userRepo.findAll(pageable)
				: userRepo.findByFullnameContainingIgnoreCaseOrEmailContainingIgnoreCase(q.trim(), q.trim(), pageable);

		model.addAttribute("userPage", userPage);
		model.addAttribute("q", q == null ? "" : q.trim());
		model.addAttribute("size", s);
		model.addAttribute("current", page);
		List<Integer> pageNumbers = (userPage.getTotalPages() > 0)
				? IntStream.rangeClosed(1, userPage.getTotalPages()).boxed().toList()
				: List.of();
		model.addAttribute("pageNumbers", pageNumbers);
		return "admin/users/list";
	}

	// CREATE FORM
	@GetMapping("/add")
	public String addForm(@RequestParam(name = "q", defaultValue = "") String q,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, Model model) {
		model.addAttribute("user", new User());
		model.addAttribute("categories", categoryService.findAll());
		 model.addAttribute("selectedCategoryIds", java.util.Collections.emptySet());
		model.addAttribute("q", q);
		model.addAttribute("page", page);
		model.addAttribute("size", size);
		return "admin/users/addOrEdit";
	}

	// EDIT FORM
	@GetMapping("/edit/{id}")
	public String editForm(@PathVariable Long id,
	                       @RequestParam(name = "q", defaultValue = "") String q,
	                       @RequestParam(name = "page", defaultValue = "1") int page,
	                       @RequestParam(name = "size", defaultValue = "10") int size,
	                       Model model, RedirectAttributes ra) {
	    User u = userRepo.findById(id).orElse(null);
	    if (u == null) {
	        ra.addFlashAttribute("message", "Không tìm thấy người dùng.");
	        return buildRedirectUrl(q, page, size);
	    }

	    // đổ DTO để hiện placeholder cho password
	    User dto = new User();
	    dto.setId(u.getId());
	    dto.setFullname(u.getFullname());
	    dto.setEmail(u.getEmail());
	    dto.setPhoneVN(u.getPhoneVN());
//	    dto.setPassword(PASSWORD_PLACEHOLDER);
	    model.addAttribute("user", dto);

	    model.addAttribute("categories", categoryService.findAll());
	    // tập id category được chọn — dùng cho template
	    java.util.Set<Long> selected = (u.getCategories() == null) ? java.util.Collections.emptySet()
	            : u.getCategories().stream().map(Category::getId).collect(java.util.stream.Collectors.toSet());
	    model.addAttribute("selectedCategoryIds", selected);

	    model.addAttribute("q", q);
	    model.addAttribute("page", page);
	    model.addAttribute("size", size);
	    return "admin/users/addOrEdit";
	}
	// SAVE OR UPDATE
	@PostMapping("/saveOrUpdate")
	public String save(@ModelAttribute("user") User form,
	                   BindingResult result,
	                   @RequestParam(name="categoryIds", required=false) List<Long> categoryIds,
	                   @RequestParam(name="q", defaultValue="") String q,
	                   @RequestParam(name="page", defaultValue="1") int page,
	                   @RequestParam(name="size", defaultValue="10") int size,
	                   Model model,
	                   RedirectAttributes ra) {

	    // 1) Email duy nhất
	    boolean emailTaken = (form.getId() == null)
	            ? userRepo.existsByEmailIgnoreCase(form.getEmail())
	            : userRepo.existsByEmailIgnoreCaseAndIdNot(form.getEmail(), form.getId());
	    if (emailTaken) {
	        result.rejectValue("email", "user.email.duplicate", "Email đã tồn tại");
	    }

	    // 2) Mật khẩu: chỉ bắt buộc khi TẠO MỚI
	    if (form.getId() == null && (form.getPassword() == null || form.getPassword().isBlank())) {
	        result.rejectValue("password", "user.password.required", "Vui lòng nhập mật khẩu");
	    }

	    // Nếu có lỗi => trả form + giữ danh mục đã chọn
	    if (result.hasErrors()) {
	        model.addAttribute("categories", categoryService.findAll());
	        java.util.Set<Long> selected =
	                (categoryIds == null) ? java.util.Collections.emptySet() : new java.util.HashSet<>(categoryIds);
	        model.addAttribute("selectedCategoryIds", selected);
	        model.addAttribute("q", q);
	        model.addAttribute("page", page);
	        model.addAttribute("size", size);
	        return "admin/users/addOrEdit";
	    }

	    // 3) Lưu
	    User entity;
	    if (form.getId() == null) {
	        // CREATE
	        entity = new User();
	        entity.setFullname(form.getFullname());
	        entity.setEmail(form.getEmail());
	        entity.setPhoneVN(form.getPhoneVN());
	        entity.setPassword(passwordEncoder.encode(form.getPassword())); // đã đảm bảo không rỗng ở trên
	    } else {
	        // UPDATE
	        entity = userRepo.findById(form.getId())
	                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng"));
	        entity.setFullname(form.getFullname());
	        entity.setEmail(form.getEmail());
	        entity.setPhoneVN(form.getPhoneVN());

	        // Nếu người dùng nhập password mới -> mã hoá & cập nhật
	        String pw = form.getPassword();
	        if (pw != null && !pw.isBlank()) {
	            entity.setPassword(passwordEncoder.encode(pw));
	        }
	        // Nếu để trống -> giữ nguyên password cũ (KHÔNG đụng gì)
	    }

	    // 4) Gán categories theo các id được chọn
	    if (categoryIds != null && !categoryIds.isEmpty()) {
	        // Nếu bạn có method findAllById trong service thì dùng:
	        // Set<Category> cats = new HashSet<>(categoryService.findAllById(categoryIds));
	        // hoặc tạm thời lọc từ findAll():
	        java.util.Set<Category> cats = categoryService.findAll().stream()
	                .filter(c -> categoryIds.contains(c.getId()))
	                .collect(java.util.stream.Collectors.toSet());
	        entity.setCategories(cats);
	    } else {
	        entity.setCategories(java.util.Collections.emptySet());
	    }

	    userRepo.save(entity);
	    ra.addFlashAttribute("message",
	            form.getId() == null ? "Thêm người dùng thành công" : "Cập nhật người dùng thành công");

	    return buildRedirectUrl(q, page, size);
	}



	// DELETE (POST)
	@PostMapping("/{id}/delete")
	public String delete(@PathVariable Long id, @RequestParam(name = "q", defaultValue = "") String q,
			@RequestParam(name = "page", defaultValue = "1") int page,
			@RequestParam(name = "size", defaultValue = "10") int size, RedirectAttributes ra) {
		userRepo.deleteById(id);
		ra.addFlashAttribute("message", "Đã xóa người dùng");
		return buildRedirectUrl(q, page, size);
	}

	private String buildRedirectUrl(String q, int page, int size) {
		return "redirect:" + UriComponentsBuilder.fromPath("/admin/users").queryParam("page", page)
				.queryParam("size", size).queryParam("q", q == null ? "" : q.trim()).build().encode().toUriString();
	}
}
