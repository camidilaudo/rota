package com.rota.unit.service;

import com.rota.dto.response.ComparacionEscenariosResponseDTO;
import com.rota.dto.response.DescuentoSugeridoResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Comercio;
import com.rota.entity.Lote;
import com.rota.entity.Producto;
import com.rota.entity.Ubicacion;
import com.rota.repository.LoteRepository;
import com.rota.repository.MermaRepository;
import com.rota.repository.MovimientoStockRepository;
import com.rota.repository.ProductoRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.service.HistorialAccionService;
import com.rota.service.VelocidadVentaService;
import com.rota.service.impl.OperacionStockServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

/**
 * HU-17, HU-18 y HU-19: el descuento sugerido y la comparación de escenarios recomiendan lo mismo.
 * Caso de la demo: queso cremoso, 8 unidades que vencen en 40 días, costo 3.500, precio 5.200 y piso de margen 20%.
 */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class DescuentoRecomendacionTest {

    @Mock
    private LoteRepository loteRepository;
    @Mock
    private MermaRepository mermaRepository;
    @Mock
    private ProductoRepository productoRepository;
    @Mock
    private MovimientoStockRepository movimientoStockRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private VelocidadVentaService velocidadVentaService;
    @Mock
    private HistorialAccionService historialAccionService;

    @InjectMocks
    private OperacionStockServiceImpl service;

    @BeforeEach
    void setUp() {
        Comercio comercio = Comercio.builder().id(1L).nombre("Almacén Don Carlos").margenMinimoPorcentaje(new BigDecimal("20.00")).build();
        Categoria categoria = Categoria.builder().id(3L).nombre("Fiambres y Embutidos").comercio(comercio).build();
        Producto queso = Producto.builder().id(4L).nombre("Queso Cremoso 1kg")
                .costo(new BigDecimal("3500.00")).precioVenta(new BigDecimal("5200.00")).categoria(categoria).build();
        Lote lote = Lote.builder().id(5L).producto(queso).cantidad(8)
                .fechaVencimiento(LocalDate.now().plusDays(40)).ubicacion(Ubicacion.GONDOLA).build();
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote));
    }

    @Test
    @DisplayName("Con ventas lentas, ni el tope del piso de margen compensa: la recomendación es no rebajar en las dos pantallas")
    void topeQueNoCompensaRecomiendaNoRebajar() {
        when(velocidadVentaService.calcularRotacionDiaria(4L)).thenReturn(new BigDecimal("0.1429"));

        DescuentoSugeridoResponseDTO sugerido = service.calcularDescuentoSugerido(5L);
        ComparacionEscenariosResponseDTO escenarios = service.compararEscenarios(5L);

        assertTrue(sugerido.getDescuentoInsuficientePorMargen());
        assertEquals(0, new BigDecimal("19.23").compareTo(sugerido.getPorcentajeDescuentoRecomendado()));
        assertFalse(sugerido.getConvieneAplicar());
        assertTrue(sugerido.getMensaje().startsWith("Conviene no rebajar"));
        assertEquals("SIN_DESCUENTO", escenarios.getRecomendacion());
        assertTrue(escenarios.getEscenarioConDescuento().getMargenTotalProyectado()
                .compareTo(escenarios.getEscenarioSinDescuento().getMargenTotalProyectado()) < 0);
    }

    @Test
    @DisplayName("Sin ventas, rebajar dentro del piso de margen deja más margen que no hacer nada: conviene aplicarlo")
    void sinVentasConvieneRebajar() {
        when(velocidadVentaService.calcularRotacionDiaria(4L)).thenReturn(BigDecimal.ZERO);

        DescuentoSugeridoResponseDTO sugerido = service.calcularDescuentoSugerido(5L);
        ComparacionEscenariosResponseDTO escenarios = service.compararEscenarios(5L);

        assertTrue(sugerido.getConvieneAplicar());
        assertEquals("CON_DESCUENTO", escenarios.getRecomendacion());
    }
}
