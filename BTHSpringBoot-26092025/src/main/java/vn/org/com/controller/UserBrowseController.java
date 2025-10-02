package vn.org.com.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UserBrowseController {

  @GetMapping("/user")
  public String userHome() { return "user/home"; }

  @GetMapping("/user/categories")
  public String userCategories() { return "user/categories/list"; }

  @GetMapping("/user/products")
  public String userProducts() { return "user/products/list"; }
}
