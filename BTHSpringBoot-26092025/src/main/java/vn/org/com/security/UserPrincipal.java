package vn.org.com.security;

import org.springframework.security.core.*;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import vn.org.com.entity.User;

import java.util.Collection;
import java.util.List;

public class UserPrincipal implements UserDetails {
  /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
  private final User user;
  public UserPrincipal(User user) { this.user = user; }

  @Override public Collection<? extends GrantedAuthority> getAuthorities() {
    // ROLE_ prefix: Spring chuẩn
    return List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));
  }
  @Override public String getPassword() { return user.getPassword(); }
  @Override public String getUsername() { return user.getEmail(); }  // login bằng email
  @Override public boolean isAccountNonExpired() { return true; }
  @Override public boolean isAccountNonLocked() { return true; }
  @Override public boolean isCredentialsNonExpired() { return true; }
  @Override public boolean isEnabled() { return true; }

  public User getDomainUser(){ return user; }
}
