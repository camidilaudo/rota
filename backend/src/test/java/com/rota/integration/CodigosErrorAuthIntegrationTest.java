package com.rota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.AuthRequestDTO;
import com.rota.dto.request.UsuarioRequestDTO;
import com.rota.entity.Rol;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

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
class CodigosErrorAuthIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("DEF-01: Login con contraseña incorrecta devuelve 401 y no 500")
    void testLoginContrasenaIncorrecta() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login("dueno@comercio.com", "incorrecta"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Email o contraseña incorrectos"));
    }

    @Test
    @DisplayName("DEF-01: Repositor con token real que llama a POST /api/usuarios recibe 403")
    void testRepositorCreaUsuario_AccesoDenegado() throws Exception {
        String tokenRepositor = obtenerToken("repositor@comercio.com", "123456");

        UsuarioRequestDTO dto = new UsuarioRequestDTO();
        dto.setNombre("Intruso");
        dto.setEmail("intruso.def01." + System.nanoTime() + "@comercio.com");
        dto.setPassword("clave123");
        dto.setRol(Rol.ROLE_REPOSITOR);

        mockMvc.perform(post("/api/usuarios")
                .header("Authorization", "Bearer " + tokenRepositor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
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
}
