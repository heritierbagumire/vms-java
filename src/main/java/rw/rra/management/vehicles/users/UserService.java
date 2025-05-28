package rw.rra.management.vehicles.users;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import rw.rra.management.vehicles.auth.dtos.RegisterRequestDto;
import rw.rra.management.vehicles.commons.exceptions.BadRequestException;
import rw.rra.management.vehicles.users.dtos.UserResponseDto;
import rw.rra.management.vehicles.users.mappers.UserMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@AllArgsConstructor

public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;

    @Value("${admin.email:heritierbagumire@gmail.com}")
    private String adminEmail;

    @Value("${admin.password:password@2025}")
    private String adminPassword;

    public UserResponseDto createUser(RegisterRequestDto user) {
        if(userRepository.existsByEmailOrPhoneNumberOrNationalId(user.email(), user.phoneNumber(), user.nationalId()))
            throw new BadRequestException("User with this email or nationalId or phone number already exists.");

        var newUser = userMapper.toEntity(user);
        newUser.setPassword(passwordEncoder.encode(user.password()));
        newUser.setRole(Role.ROLE_STANDARD);
        newUser.setEnabled(false);
        log.info("user is here, {}", newUser);
        userRepository.save(newUser);
        return userMapper.toResponseDto(newUser);
    }

    public void createAdminUserIfNotExists() {
        // Check if the admin user already exists
        if (!userRepository.findByEmail(adminEmail).isPresent()) {
            User adminUser = new User();
            adminUser.setId(UUID.randomUUID());
            adminUser.setFirstName("Heritier");
            adminUser.setLastName("Heritier");
            adminUser.setEnabled(true);
            adminUser.setStatus(Status.ACTIVE);
            adminUser.setPhoneNumber("0798978831");
            adminUser.setNationalId("1200670162551080");
            adminUser.setEmail(adminEmail);
            // Use the password from environment variables, encoded
            adminUser.setPassword(passwordEncoder.encode(adminPassword));
            adminUser.setRole(Role.ROLE_ADMIN);

            userRepository.save(adminUser);
            log.info("Admin user 'Heritier' created.");
        } else {
            log.info("Admin user 'Heritier' already exists.");
        }
    }

    public void changeUserPassword(String userEmail, String newPassword) {
        var user = findByEmail(userEmail);
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public void activateUserAccount(String userEmail) {
        var user = findByEmail(userEmail);
        user.setEnabled(true);
        userRepository.save(user);
    }

    public void updateUserStatus(String email, Status newStatus) {
        var user = findByEmail(email);
        user.setStatus(newStatus);
        userRepository.save(user);
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new BadRequestException("User with that email not found."));
    }
}
