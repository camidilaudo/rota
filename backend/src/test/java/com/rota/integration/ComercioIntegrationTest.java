package com.rota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.CategoriaRequestDTO;
import com.rota.entity.ModoNotificacion;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
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
class ComercioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("HU-01: Dar de alta categoría piloto inicial")
    void testAltaCategoriaPiloto() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Lácteos Piloto");
        
        // ¡Agregar el campo obligatorio!
        dto.setDiasUmbralAlerta(7); 
        dto.setDiasUmbralCritico(3); 
        
        dto.setModoNotificacion(ModoNotificacion.DIARIO);
        dto.setEsPiloto(false);

        mockMvc.perform(post("/api/categorias")
                .with(csrf()) // IMPORTANTE si usas Spring Security
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Lácteos Piloto"));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("Configurar notificación de categoría")
    void testConfigurarNotificacionCategoria() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Fiambrería");
        
        // ¡Agregar el campo obligatorio!
        dto.setDiasUmbralAlerta(5); 
        dto.setDiasUmbralCritico(2); 
        
        dto.setModoNotificacion(ModoNotificacion.PANTALLA);
        dto.setEsPiloto(false);

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Fiambrería"));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("FALLO: Intentar crear categoría con datos nulos o umbral crítico inválido")
    void testCrearCategoria_DatosInvalidos() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(""); // Nombre vacío
        dto.setDiasUmbralAlerta(2);
        dto.setDiasUmbralCritico(5); // Umbral crítico mayor que el de alerta (violación de regla de negocio)

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Espera HTTP 400
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("FALLO: Repositor intenta crear una categoría (Sin Autorización)")
    void testCrearCategoria_SinRolSuficiente() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Bebidas");
        dto.setDiasUmbralAlerta(5);
        dto.setDiasUmbralCritico(2);
        dto.setModoNotificacion(ModoNotificacion.DIARIO);

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()); // Espera HTTP 403 Forbidden
    }

    @Test
    @DisplayName("FALLO: Usuario no autenticado intenta acceder a la API de categorías")
    void testCrearCategoria_SinAutenticacion() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Golosinas");
        dto.setDiasUmbralAlerta(5);
        dto.setDiasUmbralCritico(2);

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized()); // Espera HTTP 401 Unauthorized
    }
}