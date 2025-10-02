package vn.org.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

	// src/main/java/vn/org/com/controllers/HomeController.java

	@GetMapping({ "/", "/home" })
	public String home() {
		return "home";
	}
//
//	@GetMapping("/user")
//	public String userHome() {
//		return "user/home";
//	}

	@GetMapping("/admin")
	public String adminHome() {
		return "admin/home";
	}
}
