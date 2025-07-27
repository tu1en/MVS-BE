package com.classroomapp.classroombackend.auth.service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.classroomapp.classroombackend.dto.GoogleAuthRequestDto;
import com.classroomapp.classroombackend.dto.LoginRequestDto;
import com.classroomapp.classroombackend.dto.LoginResponseDto;
import com.classroomapp.classroombackend.dto.PasswordConfirmationDto;
import com.classroomapp.classroombackend.dto.PasswordResetRequestDto;
import com.classroomapp.classroombackend.dto.RegisterDto;
import com.classroomapp.classroombackend.dto.UserDto;
import com.classroomapp.classroombackend.exception.ResourceNotFoundException;
import com.classroomapp.classroombackend.model.usermanagement.User;
import com.classroomapp.classroombackend.repository.usermanagement.UserRepository;
import com.classroomapp.classroombackend.security.JwtUtil;
import com.classroomapp.classroombackend.service.UserService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;

import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;

    @Autowired
    public AuthServiceImpl(UserRepository userRepository,
                           UserService userService,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil,
                           AuthenticationManager authenticationManager,
                           ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.authenticationManager = authenticationManager;
        this.modelMapper = modelMapper;
    }

    @Override
    public LoginResponseDto authenticateUser(LoginRequestDto loginRequest) {
        log.info("Authenticating user: {}", loginRequest.getUsername());
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);

            User user = userRepository.findByUsername(loginRequest.getUsername())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found"));

            String token = generateToken(user.getEmail(), user.getRoleId(), user.getEmail());
            String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());

            return LoginResponseDto.builder()
                    .token(token)
                    .role(roleName)
                    .roleId(user.getRoleId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .userId(user.getId())
                    .build();
        } catch (BadCredentialsException e) {
            throw new IllegalArgumentException("Invalid username or password");
        }
    }

    @Override
    public UserDto registerUser(RegisterDto registerDto) {
        if (userService.usernameExists(registerDto.getUsername())) {
            throw new IllegalArgumentException("Username already taken");
        }
        if (userService.emailExists(registerDto.getEmail())) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUsername(registerDto.getUsername());
        user.setPassword(passwordEncoder.encode(registerDto.getPassword()));
        user.setEmail(registerDto.getEmail());
        user.setFullName(registerDto.getFullName());
        user.setRoleId(registerDto.getRoleId() != null ? registerDto.getRoleId() : 1);

        User savedUser = userRepository.save(user);
        return modelMapper.map(savedUser, UserDto.class);
    }

    @Override
    public LoginResponseDto authenticateWithGoogle(GoogleAuthRequestDto googleAuthRequest) {
        try {
            FirebaseToken decodedToken = FirebaseAuth.getInstance().verifyIdToken(googleAuthRequest.getIdToken());
            String email = decodedToken.getEmail();

            User user = userRepository.findByEmail(email).orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setUsername(email);
                newUser.setFullName(decodedToken.getName());
                newUser.setPassword(passwordEncoder.encode(generateRandomPassword()));
                newUser.setRoleId(1);
                return userRepository.save(newUser);
            });

            String roleName = jwtUtil.convertRoleIdToName(user.getRoleId());
            String token = generateToken(user.getEmail(), user.getRoleId(), user.getEmail());

            return LoginResponseDto.builder()
                    .token(token)
                    .role(roleName)
                    .roleId(user.getRoleId())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .userId(user.getId())
                    .build();
        } catch (FirebaseAuthException e) {
            throw new IllegalArgumentException("Invalid Google ID token", e);
        }
    }

    @Override
    public void requestPasswordReset(PasswordResetRequestDto passwordResetRequest) {
        User user = userRepository.findByEmail(passwordResetRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        String resetToken = generateToken(user.getUsername(), user.getRoleId(), user.getEmail());
        userService.sendPasswordResetEmail(user.getEmail(), resetToken);
    }

    @Override
    public void resetPassword(PasswordConfirmationDto passwordConfirmation) {
        if (!jwtUtil.validateToken(passwordConfirmation.getToken())) {
            throw new IllegalArgumentException("Invalid or expired token");
        }
        String username = jwtUtil.getSubjectFromToken(passwordConfirmation.getToken());
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        user.setPassword(passwordEncoder.encode(passwordConfirmation.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public String generateToken(String subject, Integer roleId, String email) {
        String roleName = jwtUtil.convertRoleIdToName(roleId);
        Map<String, Object> claims = new HashMap<>();
        claims.put("sub", subject);
        claims.put("email", email);
        claims.put("role", roleId);
        claims.put("roles", new String[]{roleName});

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 24 * 60 * 60 * 1000))
                .signWith(jwtUtil.getSecretKeyFromString())
                .compact();
    }

    private String generateRandomPassword() {
        return "GoogleAuth-" + System.currentTimeMillis();
    }
}
