package com.resumeoptimizer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resumeoptimizer.dto.request.LoginRequest;
import com.resumeoptimizer.dto.request.RegisterRequest;
import com.resumeoptimizer.entity.User;
import com.resumeoptimizer.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.resumeoptimizer.security.JwtTokenProvider;
import com.resumeoptimizer.security.CustomUserDetailsService;
import com.resumeoptimizer.security.JwtAuthenticationEntryPoint;
import com.resumeoptimizer.security.CustomUserDetails;
import org.springframework.security.authentication.AuthenticationManager;
import org.mockito.Mockito;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
public class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthenticationManager authenticationManager;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        // Mocking behavior
        Mockito.when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        Mockito.when(passwordEncoder.encode(any())).thenReturn("hashed_password");
    }

    @Test
    void testRegisterUser() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@example.com");
        request.setPassword("password123");
        request.setFullName("Test User");

        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }

    @Test
    void testLoginUser() throws Exception {
        LoginRequest request = new LoginRequest();
        request.setEmail("login@example.com");
        request.setPassword("password123");

        // We don't really mock full auth details for WebMvcTest easily without much code
        // Instead of doing full integration test, we just check that the endpoint exists
        // and returns a status (might be 401 Unauthorized if mocks aren't set perfectly)
        // For a basic test, this is fine or we can just mock the AuthenticationManager.
        
        // Mock the AuthenticationManager
        org.springframework.security.core.Authentication auth = Mockito.mock(org.springframework.security.core.Authentication.class);
        Mockito.when(authenticationManager.authenticate(any())).thenReturn(auth);
        
        User user = new User();
        user.setId(1L);
        user.setEmail("login@example.com");
        user.setFullName("Login User");
        
        CustomUserDetails userDetails = new CustomUserDetails(user);
        Mockito.when(auth.getPrincipal()).thenReturn(userDetails);
        Mockito.when(jwtTokenProvider.generateToken(any())).thenReturn("mocked_jwt_token");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
}
