package vn.org.com.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import vn.org.com.entity.User;
import vn.org.com.repository.UserRepository;
import vn.org.com.service.UserService;

import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder; // <-- TIÊM Encoder

    @Override
    public List<User> findAll() { return userRepository.findAll(); }

    @Override
    public Page<User> findAll(Pageable pageable) { return userRepository.findAll(pageable); }

    @Override
    public Optional<User> findById(Long id) { return userRepository.findById(id); }

    @Override
    public User save(User u) {
        // mã hoá nếu là plain text
        if (u.getPassword() != null && !u.getPassword().startsWith("$2")) {
            u.setPassword(passwordEncoder.encode(u.getPassword()));
        }
        return userRepository.save(u);
    }

    @Override
    public void deleteById(Long id) { userRepository.deleteById(id); }

    @Override
    public boolean existsByEmail(String email) { return userRepository.existsByEmail(email); }

    @Override
    public User login(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .filter(u -> passwordEncoder.matches(rawPassword, u.getPassword()))
                .orElse(null);
    }

    // ====== Forgot password ======
    @Override
    public boolean resetToTemporaryPassword(String email) {
        Optional<User> ou = userRepository.findByEmail(email);
        if (ou.isEmpty()) return false;
        User u = ou.get();
        String temp = generateTempPassword(10);
        u.setPassword(passwordEncoder.encode(temp));
        userRepository.save(u);
        // TODO: nếu bạn có lớp mail, gửi email chứa "temp" cho user tại đây.
        // sendMail.send(u.getEmail(), "Mật khẩu tạm thời", "Mật khẩu mới: " + temp);
        System.out.println("TEMP PASSWORD for " + email + ": " + temp);
        return true;
    }

    private String generateTempPassword(int len) {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789";
        SecureRandom r = new SecureRandom();
        StringBuilder sb = new StringBuilder(len);
        for (int i=0;i<len;i++) sb.append(alphabet.charAt(r.nextInt(alphabet.length())));
        return sb.toString();
    }
    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

	@Override
	public long countAll() {
		// TODO Auto-generated method stub
		return userRepository.count();
	}

}
