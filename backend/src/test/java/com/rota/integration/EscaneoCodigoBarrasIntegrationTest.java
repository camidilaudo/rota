package com.rota.integration;

import com.rota.entity.Categoria;
import com.rota.entity.Producto;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.ProductoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
@ActiveProfiles("test") 
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class EscaneoCodigoBarrasIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @BeforeEach
    void setUp() {
        if (!productoRepository.existsByCodigoBarra("7791234567890")) {
            // Se asegura de tener al menos una categoría existente
            Categoria cat = categoriaRepository.findAll().stream().findFirst().orElseGet(() -> 
                categoriaRepository.save(Categoria.builder().nombre("General").build())
            );

            productoRepository.save(Producto.builder()
                    .codigoBarra("7791234567890")
                    .nombre("Producto Test")
                    .costo(new BigDecimal("100.00"))
                    .precioVenta(new BigDecimal("150.00"))
                    .categoria(cat)
                    .build());
        }
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("HU-06: Búsqueda exitosa por código de barras existente")
    void testBuscarProductoPorCodigoBarra_Exitoso() throws Exception {
        String codigoExistente = "7791234567890";

        mockMvc.perform(get("/api/productos/codigo/" + codigoExistente))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.codigoBarra").value(codigoExistente));
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("HU-06: Código inexistente retorna 404 Not Found")
    void testBuscarProductoPorCodigoBarra_Inexistente() throws Exception {
        String codigoInexistente = "0000000000000";

        mockMvc.perform(get("/api/productos/codigo/" + codigoInexistente))
                .andExpect(status().isNotFound());
    }
}