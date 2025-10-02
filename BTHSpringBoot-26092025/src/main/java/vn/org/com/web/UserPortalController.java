package vn.org.com.web;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER','ADMIN')")
public class UserPortalController {

    // GET /user hoặc /user/home => trả view "user/home.html"
    @GetMapping({"", "/home"})
    public String home() {
        return "user/home";
    }
}