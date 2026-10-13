package com.myapp.skillswap;

import com.myapp.skillswap.dto.AddUserSkillRequest;
import com.myapp.skillswap.dto.RegisterRequest;
import com.myapp.skillswap.dto.SkillRequest;
import com.myapp.skillswap.dto.UserResponse;
import com.myapp.skillswap.entity.ProficiencyLevel;
import com.myapp.skillswap.entity.Skill;
import com.myapp.skillswap.entity.SkillType;
import com.myapp.skillswap.entity.User;
import com.myapp.skillswap.repository.SkillRepository;
import com.myapp.skillswap.repository.SwapRequestRepository;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.repository.UserSkillRepository;
import com.myapp.skillswap.service.SkillService;
import com.myapp.skillswap.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SkillMarketplaceIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SwapRequestRepository swapRequestRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private SkillService skillService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        swapRequestRepository.deleteAll();
        userSkillRepository.deleteAll();
        skillRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -------------------------------------------------------------
    // 1. Skill Catalog Creation & Search with Pagination
    // -------------------------------------------------------------

    @Test
    void testCreateSkillInCatalog() throws Exception {
        UserResponse user = registerUser("Creator", "creator@example.com", "password123");

        String skillJson = """
                {
                    "name": "Spring Boot",
                    "description": "Backend Java Framework"
                }
                """;

        mockMvc.perform(post("/api/skills")
                        .with(httpBasic("creator@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.name").value("Spring Boot"))
                .andExpect(jsonPath("$.description").value("Backend Java Framework"));
    }

    @Test
    void testSearchSkillsWithPagination() throws Exception {
        skillService.createSkill(new SkillRequest("Java Programming", "Core Java"));
        skillService.createSkill(new SkillRequest("JavaScript Basics", "Frontend JS"));
        skillService.createSkill(new SkillRequest("Python Data Science", "Python 3"));
        skillService.createSkill(new SkillRequest("Rust Systems", "Memory safe language"));

        // Search for "Java" (case-insensitive substring) -> expects Java Programming & JavaScript Basics
        mockMvc.perform(get("/api/skills")
                        .param("name", "java")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2));

        // Paginate all skills with size 2
        mockMvc.perform(get("/api/skills")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(4))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    // -------------------------------------------------------------
    // 2. Skill Management (Add & Remove Offered / Learning)
    // -------------------------------------------------------------

    @Test
    void testAddAndRemoveOfferedSkill() throws Exception {
        registerUser("Teacher", "teacher@example.com", "password123");

        String addSkillJson = """
                {
                    "skillName": "Guitar",
                    "proficiencyLevel": "ADVANCED",
                    "description": "Acoustic and electric guitar instructor"
                }
                """;

        // Add offered skill
        mockMvc.perform(post("/api/users/me/skills/offered")
                        .with(httpBasic("teacher@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addSkillJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skillName").value("Guitar"))
                .andExpect(jsonPath("$.skillType").value("TEACH"))
                .andExpect(jsonPath("$.proficiencyLevel").value("ADVANCED"));

        // Retrieve user's offered skills
        mockMvc.perform(get("/api/users/me/skills/offered")
                        .with(httpBasic("teacher@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].skillName").value("Guitar"));

        Skill guitarSkill = skillRepository.findByNameIgnoreCase("Guitar").orElseThrow();

        // Remove offered skill
        mockMvc.perform(delete("/api/users/me/skills/offered/" + guitarSkill.getId())
                        .with(httpBasic("teacher@example.com", "password123")))
                .andExpect(status().isNoContent());

        // Verify it is gone
        mockMvc.perform(get("/api/users/me/skills/offered")
                        .with(httpBasic("teacher@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void testAddAndRemoveLearningSkill() throws Exception {
        registerUser("Student", "student@example.com", "password123");

        String addSkillJson = """
                {
                    "skillName": "Spanish",
                    "proficiencyLevel": "BEGINNER",
                    "description": "Interested in conversational Spanish"
                }
                """;

        // Add learning skill
        mockMvc.perform(post("/api/users/me/skills/learning")
                        .with(httpBasic("student@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(addSkillJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.skillName").value("Spanish"))
                .andExpect(jsonPath("$.skillType").value("LEARN"))
                .andExpect(jsonPath("$.proficiencyLevel").value("BEGINNER"));

        Skill spanishSkill = skillRepository.findByNameIgnoreCase("Spanish").orElseThrow();

        // Remove learning skill
        mockMvc.perform(delete("/api/users/me/skills/learning/" + spanishSkill.getId())
                        .with(httpBasic("student@example.com", "password123")))
                .andExpect(status().isNoContent());

        // Verify list is empty
        mockMvc.perform(get("/api/users/me/skills/learning")
                        .with(httpBasic("student@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------
    // 3. Duplicate and Invalid Associations
    // -------------------------------------------------------------

    @Test
    void testPreventDuplicateSkillAssociation() throws Exception {
        registerUser("Dan", "dan@example.com", "password123");

        String skillJson = """
                {
                    "skillName": "Python",
                    "proficiencyLevel": "INTERMEDIATE"
                }
                """;

        // First add succeeds
        mockMvc.perform(post("/api/users/me/skills/offered")
                        .with(httpBasic("dan@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isCreated());

        // Duplicate add to same list returns 409 Conflict
        mockMvc.perform(post("/api/users/me/skills/offered")
                        .with(httpBasic("dan@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message", containsString("already have 'Python'")));
    }

    @Test
    void testPreventContradictorySkillAssociation() throws Exception {
        registerUser("Elena", "elena@example.com", "password123");

        String offerJson = """
                {
                    "skillName": "French",
                    "proficiencyLevel": "EXPERT"
                }
                """;

        // Elena offers French
        mockMvc.perform(post("/api/users/me/skills/offered")
                        .with(httpBasic("elena@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(offerJson))
                .andExpect(status().isCreated());

        String learnJson = """
                {
                    "skillName": "French",
                    "proficiencyLevel": "BEGINNER"
                }
                """;

        // Elena tries to also add French as a skill she wants to learn -> 400 Bad Request
        mockMvc.perform(post("/api/users/me/skills/learning")
                        .with(httpBasic("elena@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(learnJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message", containsString("cannot learn a skill ('French') that you already registered to teach")));
    }

    // -------------------------------------------------------------
    // 4. Public Skill Profile
    // -------------------------------------------------------------

    @Test
    void testViewUserPublicSkillProfile() throws Exception {
        UserResponse user = registerUser("Frank", "frank@example.com", "password123");
        User userEntity = userRepository.findById(user.getId()).orElseThrow();

        Skill chess = skillRepository.save(new Skill("Chess", "Strategy"));
        Skill piano = skillRepository.save(new Skill("Piano", "Music"));

        skillService.addSkillToUser(userEntity, new AddUserSkillRequest(chess.getId(), ProficiencyLevel.EXPERT, "Grandmaster"), SkillType.TEACH);
        skillService.addSkillToUser(userEntity, new AddUserSkillRequest(piano.getId(), ProficiencyLevel.BEGINNER, "Learning keys"), SkillType.LEARN);

        // Public endpoint without authentication
        mockMvc.perform(get("/api/users/" + user.getId() + "/profile"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(user.getId()))
                .andExpect(jsonPath("$.name").value("Frank"))
                .andExpect(jsonPath("$.skillsOffered", hasSize(1)))
                .andExpect(jsonPath("$.skillsOffered[0].skillName").value("Chess"))
                .andExpect(jsonPath("$.skillsLearning", hasSize(1)))
                .andExpect(jsonPath("$.skillsLearning[0].skillName").value("Piano"));
    }

    @Test
    void testViewNonExistentUserProfile() throws Exception {
        mockMvc.perform(get("/api/users/99999/profile"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    // -------------------------------------------------------------
    // 5. Discover Skills Offered by Other Users
    // -------------------------------------------------------------

    @Test
    void testDiscoverOfferedSkillsExcludesSelf() throws Exception {
        UserResponse userA = registerUser("User A", "usera@example.com", "password123");
        UserResponse userB = registerUser("User B", "userb@example.com", "password123");

        User entityA = userRepository.findById(userA.getId()).orElseThrow();
        User entityB = userRepository.findById(userB.getId()).orElseThrow();

        skillService.addSkillToUser(entityA, new AddUserSkillRequest("Java", ProficiencyLevel.ADVANCED, "Teach Java"), SkillType.TEACH);
        skillService.addSkillToUser(entityB, new AddUserSkillRequest("Kotlin", ProficiencyLevel.ADVANCED, "Teach Kotlin"), SkillType.TEACH);
        skillService.addSkillToUser(entityB, new AddUserSkillRequest("Scala", ProficiencyLevel.INTERMEDIATE, "Teach Scala"), SkillType.TEACH);

        // When User A discovers skills, they should see Kotlin and Scala from User B, NOT their own Java
        mockMvc.perform(get("/api/skills/offered")
                        .with(httpBasic("usera@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[?(@.userName == 'User B')]", hasSize(2)))
                .andExpect(jsonPath("$.content[?(@.userName == 'User A')]", hasSize(0)));
    }

    // -------------------------------------------------------------
    // 6. Authorization & Ownership Protection
    // -------------------------------------------------------------

    @Test
    void testUnauthenticatedUserCannotAddSkill() throws Exception {
        String skillJson = """
                {
                    "skillName": "Rust",
                    "proficiencyLevel": "BEGINNER"
                }
                """;

        mockMvc.perform(post("/api/users/me/skills/offered")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testPreventUserFromModifyingAnotherUsersSkills() throws Exception {
        UserResponse userA = registerUser("User One", "user1@example.com", "password123");
        UserResponse userB = registerUser("User Two", "user2@example.com", "password123");

        String skillJson = """
                {
                    "skillName": "Hacking",
                    "proficiencyLevel": "EXPERT"
                }
                """;

        // User A attempts to modify User B's skills via /api/users/{userB.id}/skills/offered
        mockMvc.perform(post("/api/users/" + userB.getId() + "/skills/offered")
                        .with(httpBasic("user1@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(skillJson))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message", containsString("cannot modify another user's skill associations")));
    }

    // -------------------------------------------------------------
    // 7. Complementary Skill Matching
    // -------------------------------------------------------------

    @Test
    void testComplementarySkillMatching() throws Exception {
        // User A: Teaches "Java", Wants to learn "Guitar"
        UserResponse userA = registerUser("Alice Match", "alice.match@example.com", "password123");
        User entityA = userRepository.findById(userA.getId()).orElseThrow();
        skillService.addSkillToUser(entityA, new AddUserSkillRequest("Java", ProficiencyLevel.ADVANCED, "Teach Java"), SkillType.TEACH);
        skillService.addSkillToUser(entityA, new AddUserSkillRequest("Guitar", ProficiencyLevel.BEGINNER, "Learn Guitar"), SkillType.LEARN);

        // User B (Perfect Match): Teaches "Guitar", Wants to learn "Java"
        UserResponse userB = registerUser("Bob Match", "bob.match@example.com", "password123");
        User entityB = userRepository.findById(userB.getId()).orElseThrow();
        skillService.addSkillToUser(entityB, new AddUserSkillRequest("Guitar", ProficiencyLevel.EXPERT, "Teach Guitar"), SkillType.TEACH);
        skillService.addSkillToUser(entityB, new AddUserSkillRequest("Java", ProficiencyLevel.BEGINNER, "Learn Java"), SkillType.LEARN);

        // User C (One-way only): Teaches "Guitar", Wants to learn "Cooking" (No match for Alice)
        UserResponse userC = registerUser("Charlie OneWay", "charlie.oneway@example.com", "password123");
        User entityC = userRepository.findById(userC.getId()).orElseThrow();
        skillService.addSkillToUser(entityC, new AddUserSkillRequest("Guitar", ProficiencyLevel.INTERMEDIATE, "Teach Guitar"), SkillType.TEACH);
        skillService.addSkillToUser(entityC, new AddUserSkillRequest("Cooking", ProficiencyLevel.BEGINNER, "Learn Cooking"), SkillType.LEARN);

        // User D (One-way only): Teaches "Cooking", Wants to learn "Java" (No match for Alice)
        UserResponse userD = registerUser("Diana OneWay", "diana.oneway@example.com", "password123");
        User entityD = userRepository.findById(userD.getId()).orElseThrow();
        skillService.addSkillToUser(entityD, new AddUserSkillRequest("Cooking", ProficiencyLevel.ADVANCED, "Teach Cooking"), SkillType.TEACH);
        skillService.addSkillToUser(entityD, new AddUserSkillRequest("Java", ProficiencyLevel.BEGINNER, "Learn Java"), SkillType.LEARN);

        // Query matches for Alice
        mockMvc.perform(get("/api/skills/matches")
                        .with(httpBasic("alice.match@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].matchedUser.id").value(userB.getId()))
                .andExpect(jsonPath("$[0].matchedUser.name").value("Bob Match"))
                .andExpect(jsonPath("$[0].matchedUser.email").value("bob.match@example.com"))
                .andExpect(jsonPath("$[0].skillsTheyTeachYou", hasSize(1)))
                .andExpect(jsonPath("$[0].skillsTheyTeachYou[0].skillName").value("Guitar"))
                .andExpect(jsonPath("$[0].skillsYouTeachThem", hasSize(1)))
                .andExpect(jsonPath("$[0].skillsYouTeachThem[0].skillName").value("Java"));
    }

    private UserResponse registerUser(String name, String email, String password) {
        return userService.register(new RegisterRequest(name, email, password));
    }
}
