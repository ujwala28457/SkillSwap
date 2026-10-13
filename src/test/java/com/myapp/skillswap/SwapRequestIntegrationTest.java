package com.myapp.skillswap;

import com.myapp.skillswap.dto.AddUserSkillRequest;
import com.myapp.skillswap.dto.RegisterRequest;
import com.myapp.skillswap.dto.UserResponse;
import com.myapp.skillswap.entity.*;
import com.myapp.skillswap.repository.SkillRepository;
import com.myapp.skillswap.repository.SwapRequestRepository;
import com.myapp.skillswap.repository.UserRepository;
import com.myapp.skillswap.repository.UserSkillRepository;
import com.myapp.skillswap.service.SkillService;
import com.myapp.skillswap.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.jayway.jsonpath.JsonPath;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
class SwapRequestIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private SwapRequestRepository swapRequestRepository;

    @Autowired
    private UserSkillRepository userSkillRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private UserRepository userRepository;

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

    @AfterEach
    void tearDown() {
        swapRequestRepository.deleteAll();
        userSkillRepository.deleteAll();
        skillRepository.deleteAll();
        userRepository.deleteAll();
    }

    // -------------------------------------------------------------
    // 1. Successful Creation
    // -------------------------------------------------------------

    @Test
    void testCreateSwapRequestSuccess() throws Exception {
        UserResponse alice = registerUser("Alice", "alice@example.com", "password123");
        UserResponse bob = registerUser("Bob", "bob@example.com", "password123");

        User aliceEntity = userRepository.findById(alice.getId()).orElseThrow();
        User bobEntity = userRepository.findById(bob.getId()).orElseThrow();

        var aliceTeach = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("Java", ProficiencyLevel.EXPERT, "Teach Java"), SkillType.TEACH);
        var aliceLearn = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("React", ProficiencyLevel.BEGINNER, "Learn React"), SkillType.LEARN);

        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("React", ProficiencyLevel.ADVANCED, "Teach React"), SkillType.TEACH);
        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("Java", ProficiencyLevel.BEGINNER, "Learn Java"), SkillType.LEARN);

        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Let's swap Java for React!"
                }
                """, bob.getId(), aliceTeach.getSkillId(), aliceLearn.getSkillId());

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic("alice@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.message").value("Let's swap Java for React!"))
                .andExpect(jsonPath("$.requester.id").value(alice.getId()))
                .andExpect(jsonPath("$.requester.name").value("Alice"))
                .andExpect(jsonPath("$.requester.email").value("alice@example.com"))
                .andExpect(jsonPath("$.requester.password").doesNotExist())
                .andExpect(jsonPath("$.recipient.id").value(bob.getId()))
                .andExpect(jsonPath("$.recipient.name").value("Bob"))
                .andExpect(jsonPath("$.recipient.email").value("bob@example.com"))
                .andExpect(jsonPath("$.recipient.password").doesNotExist())
                .andExpect(jsonPath("$.offeredSkill.skillId").value(aliceTeach.getSkillId()))
                .andExpect(jsonPath("$.offeredSkill.skillName").value("Java"))
                .andExpect(jsonPath("$.offeredSkill.proficiencyLevel").value("EXPERT"))
                .andExpect(jsonPath("$.requestedSkill.skillId").value(aliceLearn.getSkillId()))
                .andExpect(jsonPath("$.requestedSkill.skillName").value("React"))
                .andExpect(jsonPath("$.requestedSkill.proficiencyLevel").value("ADVANCED"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    // -------------------------------------------------------------
    // 2. Incoming & Outgoing Swap Requests List
    // -------------------------------------------------------------

    @Test
    void testGetIncomingAndOutgoingSwapRequests() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();

        createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Alice to Bob");

        // Bob's incoming requests should have 1 item
        mockMvc.perform(get("/api/swaps/incoming")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].requester.id").value(users.aliceId))
                .andExpect(jsonPath("$[0].recipient.id").value(users.bobId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // Alice's outgoing requests should have 1 item
        mockMvc.perform(get("/api/swaps/outgoing")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].requester.id").value(users.aliceId))
                .andExpect(jsonPath("$[0].recipient.id").value(users.bobId))
                .andExpect(jsonPath("$[0].status").value("PENDING"));

        // Alice's incoming requests should be empty
        mockMvc.perform(get("/api/swaps/incoming")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // Bob's outgoing requests should be empty
        mockMvc.perform(get("/api/swaps/outgoing")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    // -------------------------------------------------------------
    // 3. Accept Swap Request
    // -------------------------------------------------------------

    @Test
    void testAcceptSwapRequestSuccess() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();
        Long swapId = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Swap!");

        mockMvc.perform(put("/api/swaps/" + swapId + "/accept")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(swapId))
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    // -------------------------------------------------------------
    // 4. Reject Swap Request
    // -------------------------------------------------------------

    @Test
    void testRejectSwapRequestSuccess() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();
        Long swapId = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Swap!");

        mockMvc.perform(put("/api/swaps/" + swapId + "/reject")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(swapId))
                .andExpect(jsonPath("$.status").value("REJECTED"));
    }

    // -------------------------------------------------------------
    // 5. Cancel Swap Request
    // -------------------------------------------------------------

    @Test
    void testCancelSwapRequestSuccess() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();
        Long swapId = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Swap!");

        mockMvc.perform(put("/api/swaps/" + swapId + "/cancel")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(swapId))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    // -------------------------------------------------------------
    // 6. Self-Swap Rejection
    // -------------------------------------------------------------

    @Test
    void testSelfSwapRejection() throws Exception {
        UserResponse alice = registerUser("Alice", "alice@example.com", "password123");
        User aliceEntity = userRepository.findById(alice.getId()).orElseThrow();

        var javaSkill = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("Java", ProficiencyLevel.EXPERT, "Teach Java"), SkillType.TEACH);
        var reactSkill = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("React", ProficiencyLevel.BEGINNER, "Learn React"), SkillType.LEARN);

        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Self swap attempt"
                }
                """, alice.getId(), javaSkill.getSkillId(), reactSkill.getSkillId());

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic("alice@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("cannot create a swap request with yourself")));
    }

    // -------------------------------------------------------------
    // 7. Invalid Skill Ownership
    // -------------------------------------------------------------

    @Test
    void testInvalidSkillOwnership_RequesterDoesNotTeachOfferedSkill() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();

        // Create third skill Python that Alice doesn't teach
        Skill python = skillRepository.save(new Skill("Python", "Python language"));

        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Invalid offered skill"
                }
                """, users.bobId, python.getId(), users.reactSkillId);

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic(users.aliceEmail, "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("offered (TEACH) skills list")));
    }

    @Test
    void testInvalidSkillOwnership_RequesterDoesNotLearnRequestedSkill() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();

        // Create third skill Python that Alice doesn't learn
        Skill python = skillRepository.save(new Skill("Python", "Python language"));

        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Invalid requested skill"
                }
                """, users.bobId, users.javaSkillId, python.getId());

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic(users.aliceEmail, "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("learning (LEARN) skills list")));
    }

    @Test
    void testInvalidSkillOwnership_RecipientDoesNotTeachRequestedSkill() throws Exception {
        UserResponse alice = registerUser("Alice", "alice@example.com", "password123");
        UserResponse bob = registerUser("Bob", "bob@example.com", "password123");

        User aliceEntity = userRepository.findById(alice.getId()).orElseThrow();
        User bobEntity = userRepository.findById(bob.getId()).orElseThrow();

        var javaSkill = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("Java", ProficiencyLevel.EXPERT, "Teach Java"), SkillType.TEACH);
        var reactSkill = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("React", ProficiencyLevel.BEGINNER, "Learn React"), SkillType.LEARN);

        // Bob learns Java, but does NOT teach React (teaches Guitar instead)
        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("Guitar", ProficiencyLevel.ADVANCED, "Teach Guitar"), SkillType.TEACH);
        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("Java", ProficiencyLevel.BEGINNER, "Learn Java"), SkillType.LEARN);

        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Bob does not teach React"
                }
                """, bob.getId(), javaSkill.getSkillId(), reactSkill.getSkillId());

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic("alice@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Recipient does not offer the requested skill")));
    }

    @Test
    void testInvalidSkillOwnership_RecipientDoesNotLearnOfferedSkill() throws Exception {
        UserResponse alice = registerUser("Alice", "alice@example.com", "password123");
        UserResponse bob = registerUser("Bob", "bob@example.com", "password123");

        User aliceEntity = userRepository.findById(alice.getId()).orElseThrow();
        User bobEntity = userRepository.findById(bob.getId()).orElseThrow();

        var javaSkill = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("Java", ProficiencyLevel.EXPERT, "Teach Java"), SkillType.TEACH);
        var reactSkill = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("React", ProficiencyLevel.BEGINNER, "Learn React"), SkillType.LEARN);

        // Bob teaches React, but does NOT learn Java (wants Guitar instead)
        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("React", ProficiencyLevel.ADVANCED, "Teach React"), SkillType.TEACH);
        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("Guitar", ProficiencyLevel.BEGINNER, "Learn Guitar"), SkillType.LEARN);

        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Bob does not want Java"
                }
                """, bob.getId(), javaSkill.getSkillId(), reactSkill.getSkillId());

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic("alice@example.com", "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Recipient is not seeking to learn the offered skill")));
    }

    // -------------------------------------------------------------
    // 8. Duplicate Pending Requests
    // -------------------------------------------------------------

    @Test
    void testDuplicatePendingSwapRequestRejection() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();

        createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "First swap");

        // Alice attempts to send duplicate request
        String duplicateJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Duplicate swap attempt"
                }
                """, users.bobId, users.javaSkillId, users.reactSkillId);

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic(users.aliceEmail, "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("pending swap request already exists")));

        // Bob attempts reciprocal swap while Alice's is pending
        String reciprocalJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "Bob's reverse swap attempt"
                }
                """, users.aliceId, users.reactSkillId, users.javaSkillId);

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic(users.bobEmail, "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(reciprocalJson))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("pending swap request already exists")));
    }

    // -------------------------------------------------------------
    // 9. Unauthorized Actions
    // -------------------------------------------------------------

    @Test
    void testUnauthorizedActions() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();
        UserResponse charlie = registerUser("Charlie", "charlie@example.com", "password123");

        Long swapId = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Pending swap");

        // Unauthenticated access
        mockMvc.perform(get("/api/swaps/incoming"))
                .andExpect(status().isUnauthorized());

        // Requester Alice cannot accept
        mockMvc.perform(put("/api/swaps/" + swapId + "/accept")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isForbidden());

        // Requester Alice cannot reject
        mockMvc.perform(put("/api/swaps/" + swapId + "/reject")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isForbidden());

        // Recipient Bob cannot cancel
        mockMvc.perform(put("/api/swaps/" + swapId + "/cancel")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isForbidden());

        // Third-party Charlie cannot accept, reject, or cancel
        mockMvc.perform(put("/api/swaps/" + swapId + "/accept")
                        .with(httpBasic("charlie@example.com", "password123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/swaps/" + swapId + "/reject")
                        .with(httpBasic("charlie@example.com", "password123")))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/swaps/" + swapId + "/cancel")
                        .with(httpBasic("charlie@example.com", "password123")))
                .andExpect(status().isForbidden());
    }

    // -------------------------------------------------------------
    // 10. Invalid Transitions
    // -------------------------------------------------------------

    @Test
    void testInvalidTransitionsFromAccepted() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();
        Long swapId = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Pending swap");

        // Bob accepts
        mockMvc.perform(put("/api/swaps/" + swapId + "/accept")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isOk());

        // Cannot accept again
        mockMvc.perform(put("/api/swaps/" + swapId + "/accept")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot accept swap request that is not pending")));

        // Cannot reject after accepted
        mockMvc.perform(put("/api/swaps/" + swapId + "/reject")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot reject swap request that is not pending")));

        // Cannot cancel after accepted
        mockMvc.perform(put("/api/swaps/" + swapId + "/cancel")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot cancel swap request that is not pending")));
    }

    @Test
    void testInvalidTransitionsFromRejectedAndCancelled() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();

        // 1. Rejected swap cannot be accepted
        Long swapId1 = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Swap 1");
        mockMvc.perform(put("/api/swaps/" + swapId1 + "/reject")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/swaps/" + swapId1 + "/accept")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot accept swap request that is not pending")));

        // 2. Cancelled swap cannot be accepted or rejected
        Long swapId2 = createSwap(users.aliceEmail, users.bobId, users.javaSkillId, users.reactSkillId, "Swap 2");
        mockMvc.perform(put("/api/swaps/" + swapId2 + "/cancel")
                        .with(httpBasic(users.aliceEmail, "password123")))
                .andExpect(status().isOk());

        mockMvc.perform(put("/api/swaps/" + swapId2 + "/accept")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot accept swap request that is not pending")));

        mockMvc.perform(put("/api/swaps/" + swapId2 + "/reject")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("Cannot reject swap request that is not pending")));
    }

    // -------------------------------------------------------------
    // 11. Resource Not Found for Invalid IDs
    // -------------------------------------------------------------

    @Test
    void testResourceNotFoundForInvalidIds() throws Exception {
        SetupUsers users = setupAliceAndBobWithCompatibleSkills();

        // Non-existent recipient
        String badRecipientJson = String.format("""
                {
                    "recipientId": 99999,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d
                }
                """, users.javaSkillId, users.reactSkillId);

        mockMvc.perform(post("/api/swaps")
                        .with(httpBasic(users.aliceEmail, "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badRecipientJson))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Recipient user not found")));

        // Non-existent swap request ID on accept
        mockMvc.perform(put("/api/swaps/99999/accept")
                        .with(httpBasic(users.bobEmail, "password123")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message", containsString("Swap request not found")));
    }

    // -------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------

    private static class SetupUsers {
        Long aliceId;
        String aliceEmail;
        Long bobId;
        String bobEmail;
        Long javaSkillId;
        Long reactSkillId;
    }

    private SetupUsers setupAliceAndBobWithCompatibleSkills() {
        UserResponse alice = registerUser("Alice", "alice@example.com", "password123");
        UserResponse bob = registerUser("Bob", "bob@example.com", "password123");

        User aliceEntity = userRepository.findById(alice.getId()).orElseThrow();
        User bobEntity = userRepository.findById(bob.getId()).orElseThrow();

        var aliceTeach = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("Java", ProficiencyLevel.EXPERT, "Teach Java"), SkillType.TEACH);
        var aliceLearn = skillService.addSkillToUser(aliceEntity, new AddUserSkillRequest("React", ProficiencyLevel.BEGINNER, "Learn React"), SkillType.LEARN);

        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("React", ProficiencyLevel.ADVANCED, "Teach React"), SkillType.TEACH);
        skillService.addSkillToUser(bobEntity, new AddUserSkillRequest("Java", ProficiencyLevel.BEGINNER, "Learn Java"), SkillType.LEARN);

        SetupUsers result = new SetupUsers();
        result.aliceId = alice.getId();
        result.aliceEmail = alice.getEmail();
        result.bobId = bob.getId();
        result.bobEmail = bob.getEmail();
        result.javaSkillId = aliceTeach.getSkillId();
        result.reactSkillId = aliceLearn.getSkillId();
        return result;
    }

    private Long createSwap(String requesterEmail, Long recipientId, Long offeredSkillId, Long requestedSkillId, String message) throws Exception {
        String createJson = String.format("""
                {
                    "recipientId": %d,
                    "offeredSkillId": %d,
                    "requestedSkillId": %d,
                    "message": "%s"
                }
                """, recipientId, offeredSkillId, requestedSkillId, message);

        MvcResult result = mockMvc.perform(post("/api/swaps")
                        .with(httpBasic(requesterEmail, "password123"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createJson))
                .andExpect(status().isCreated())
                .andReturn();

        Number id = JsonPath.read(result.getResponse().getContentAsString(), "$.id");
        return id.longValue();
    }

    private UserResponse registerUser(String name, String email, String password) {
        return userService.register(new RegisterRequest(name, email, password));
    }
}
