package com.rota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.LoteRequestDTO;
import com.rota.entity.Ubicacion;
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

import java.time.LocalDate;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@SuppressWarnings("null")
@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class LoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(roles = "REPOSITOR")
    @DisplayName("HU-07 & HU-08: Registrar lote de producto exitosamente")
    void testRegistrarLoteAPI() throws Exception {
        LoteRequestDTO dto = new LoteRequestDTO();
        dto.setProductoId(1L);
        dto.setCantidad(20);
        dto.setFechaVencimiento(LocalDate.now().plusMonths(2));
        dto.setUbicacion(Ubicacion.DEPOSITO);

        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.cantidad").value(20))
                .andExpect(jsonPath("$.ubicacion").value("DEPOSITO"));
    }

    @Test
    @WithMockUser(roles = "REPOSITOR")
    @DisplayName("HU-13 & HU-14: Obtener la ruta diaria con semáforo de riesgo")
    void testObtenerRutaDiariaAPI() throws Exception {
        mockMvc.perform(get("/api/riesgo/ruta-diaria")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalLotesAtencion").exists())
                .andExpect(jsonPath("$.lotesPriorizados").isArray());
    }

    @Test
    @WithMockUser(roles = "REPOSITOR")
    @DisplayName("ROMPER: Registrar lote con cantidad negativa (-5)")
    void testRegistrarLote_CantidadNegativa() throws Exception {
        LoteRequestDTO dto = new LoteRequestDTO();
        dto.setProductoId(1L);
        dto.setCantidad(-5); // Cantidad inválida
        dto.setFechaVencimiento(LocalDate.now().plusMonths(2));
        dto.setUbicacion(Ubicacion.DEPOSITO);

        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Esperamos HTTP 400
    }

    @Test
    @WithMockUser(roles = "REPOSITOR")
    @DisplayName("ROMPER: Registrar lote con fecha de vencimiento en el pasado")
    void testRegistrarLote_FechaPasada() throws Exception {
        LoteRequestDTO dto = new LoteRequestDTO();
        dto.setProductoId(1L);
        dto.setCantidad(10);
        dto.setFechaVencimiento(LocalDate.now().minusDays(10)); // Vencido hace 10 días
        dto.setUbicacion(Ubicacion.DEPOSITO);

        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Esperamos HTTP 400
    }

    @Test
    @WithMockUser(roles = "REPOSITOR")
    @DisplayName("ROMPER: Registrar lote asociándolo a un Producto Inexistente (ID 999999)")
    void testRegistrarLote_ProductoInexistente() throws Exception {
        LoteRequestDTO dto = new LoteRequestDTO();
        dto.setProductoId(999999L); // ID que no existe en BD
        dto.setCantidad(10);
        dto.setFechaVencimiento(LocalDate.now().plusMonths(2));
        dto.setUbicacion(Ubicacion.DEPOSITO);

        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isNotFound()); // Esperamos HTTP 404
    }

    @Test
    @WithMockUser(roles = "REPOSITOR")
    @DisplayName("ROMPER: Registrar lote sin enviar datos obligatorios (Payload vacío)")
    void testRegistrarLote_PayloadVacio() throws Exception {
        LoteRequestDTO dto = new LoteRequestDTO(); // Campos en null

        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Esperamos HTTP 400
    }

    @Test
    @DisplayName("ROMPER ACCESO: Usuario no autenticado intenta registrar lote")
    void testRegistrarLote_SinAutenticacion() throws Exception {
        LoteRequestDTO dto = new LoteRequestDTO();
        dto.setProductoId(1L);
        dto.setCantidad(10);
        dto.setFechaVencimiento(LocalDate.now().plusMonths(2));
        dto.setUbicacion(Ubicacion.DEPOSITO);

        // Notar que NO lleva @WithMockUser
        mockMvc.perform(post("/api/lotes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized()); // Esperamos HTTP 401
    }

    
}