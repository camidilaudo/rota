package com.rota.unit.service;

import com.rota.dto.response.ComparacionLineaBaseResponseDTO;
import com.rota.dto.response.ResumenMermaResponseDTO;
import com.rota.entity.CausaMerma;
import com.rota.entity.Categoria;
import com.rota.entity.Comercio;
import com.rota.entity.Merma;
import com.rota.entity.Producto;
import com.rota.entity.Rol;
import com.rota.entity.Usuario;
import com.rota.exception.BadRequestException;
import com.rota.repository.MermaRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.service.impl.MermaReporteServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

/** HU-25 (panel de merma por categoría) y HU-26 (comparación contra la línea base). */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class MermaReporteServiceTest {

    @Mock
    private MermaRepository mermaRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private MermaReporteServiceImpl service;

    private Comercio comercio;
    private List<Merma> mermas;

    @BeforeEach
    void setUp() {
        comercio = Comercio.builder().id(1L).nombre("Almacén Don Carlos").build();
        Usuario dueno = Usuario.builder().id(1L).nombre("Carlos").email("dueno@comercio.com")
                .rol(Rol.ROLE_DUENO).comercio(comercio).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(dueno, null, dueno.getAuthorities()));
        lenient().when(usuarioRepository.findByEmail("dueno@comercio.com")).thenReturn(Optional.of(dueno));

        Categoria lacteos = Categoria.builder().id(1L).nombre("Lácteos").comercio(comercio).build();
        Categoria panaderia = Categoria.builder().id(2L).nombre("Panadería").comercio(comercio).build();
        Producto leche = Producto.builder().id(10L).nombre("Leche").categoria(lacteos).build();
        Producto yogur = Producto.builder().id(11L).nombre("Yogur").categoria(lacteos).build();
        Producto pan = Producto.builder().id(12L).nombre("Pan").categoria(panaderia).build();

        mermas = List.of(
                merma(leche, 3, "900.00", CausaMerma.VENCIMIENTO, LocalDateTime.of(2026, 10, 10, 9, 0)),
                merma(yogur, 2, "1200.00", CausaMerma.DANIO, LocalDateTime.of(2026, 10, 12, 9, 0)),
                merma(pan, 4, "900.00", CausaMerma.VENCIMIENTO, LocalDateTime.of(2026, 10, 15, 9, 0)));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private Merma merma(Producto producto, int cantidad, String perdida, CausaMerma causa, LocalDateTime fecha) {
        Merma m = Merma.builder()
                .producto(producto)
                .cantidad(cantidad)
                .motivo(causa.getEtiqueta())
                .causa(causa)
                .costoUnitario(new BigDecimal("100.00"))
                .perdidaTotal(new BigDecimal(perdida))
                .build();
        m.setFechaRegistro(fecha);
        return m;
    }

    private static boolean igual(String esperado, BigDecimal real) {
        return new BigDecimal(esperado).compareTo(real) == 0;
    }

    @Test
    @DisplayName("HU-25: suma la merma del período a valor costo y la agrupa por categoría, causa y mes")
    void resumenAgrupaPorCategoriaCausaYMes() {
        when(mermaRepository.buscarEntreFechas(any(), any())).thenReturn(mermas);

        ResumenMermaResponseDTO r = service.obtenerResumen(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null, null);

        assertTrue(igual("3000.00", r.getPerdidaTotal()));
        assertEquals(9, r.getUnidadesTotales());
        assertEquals(3, r.getRegistros());

        assertEquals(2, r.getPorCategoria().size());
        assertEquals("Lácteos", r.getPorCategoria().get(0).getNombre());
        assertTrue(igual("2100.00", r.getPorCategoria().get(0).getPerdidaTotal()));
        assertTrue(igual("70.00", r.getPorCategoria().get(0).getPorcentaje()));
        assertEquals("Panadería", r.getPorCategoria().get(1).getNombre());

        assertEquals(2, r.getPorCausa().size());
        assertEquals("Vencimiento", r.getPorCausa().get(0).getNombre());
        assertTrue(igual("1800.00", r.getPorCausa().get(0).getPerdidaTotal()));

        assertEquals(1, r.getPorMes().size());
        assertEquals("2026-10", r.getPorMes().get(0).getMes());
        assertTrue(igual("3000.00", r.getPorMes().get(0).getPerdidaTotal()));
    }

    @Test
    @DisplayName("HU-25: permite filtrar por categoría y por causa de baja")
    void resumenFiltraPorCategoriaYCausa() {
        when(mermaRepository.buscarEntreFechas(any(), any())).thenReturn(mermas);

        ResumenMermaResponseDTO soloPanaderia = service.obtenerResumen(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), 2L, null);
        assertTrue(igual("900.00", soloPanaderia.getPerdidaTotal()));
        assertEquals(1, soloPanaderia.getRegistros());

        ResumenMermaResponseDTO soloDanio = service.obtenerResumen(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 31), null, CausaMerma.DANIO);
        assertTrue(igual("1200.00", soloDanio.getPerdidaTotal()));
        assertEquals(1, soloDanio.getRegistros());
    }

    @Test
    @DisplayName("HU-25: un período invertido se rechaza")
    void resumenRechazaPeriodoInvertido() {
        // la validación ocurre antes de consultar la base
        assertThrows(BadRequestException.class,
                () -> service.obtenerResumen(LocalDate.of(2026, 10, 31), LocalDate.of(2026, 10, 1), null, null));
    }

    @Test
    @DisplayName("HU-26: compara contra la línea base mensual y calcula el ahorro en pesos y porcentaje")
    void comparaContraLineaBase() {
        comercio.setLineaBaseMermaMensual(new BigDecimal("6000.00"));
        when(mermaRepository.buscarEntreFechas(any(), any())).thenReturn(mermas);

        // 30 días de período = un mes de referencia, así que la línea base del período es la mensual
        ComparacionLineaBaseResponseDTO r = service.compararContraLineaBase(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 30));

        assertTrue(r.getHayLineaBase());
        assertTrue(igual("6000.00", r.getLineaBasePeriodo()));
        assertTrue(igual("3000.00", r.getMermaPeriodo()));
        assertTrue(igual("3000.00", r.getAhorroPesos()));
        assertTrue(igual("50.00", r.getAhorroPorcentaje()));
        assertTrue(r.getMejoro());
    }

    @Test
    @DisplayName("HU-26: si la merma supera la línea base, la diferencia es negativa")
    void mermaMayorQueLaLineaBase() {
        comercio.setLineaBaseMermaMensual(new BigDecimal("2000.00"));
        when(mermaRepository.buscarEntreFechas(any(), any())).thenReturn(mermas);

        ComparacionLineaBaseResponseDTO r = service.compararContraLineaBase(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 30));

        assertTrue(igual("-1000.00", r.getAhorroPesos()));
        assertTrue(igual("-50.00", r.getAhorroPorcentaje()));
        assertFalse(r.getMejoro());
    }

    @Test
    @DisplayName("HU-26: sin línea base cargada no hay comparación y se avisa")
    void sinLineaBase() {
        when(mermaRepository.buscarEntreFechas(any(), any())).thenReturn(mermas);

        ComparacionLineaBaseResponseDTO r = service.compararContraLineaBase(LocalDate.of(2026, 10, 1), LocalDate.of(2026, 10, 30));

        assertFalse(r.getHayLineaBase());
        assertNull(r.getAhorroPesos());
        assertTrue(igual("3000.00", r.getMermaPeriodo()));
    }
}
