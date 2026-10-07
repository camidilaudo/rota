package com.rota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.AuthRequestDTO;
import com.rota.dto.request.UsuarioRequestDTO;
import com.rota.entity.Rol;
import com.rota.entity.Usuario;
import com.rota.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@SuppressWarnings("null")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class UsuarioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("HU-05: El dueño crea un usuario con rol asignado y contraseña encriptada")
    void testCrearUsuarioConRol() throws Exception {
        String email = "repositor.hu05." + System.nanoTime() + "@comercio.com";
        UsuarioRequestDTO dto = nuevoUsuario("Ana Repositora", email, "clave123", Rol.ROLE_REPOSITOR);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.email").value(email))
                .andExpect(jsonPath("$.rol").value("ROLE_REPOSITOR"))
                .andExpect(jsonPath("$.comercioId").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        Usuario guardado = usuarioRepository.findByEmail(email).orElseThrow();
        assertEquals(Rol.ROLE_REPOSITOR, guardado.getRol());
        assertNotEquals("clave123", guardado.getPassword());
        assertTrue(passwordEncoder.matches("clave123", guardado.getPassword()));

        // El usuario creado puede iniciar sesión con su rol
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login(email, "clave123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.rol").value("ROLE_REPOSITOR"));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("FALLO HU-05: No se puede crear un usuario con un email ya registrado")
    void testCrearUsuario_EmailDuplicado() throws Exception {
        UsuarioRequestDTO dto = nuevoUsuario("Otro Repositor", "repositor@comercio.com", "clave123", Rol.ROLE_REPOSITOR);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("FALLO HU-05: No se puede crear un usuario sin rol")
    void testCrearUsuario_SinRol() throws Exception {
        UsuarioRequestDTO dto = nuevoUsuario("Sin Rol", "sin.rol." + System.nanoTime() + "@comercio.com", "clave123", null);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("FALLO HU-05: Repositor intenta crear un usuario (Sin Autorización)")
    void testCrearUsuario_SinRolSuficiente() throws Exception {
        UsuarioRequestDTO dto = nuevoUsuario("Intruso", "intruso." + System.nanoTime() + "@comercio.com", "clave123", Rol.ROLE_DUENO);

        mockMvc.perform(post("/api/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("TC-12: Con login de repositor GET /api/productos no devuelve costo, margenGanancia ni porcentajeMargen; con login de dueño sí")
    void testOcultamientoDeCostosSegunRol() throws Exception {
        String tokenRepositor = obtenerToken("repositor@comercio.com", "123456");
        String tokenDueno = obtenerToken("dueno@comercio.com", "123456");

        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + tokenRepositor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").exists())
                .andExpect(jsonPath("$[0].precioVenta").exists())
                .andExpect(jsonPath("$", everyItem(not(hasKey("costo")))))
                .andExpect(jsonPath("$", everyItem(not(hasKey("margenGanancia")))))
                .andExpect(jsonPath("$", everyItem(not(hasKey("porcentajeMargen")))));

        mockMvc.perform(get("/api/productos")
                .header("Authorization", "Bearer " + tokenDueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", everyItem(hasKey("costo"))))
                .andExpect(jsonPath("$", everyItem(hasKey("margenGanancia"))))
                .andExpect(jsonPath("$", everyItem(hasKey("porcentajeMargen"))));
    }

    @Test
    @DisplayName("HU-05: Con login de repositor el motor de riesgo no devuelve valorEnRiesgo; con login de dueño sí")
    void testOcultamientoValorEnRiesgoSegunRol() throws Exception {
        String tokenRepositor = obtenerToken("repositor@comercio.com", "123456");
        String tokenDueno = obtenerToken("dueno@comercio.com", "123456");

        mockMvc.perform(get("/api/riesgo/evaluacion-general")
                .header("Authorization", "Bearer " + tokenRepositor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estadoRiesgo").exists())
                .andExpect(jsonPath("$", everyItem(not(hasKey("valorEnRiesgo")))));

        mockMvc.perform(get("/api/riesgo/ruta-diaria")
                .header("Authorization", "Bearer " + tokenRepositor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotalEnRiesgo").doesNotExist());

        mockMvc.perform(get("/api/riesgo/evaluacion-general")
                .header("Authorization", "Bearer " + tokenDueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", everyItem(hasKey("valorEnRiesgo"))));

        mockMvc.perform(get("/api/riesgo/ruta-diaria")
                .header("Authorization", "Bearer " + tokenDueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorTotalEnRiesgo").exists());
    }

    private String obtenerToken(String email, String password) throws Exception {
        String respuesta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login(email, password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("token").asText();
    }

    private AuthRequestDTO login(String email, String password) {
        AuthRequestDTO login = new AuthRequestDTO();
        login.setEmail(email);
        login.setPassword(password);
        return login;
    }

    private UsuarioRequestDTO nuevoUsuario(String nombre, String email, String password, Rol rol) {
        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNombre(nombre);
        dto.setEmail(email);
        dto.setPassword(password);
        dto.setRol(rol);
        return dto;
    }
}
