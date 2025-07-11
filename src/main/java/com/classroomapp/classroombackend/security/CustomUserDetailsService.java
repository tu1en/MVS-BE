package com.classroomapp.classroombackend.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;

import lombok.extern.slf4j.Slf4j;

/**
 * UserDetailsService implementation for Spring Security
 */
@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Autowired
    public CustomUserDetailsService(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.info("CustomUserDetailsService - Attempting to load user by: {}", usernameOrEmail);

        // Regex to check if the input is an email address
        final Pattern emailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

        Optional<User> userOptional;

        if (emailPattern.matcher(usernameOrEmail).matches()) {
            log.info("CustomUserDetailsService - Input is an email. Searching by email: {}", usernameOrEmail);
            userOptional = userRepository.findByEmail(usernameOrEmail);
        } else {
            log.info("CustomUserDetailsService - Input is a username. Searching by username: {}", usernameOrEmail);
            userOptional = userRepository.findByUsername(usernameOrEmail);
        }

        User user = userOptional.orElseThrow(() -> {
            log.error("CustomUserDetailsService - User not found with identifier: {}", usernameOrEmail);
            return new UsernameNotFoundException("User not found with identifier: " + usernameOrEmail);
        });

        log.info("CustomUserDetailsService - Found user: ID={}, Username={}, Email={}, RoleId={}",
                user.getId(), user.getUsername(), user.getEmail(), user.getRoleId());

        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);

        log.info("CustomUserDetailsService - Successfully loaded user '{}' with authorities: {}",
                user.getUsername(), authorities);

        // Return our custom UserDetails object that holds the full User entity
        return new CustomUserDetails(user, authorities);
    }
    
    /**
     * Get user authorities based on role
     *
     * @param user user entity
     * @return collection of authorities
     */
    private Collection<? extends GrantedAuthority> getAuthorities(User user) {
        String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

        // Add ROLE_X authority
        String authority = "ROLE_" + roleName;
        authorities.add(new SimpleGrantedAuthority(authority));

        log.info("CustomUserDetailsService - Generated authorities for user {}: [{}]",
                user.getUsername(), authority);

        // Add permissions based on role if needed
        // This can be extended to include specific permissions

        return authorities;
    }
}
