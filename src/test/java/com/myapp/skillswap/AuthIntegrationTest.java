package com.myapp.skillswap;

import com.myapp.skillswap.entity.User;
import com.myapp.skillswap.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class AuthIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();
        userRepository.deleteAll();
    }

    @Test
    void testRegisterSuccess() throws Exception {
        String registerJson = """
                {
                    "name": "Alice Smith",
                    "email": "alice@example.com",
                    "password": "securePassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Alice Smith"))
                .andExpect(jsonPath("$.email").value("alice@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());

        User savedUser = userRepository.findByEmail("alice@example.com").orElse(null);
        assertNotNull(savedUser);
        assertEquals("Alice Smith", savedUser.getName());
    }

    @Test
    void testPasswordHashing() throws Exception {
        String rawPassword = "mySecretPassword";
        String registerJson = String.format("""
                {
                    "name": "Bob Jones",
                    "email": "bob@example.com",
                    "password": "%s"
                }
                """, rawPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        User savedUser = userRepository.findByEmail("bob@example.com").orElse(null);
        assertNotNull(savedUser);

        // Verify password is NOT saved in plaintext
        assertNotEquals(rawPassword, savedUser.getPassword());

        // Verify password hash uses BCrypt format ($2a$ or $2b$)
        assertTrue(savedUser.getPassword().startsWith("$2a$") || savedUser.getPassword().startsWith("$2b$"));

        // Verify passwordEncoder.matches evaluates to true
        assertTrue(passwordEncoder.matches(rawPassword, savedUser.getPassword()));
    }

    @Test
    void testRegisterDuplicateEmail() throws Exception {
        String firstUser = """
                {
                    "name": "Charlie",
                    "email": "charlie@example.com",
                    "password": "password123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(firstUser))
                .andExpect(status().isCreated());

        // Second registration with the same email (different casing)
        String duplicateUser = """
                {
                    "name": "Charlie Duplicate",
                    "email": "CHARLIE@EXAMPLE.COM",
                    "password": "differentPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateUser))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message", containsString("already registered")));
    }

    @Test
    void testRegisterInvalidInput_BlankName() throws Exception {
        String invalidJson = """
                {
                    "name": "",
                    "email": "valid@example.com",
                    "password": "validPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.name").exists());
    }

    @Test
    void testRegisterInvalidInput_InvalidEmail() throws Exception {
        String invalidJson = """
                {
                    "name": "David",
                    "email": "not-an-email",
                    "password": "validPassword123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.email").exists());
    }

    @Test
    void testRegisterInvalidInput_ShortPassword() throws Exception {
        String invalidJson = """
                {
                    "name": "Eve",
                    "email": "eve@example.com",
                    "password": "123"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.validationErrors.password").exists());
    }

    @Test
    void testLoginSuccess() throws Exception {
        // Register user
        String rawPassword = "superSecretPassword";
        String registerJson = String.format("""
                {
                    "name": "Grace",
                    "email": "grace@example.com",
                    "password": "%s"
                }
                """, rawPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // Login
        String loginJson = String.format("""
                {
                    "email": "grace@example.com",
                    "password": "%s"
                }
                """, rawPassword);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Login successful"))
                .andExpect(jsonPath("$.user.id").isNumber())
                .andExpect(jsonPath("$.user.name").value("Grace"))
                .andExpect(jsonPath("$.user.email").value("grace@example.com"))
                .andExpect(jsonPath("$.user.password").doesNotExist())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testLoginFailureWrongPassword() throws Exception {
        // Register user
        String registerJson = """
                {
                    "name": "Henry",
                    "email": "henry@example.com",
                    "password": "correctPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // Login with incorrect password
        String loginJson = """
                {
                    "email": "henry@example.com",
                    "password": "wrongPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testLoginFailureNonExistentUser() throws Exception {
        String loginJson = """
                {
                    "email": "nobody@example.com",
                    "password": "anyPassword"
                }
                """;

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.message").value("Invalid email or password"));
    }

    @Test
    void testProtectedEndpointAccess() throws Exception {
        // Register user to use for authentication
        String rawPassword = "passwordProtected123";
        String registerJson = String.format("""
                {
                    "name": "Protected User",
                    "email": "protected@example.com",
                    "password": "%s"
                }
                """, rawPassword);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson))
                .andExpect(status().isCreated());

        // 1. Unauthenticated request to GET /api/users should return 401 Unauthorized
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("Unauthorized"));

        // 2. Authenticated request using HTTP Basic should return 200 OK and list of users without password
        mockMvc.perform(get("/api/users")
                        .with(httpBasic("protected@example.com", rawPassword)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].email").value("protected@example.com"))
                .andExpect(jsonPath("$[0].password").doesNotExist());
    }

    @Test
    void testGetCurrentUserUnauthenticated() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetCurrentUserAuthenticated() throws Exception {
        userRepository.save(new User("Test User", "testuser@example.com", passwordEncoder.encode("password123")));

        mockMvc.perform(get("/api/auth/me")
                        .with(httpBasic("testuser@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Test User"))
                .andExpect(jsonPath("$.email").value("testuser@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void testSessionBasedAuthenticationAfterLogin() throws Exception {
        userRepository.save(new User("Session User", "session@example.com", passwordEncoder.encode("password123")));

        String loginJson = """
                {
                    "email": "session@example.com",
                    "password": "password123"
                }
                """;

        var loginResult = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();

        var session = loginResult.getRequest().getSession(false);
        assertNotNull(session);

        // Access protected /api/auth/me using the session without sending basic auth header
        mockMvc.perform(get("/api/auth/me").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("session@example.com"));

        // Logout
        mockMvc.perform(post("/api/auth/logout").session((org.springframework.mock.web.MockHttpSession) session))
                .andExpect(status().isNoContent());
    }
}
