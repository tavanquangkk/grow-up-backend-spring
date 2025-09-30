package jp.trial.grow_up.config;

import jp.trial.grow_up.domain.User;
import jp.trial.grow_up.repository.client.UserRepository;
import jp.trial.grow_up.util.UserRole;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {
    @Bean
    public CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder encoder) {
        return args -> {
            if (!userRepository.existsByEmail("admin@gmail.com")) {
                User admin = new User();
                admin.setName("Admin");
                admin.setEmail("admin@gmail.com");
                admin.setPassword(encoder.encode("admin123")); // ← 本番はもっと強力なパスワードに！
                admin.setRole(UserRole.ADMIN);
                userRepository.save(admin);
                System.out.println("✅ Admin user created: admin@example.com / admin123");
            }
        };
    }
}
