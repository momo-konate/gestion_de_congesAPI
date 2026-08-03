package apiprojet.apigestiondeconge.config;

import apiprojet.apigestiondeconge.controllers.AuthController;
import apiprojet.apigestiondeconge.controllers.UtilisateurController;
import apiprojet.apigestiondeconge.dto.UtilisateurDto;
import apiprojet.apigestiondeconge.repository.UtilisateurRepository;
import apiprojet.apigestiondeconge.service.UtilisateurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = {AuthController.class, UtilisateurController.class})
@Import({SecurityConfig.class, JwtFilter.class, JwtUtils.class, CustomAccessDeniedHandler.class, CustomAuthenticationEntryPoint.class})
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UtilisateurService utilisateurService;

    @MockitoBean
    private UtilisateurRepository utilisateurRepository;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    private String adminToken;
    private String employeToken;

    @BeforeEach
    void setUp() {
        // Génération de vrais tokens valides pour les tests
        adminToken = jwtUtils.generateToken("admin@email.com", "ADMIN");
        employeToken = jwtUtils.generateToken("employe@email.com", "EMPLOYE");
    }

    @Test
    void testTokenDirectly() {
        String token = jwtUtils.generateToken("test@email.com", "ADMIN");
        boolean isValid = jwtUtils.validateToken(token);
        System.out.println(">>> TOKEN DIRECTLY IS VALID: " + isValid);
        assertTrue(isValid);
        assertEquals("test@email.com", jwtUtils.getEmailFromToken(token));
        assertEquals("ADMIN", jwtUtils.getRoleFromToken(token));
    }

    @Test
    void testPublicEndpoint_Login_IsAllowed() throws Exception {
        // Pas de token requis pour la route publique de login
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"test@email.com\",\"motDePasse\":\"password123\"}"))
                .andExpect(status().isUnauthorized()); // Le controlleur répondra 401 si identifiants faux, ce qui prouve qu'on a traversé la sécurité
    }

    @Test
    void testProtectedEndpoint_WithoutAuth_Returns401() throws Exception {
        // Sans en-tête Authorization -> 401 Unauthorized
        mockMvc.perform(get("/api/utilisateurs/1"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpoint_WithInvalidToken_Returns401() throws Exception {
        // Avec un token invalide -> 401 Unauthorized
        mockMvc.perform(get("/api/utilisateurs/1")
                        .header("Authorization", "Bearer invalid-token-value"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testProtectedEndpoint_WithEmployeRole_Returns403() throws Exception {
        // Un utilisateur EMPLOYE n'est pas autorisé sur /api/utilisateurs/{id} (requiert ADMIN) -> 403 Forbidden
        mockMvc.perform(get("/api/utilisateurs/1")
                        .header("Authorization", "Bearer " + employeToken))
                .andExpect(status().isForbidden());
    }

    @Test
    void testProtectedEndpoint_WithAdminRole_IsAllowed() throws Exception {
        // Un utilisateur ADMIN est autorisé
        UtilisateurDto.Response mockResponse = UtilisateurDto.Response.builder().id(1L).email("user@email.com").build();
        when(utilisateurService.getById(1L)).thenReturn(mockResponse);

        mockMvc.perform(get("/api/utilisateurs/1")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }
}
