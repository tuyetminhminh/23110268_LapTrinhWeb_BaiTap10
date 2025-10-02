package vn.org.com.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CustomAuthFailureHandler implements AuthenticationFailureHandler {
  @Override
  public void onAuthenticationFailure(HttpServletRequest req, HttpServletResponse res,
                                      AuthenticationException ex) throws IOException, ServletException {
    String key = "error";
    if (ex instanceof UsernameNotFoundException) {
      key = "usernotfound";
    } else if (ex instanceof BadCredentialsException) {
      key = "badcreds";
    }
    String msg = switch (key) {
      case "usernotfound" -> "Tài khoản không tồn tại.";
      case "badcreds"     -> "Sai mật khẩu.";
      default             -> "Đăng nhập thất bại.";
    };
    String url = "/login?"+key+"=1&msg=" + URLEncoder.encode(msg, StandardCharsets.UTF_8);
    res.sendRedirect(url);
  }
}
