package com.rota.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.TrasladoLoteRequestDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Lote;
import com.rota.entity.ModoNotificacion;
import com.rota.entity.MovimientoStock;
import com.rota.entity.Producto;
import com.rota.entity.TipoMovimiento;
import com.rota.entity.Ubicacion;
import com.rota.entity.Usuario;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.LoteRepository;
import com.rota.repository.MovimientoStockRepository;
import com.rota.repository.ProductoRepository;
import com.rota.repository.UsuarioRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
class TrasladoLoteIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private MovimientoStockRepository movimientoStockRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("HU-12: El traslado de depósito a góndola registra el usuario autenticado y la fecha del movimiento")
    void testTrasladoRegistraUsuarioYFecha() throws Exception {
        Lote loteDeposito = crearLoteEnDeposito(12);

        TrasladoLoteRequestDTO request = new TrasladoLoteRequestDTO();
        request.setLoteOrigenId(loteDeposito.getId());
        request.setCantidadATrasladar(4);

        LocalDateTime antes = LocalDateTime.now().minusSeconds(1);

        // La respuesta del endpoint se mantiene igual: el lote resultante en góndola
        mockMvc.perform(post("/api/rotacion/trasladar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ubicacion").value("GONDOLA"))
                .andExpect(jsonPath("$.cantidad").value(4))
                .andExpect(jsonPath("$.productoId").value(loteDeposito.getProducto().getId()));

        LocalDateTime despues = LocalDateTime.now().plusSeconds(1);

        List<MovimientoStock> movimientos = movimientoStockRepository.findByLoteIdOrderByFechaHoraDesc(loteDeposito.getId());
        assertEquals(1, movimientos.size());

        MovimientoStock movimiento = movimientos.get(0);
        Usuario repositor = usuarioRepository.findByEmail("repositor@comercio.com").orElseThrow();

        assertEquals(TipoMovimiento.TRASLADO, movimiento.getTipo());
        assertEquals(4, movimiento.getCantidad());
        assertEquals(Ubicacion.DEPOSITO, movimiento.getUbicacionOrigen());
        assertEquals(Ubicacion.GONDOLA, movimiento.getUbicacionDestino());
        assertEquals(repositor.getId(), movimiento.getUsuario().getId());
        assertNotNull(movimiento.getFechaHora());
        assertFalse(movimiento.getFechaHora().isBefore(antes));
        assertTrue(movimiento.getFechaHora().isBefore(despues));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("HU-12: El traslado total también queda registrado con el usuario autenticado")
    void testTrasladoTotalRegistraMovimiento() throws Exception {
        Lote loteDeposito = crearLoteEnDeposito(6);

        TrasladoLoteRequestDTO request = new TrasladoLoteRequestDTO();
        request.setLoteOrigenId(loteDeposito.getId());
        request.setCantidadATrasladar(6);

        mockMvc.perform(post("/api/rotacion/trasladar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(loteDeposito.getId()))
                .andExpect(jsonPath("$.ubicacion").value("GONDOLA"));

        List<MovimientoStock> movimientos = movimientoStockRepository.findByLoteIdOrderByFechaHoraDesc(loteDeposito.getId());
        assertEquals(1, movimientos.size());
        Usuario dueno = usuarioRepository.findByEmail("dueno@comercio.com").orElseThrow();
        assertEquals(dueno.getId(), movimientos.get(0).getUsuario().getId());
        assertEquals(6, movimientos.get(0).getCantidad());
        assertNotNull(movimientos.get(0).getFechaHora());
    }

    private Lote crearLoteEnDeposito(int cantidad) {
        String sufijo = String.valueOf(System.nanoTime());
        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Bebidas HU-12 " + sufijo)
                .diasUmbralAlerta(10)
                .diasUmbralCritico(3)
                .modoNotificacion(ModoNotificacion.DIARIO)
                .build());

        Producto producto = productoRepository.save(Producto.builder()
                .codigoBarra("HU12" + sufijo)
                .nombre("Agua Mineral 2L")
                .costo(new BigDecimal("300.00"))
                .precioVenta(new BigDecimal("550.00"))
                .categoria(categoria)
                .build());

        return loteRepository.save(Lote.builder()
                .producto(producto)
                .cantidad(cantidad)
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .ubicacion(Ubicacion.DEPOSITO)
                .build());
    }
}
