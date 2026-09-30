package com.rota.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ConsultaVencimientosIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("HU-09: Búsqueda rápida de lotes por nombre de producto")
    void testBuscarLotesPorNombre_Exitoso() throws Exception {
        mockMvc.perform(get("/api/lotes/buscar").param("filtro", "Leche"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("HU-09: Búsqueda rápida de lotes por código de barras")
    void testBuscarLotesPorCodigoBarra_Exitoso() throws Exception {
        mockMvc.perform(get("/api/lotes/buscar").param("filtro", "7791234567890"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}