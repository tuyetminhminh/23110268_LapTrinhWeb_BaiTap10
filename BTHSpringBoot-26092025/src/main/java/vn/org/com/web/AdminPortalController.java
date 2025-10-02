package vn.org.com.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPortalController {

    // GET /admin hoặc /admin/home => trả view "admin/home.html"
    @GetMapping({"", "/home"})
    public String home() {
        return "admin/home";
    }
}