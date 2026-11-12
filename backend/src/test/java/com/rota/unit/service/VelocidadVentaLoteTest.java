package com.rota.unit.service;

import com.rota.dto.response.VelocidadVentaLoteResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.MovimientoStock;
import com.rota.entity.Producto;
import com.rota.entity.TipoMovimiento;
import com.rota.entity.Ubicacion;
import com.rota.repository.LoteRepository;
import com.rota.repository.MermaRepository;
import com.rota.repository.MovimientoStockRepository;
import com.rota.repository.ProductoRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.service.HistorialAccionService;
import com.rota.service.VelocidadVentaService;
import com.rota.service.impl.OperacionStockServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/** Gráfico de velocidad de venta del lote: ventas diarias contra la velocidad necesaria. */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class VelocidadVentaLoteTest {

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

    private Lote lote(int cantidad, LocalDate vence) {
        Producto producto = Producto.builder().id(10L).nombre("Yogur").costo(new BigDecimal("300.00")).build();
        return Lote.builder().id(5L).producto(producto).cantidad(cantidad).ubicacion(Ubicacion.GONDOLA)
                .fechaVencimiento(vence).fechaRecepcion(LocalDate.now().minusDays(20)).build();
    }

    private MovimientoStock venta(LocalDate dia, int cantidad) {
        return MovimientoStock.builder().tipo(TipoMovimiento.VENTA).cantidad(cantidad).fechaHora(dia.atTime(11, 30)).build();
    }

    @Test
    @DisplayName("agrupa las ventas por día, completa con 0 los días sin ventas y calcula la velocidad necesaria")
    void ventasDiariasYVelocidadNecesaria() {
        LocalDate hoy = LocalDate.now();
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote(20, hoy.plusDays(10))));
        when(movimientoStockRepository.findByTipoAndLote_Producto_IdAndFechaHoraGreaterThanEqual(eq(TipoMovimiento.VENTA), eq(10L), any()))
                .thenReturn(List.of(venta(hoy, 2), venta(hoy, 3), venta(hoy.minusDays(2), 1)));
        when(velocidadVentaService.calcularRotacionDiaria(10L)).thenReturn(new BigDecimal("0.4286"));

        VelocidadVentaLoteResponseDTO r = service.obtenerVelocidadVentaLote(5L);

        assertEquals(14, r.getVentasPorDia().size());
        assertEquals(hoy.minusDays(13), r.getVentasPorDia().get(0).getFecha());
        assertEquals(hoy, r.getVentasPorDia().get(13).getFecha());
        assertEquals(5, r.getVentasPorDia().get(13).getUnidades());
        assertEquals(0, r.getVentasPorDia().get(12).getUnidades());
        assertEquals(1, r.getVentasPorDia().get(11).getUnidades());
        assertEquals(0, new BigDecimal("2").compareTo(r.getVelocidadVentaDiariaNecesaria()));
        assertEquals(new BigDecimal("0.4286"), r.getVelocidadVentaDiariaActual());
        assertEquals(10, r.getDiasHastaVencimiento());
    }

    @Test
    @DisplayName("un lote vencido no tiene velocidad necesaria")
    void loteVencidoSinVelocidadNecesaria() {
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote(20, LocalDate.now().minusDays(1))));
        when(movimientoStockRepository.findByTipoAndLote_Producto_IdAndFechaHoraGreaterThanEqual(any(), any(), any())).thenReturn(List.of());
        when(velocidadVentaService.calcularRotacionDiaria(10L)).thenReturn(BigDecimal.ZERO);

        assertNull(service.obtenerVelocidadVentaLote(5L).getVelocidadVentaDiariaNecesaria());
    }
}
