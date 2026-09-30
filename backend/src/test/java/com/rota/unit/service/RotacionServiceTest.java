package com.rota.unit.service;

import com.rota.dto.response.AlertaRotacionResponseDTO;
import com.rota.dto.response.LoteResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Lote;
import com.rota.entity.Producto;
import com.rota.entity.Ubicacion;
import com.rota.repository.LoteRepository;
import com.rota.service.impl.RotacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RotacionServiceTest {

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private RotacionServiceImpl rotacionService;

    private Producto productoPrueba;
    private Lote loteGondola;
    private Lote loteDeposito;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Lácteos").build();
        productoPrueba = Producto.builder().id(10L).nombre("Leche Entera").codigoBarra("779000100").categoria(categoria).build();

        // Lote en Góndola que vence en 10 días
        loteGondola = Lote.builder()
                .id(100L)
                .producto(productoPrueba)
                .cantidad(5)
                .ubicacion(Ubicacion.GONDOLA)
                .fechaVencimiento(LocalDate.now().plusDays(10))
                .build();

        // Lote en Depósito que vence en 3 días (Vence ANTES -> Debe generar alerta)
        loteDeposito = Lote.builder()
                .id(101L)
                .producto(productoPrueba)
                .cantidad(10)
                .ubicacion(Ubicacion.DEPOSITO)
                .fechaVencimiento(LocalDate.now().plusDays(3))
                .build();
    }

    @Test
    @DisplayName("HU-10: Debe retornar los lotes ordenados por FEFO")
    void testObtenerLotesPorFefo() {
        when(loteRepository.findByProductoIdAndCantidadGreaterThanOrderByFechaVencimientoAsc(10L, 0))
                .thenReturn(List.of(loteDeposito, loteGondola));

        List<LoteResponseDTO> resultado = rotacionService.obtenerLotesPorFefo(10L);

        assertNotNull(resultado);
        assertEquals(2, resultado.size());
        assertEquals(101L, resultado.get(0).getId()); // Primero el de vencimiento más próximo
        verify(loteRepository, times(1)).findByProductoIdAndCantidadGreaterThanOrderByFechaVencimientoAsc(10L, 0);
    }

    @Test
    @DisplayName("HU-11: Debe detectar alerta cuando depósito vence antes que góndola")
    void testEvaluarAlertasRotacionIncorrecta() {
        when(loteRepository.findLotesActivosPorUbicacion(Ubicacion.GONDOLA)).thenReturn(List.of(loteGondola));
        when(loteRepository.findLotesActivosPorUbicacion(Ubicacion.DEPOSITO)).thenReturn(List.of(loteDeposito));

        List<AlertaRotacionResponseDTO> alertas = rotacionService.evaluarAlertasRotacionIncorrecta();

        assertFalse(alertas.isEmpty());
        assertEquals(1, alertas.size());
        assertEquals(101L, alertas.get(0).getLoteDepositoId());
        assertEquals(100L, alertas.get(0).getLoteGondolaId());
    }
}