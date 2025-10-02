package vn.org.com.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CustomAuthFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(HttpServletRequest req,
                                      HttpServletResponse res,
                                      AuthenticationException ex)
      throws IOException, ServletException {

    // Lấy lại email người dùng nhập để điền lại form
    String email = req.getParameter("email");
    String emailParam = email == null ? "" : URLEncoder.encode(email, StandardCharsets.UTF_8);

    // Xác định loại lỗi & thông điệp
    String key;
    String msg;
    if (ex instanceof UsernameNotFoundException) {
      key = "usernotfound";
      msg = "Tài khoản không tồn tại.";
    } else if (ex instanceof BadCredentialsException) {
      key = "badcreds";
      msg = "Sai mật khẩu.";
    } else {
      key = "error";
      msg = "Đăng nhập thất bại.";
    }

    String msgParam = URLEncoder.encode(msg, StandardCharsets.UTF_8);

    // Redirect về /login kèm email & thông điệp
    String url = "/login?" + key + "=1&msg=" + msgParam + "&email=" + emailParam;
    res.sendRedirect(url);
  }
}
