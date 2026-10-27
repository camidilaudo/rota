package com.rota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.AuthRequestDTO;
import com.rota.entity.Lote;
import com.rota.repository.HistorialAccionRepository;
import com.rota.repository.LoteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.junit.jupiter.api.Assertions.assertEquals;
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
class RevisionLoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private HistorialAccionRepository historialAccionRepository;

    @Autowired
    private TransactionTemplate transactionTemplate;

    private Long loteQuesoId;
    private Long lotePanId;

    @BeforeEach
    void setUp() {
        loteQuesoId = loteIdPorCodigo("7790002000025");
        lotePanId = loteIdPorCodigo("7790002000018");
        // Cada test arranca con los dos lotes sin revisar
        transactionTemplate.executeWithoutResult(status -> {
            for (Long id : List.of(loteQuesoId, lotePanId)) {
                Lote lote = loteRepository.findById(id).orElseThrow();
                lote.setRevisadoEn(null);
                lote.setRevisadoPor(null);
                loteRepository.save(lote);
            }
        });
    }

    @Test
    @DisplayName("HU-16: la segunda revisión del día devuelve 409, conserva la primera y queda en el historial")
    void testRevisionDuplicadaDevuelveConflicto() throws Exception {
        String tokenRepositor = obtenerToken("repositor@comercio.com");
        String tokenDueno = obtenerToken("dueno@comercio.com");
        long historialPrevio = contarRevisiones(loteQuesoId);

        mockMvc.perform(post("/api/operaciones/lotes/" + loteQuesoId + "/revisado")
                .header("Authorization", "Bearer " + tokenRepositor))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.revisadoPor").value("Juan Repositor"));

        mockMvc.perform(post("/api/operaciones/lotes/" + loteQuesoId + "/revisado")
                .header("Authorization", "Bearer " + tokenDueno))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value(startsWith("Este lote ya fue revisado hoy por Juan Repositor a las ")));

        String revisor = transactionTemplate.execute(s -> loteRepository.findById(loteQuesoId).orElseThrow().getRevisadoPor().getNombre());
        assertEquals("Juan Repositor", revisor);
        assertEquals(historialPrevio + 1, contarRevisiones(loteQuesoId));

        mockMvc.perform(get("/api/operaciones/lotes/" + loteQuesoId + "/historial")
                .header("Authorization", "Bearer " + tokenDueno))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipoAccion == 'REVISION')]", hasSize((int) (historialPrevio + 1))))
                .andExpect(jsonPath("$[0].tipoAccion").value("REVISION"))
                .andExpect(jsonPath("$[0].usuarioNombre").value("Juan Repositor"));
    }

    @Test
    @DisplayName("HU-16: dos revisiones simultáneas del mismo lote registran solo una")
    void testRevisionesSimultaneas() throws Exception {
        String tokenRepositor = obtenerToken("repositor@comercio.com");
        String tokenDueno = obtenerToken("dueno@comercio.com");
        long historialPrevio = contarRevisiones(lotePanId);

        CountDownLatch largada = new CountDownLatch(1);
        ExecutorService pool = Executors.newFixedThreadPool(2);
        List<Future<Integer>> resultados = new ArrayList<>();
        for (String token : List.of(tokenRepositor, tokenDueno)) {
            Callable<Integer> pedido = () -> {
                largada.await();
                return mockMvc.perform(post("/api/operaciones/lotes/" + lotePanId + "/revisado")
                        .header("Authorization", "Bearer " + token))
                        .andReturn().getResponse().getStatus();
            };
            resultados.add(pool.submit(pedido));
        }
        largada.countDown();
        List<Integer> codigos = new ArrayList<>();
        for (Future<Integer> f : resultados) codigos.add(f.get());
        pool.shutdown();

        assertEquals(1, codigos.stream().filter(c -> c == 200).count(), "códigos: " + codigos);
        assertEquals(1, codigos.stream().filter(c -> c == 409).count(), "códigos: " + codigos);
        assertEquals(historialPrevio + 1, contarRevisiones(lotePanId));
    }

    private long contarRevisiones(Long loteId) {
        return transactionTemplate.execute(s -> historialAccionRepository.findAll().stream()
                .filter(h -> h.getLote().getId().equals(loteId) && h.getTipoAccion() == com.rota.entity.TipoAccion.REVISION)
                .count());
    }

    private Long loteIdPorCodigo(String codigoBarra) {
        return loteRepository.findByProductoCodigoBarraOrderByFechaVencimientoAsc(codigoBarra).get(0).getId();
    }

    private String obtenerToken(String email) throws Exception {
        AuthRequestDTO login = new AuthRequestDTO();
        login.setEmail(email);
        login.setPassword("123456");
        String respuesta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(respuesta).get("token").asText();
    }
}
