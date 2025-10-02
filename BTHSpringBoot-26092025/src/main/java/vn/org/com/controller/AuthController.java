package vn.org.com.controller;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import vn.org.com.entity.User;
import vn.org.com.entity.User.Role;
import vn.org.com.service.UserService;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // --- LOGIN PAGE (Security sẽ xử lý POST /login) ---
    @GetMapping("/login")
    public String loginPage() { return "login"; }

    // --- REGISTER ---
    @GetMapping("/register")
    public String registerPage() { return "register"; }

    @PostMapping("/register")
    public String doRegister(@RequestParam String fullname,
                             @RequestParam String email,
                             @RequestParam String password,
                             @RequestParam(required = false) String phone,
                             RedirectAttributes ra) {
        if (userService.existsByEmail(email)) {
            ra.addFlashAttribute("err", "Email đã tồn tại");
            return "redirect:/register";
        }
        User u = new User();
        u.setFullname(fullname);
        u.setEmail(email);
        u.setPassword(password); // sẽ được encode trong service
        u.setPhone(phone);
        if (u.getRole() == null) u.setRole(Role.USER);
        userService.save(u);

        // báo thành công qua query param để chắc chắn hiển thị ở login
        return "redirect:/login?registered=1";
    }

    // --- FORGOT PASSWORD ---
    @GetMapping("/forgot-password")
    public String forgotPage() { return "forgotpassword"; }

    @PostMapping("/forgot-password")
    public String doForgot(@RequestParam String email,
                           @RequestParam String newPassword,
                           @RequestParam String confirmPassword,
                           RedirectAttributes ra) {
        if (!newPassword.equals(confirmPassword)) {
            ra.addFlashAttribute("err", "Mật khẩu nhập lại không khớp");
            return "redirect:/forgot-password";
        }
        var opt = userService.findByEmail(email);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("err", "Email không tồn tại");
            return "redirect:/forgot-password";
        }
        User u = opt.get();
        u.setPassword(newPassword); // service sẽ encode
        userService.save(u);
        return "redirect:/login?reset=1";
    }

    // (tuỳ chọn) đăng xuất nếu không dùng logout của Security
    @PostMapping("/logout-session")
    public String logoutSession(HttpSession session) {
        session.invalidate();
        return "redirect:/login?logout";
    }
}
