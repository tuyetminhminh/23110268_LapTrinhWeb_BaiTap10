package vn.org.com.security;

import jakarta.servlet.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class LoginSuccessHandler implements AuthenticationSuccessHandler {
	@Override
	public void onAuthenticationSuccess(HttpServletRequest req, HttpServletResponse res, Authentication auth)
			throws IOException {
		String target = "/user"; // mặc định
		for (GrantedAuthority ga : auth.getAuthorities()) {
			if ("ROLE_ADMIN".equals(ga.getAuthority())) {
				target = "/admin";
				break;
			}
		}
		res.sendRedirect(target);
	}
}
