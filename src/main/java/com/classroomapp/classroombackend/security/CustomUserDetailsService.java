package com.classroomapp.classroombackend.security;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user details for: {}", username);
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.error("User not found: {}", username);
                    return new UsernameNotFoundException("User not found with username: " + username);
                });
        
        Collection<? extends GrantedAuthority> authorities = getAuthorities(user);
        
        log.info("User found: {} with {} authorities", username, authorities.size());
        
        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                authorities);
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
        authorities.add(new SimpleGrantedAuthority("ROLE_" + roleName));
        
        // Add permissions based on role if needed
        // This can be extended to include specific permissions
        
        return authorities;
    }
}
