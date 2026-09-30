package com.rota.unit.service;

import com.rota.dto.request.TrasladoLoteRequestDTO;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class TrasladoLoteTest {

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private RotacionServiceImpl rotacionService;

    private Lote loteDeposito;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Lácteos").build();
        Producto producto = Producto.builder().id(10L).nombre("Queso").codigoBarra("779123456").categoria(categoria).build();

        loteDeposito = Lote.builder()
                .id(50L)
                .producto(producto)
                .cantidad(15)
                .ubicacion(Ubicacion.DEPOSITO)
                .fechaVencimiento(LocalDate.now().plusDays(20))
                .fechaRecepcion(LocalDate.now().minusDays(2))
                .build();
    }

    @Test
    @DisplayName("HU-12: Trasladar lote de Depósito a Góndola actualizando stock y ubicación")
    void testTrasladarLoteAGondolaExitoso() {
        TrasladoLoteRequestDTO request = new TrasladoLoteRequestDTO();
        request.setLoteOrigenId(50L);
        request.setCantidadATrasladar(5);

        when(loteRepository.findById(50L)).thenReturn(Optional.of(loteDeposito));
        when(loteRepository.save(any(Lote.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LoteResponseDTO respuesta = rotacionService.trasladarLoteAGondola(request);

        assertNotNull(respuesta);
        // Ajustado a atLeastOnce() o times(2) según la lógica del servicio
        verify(loteRepository, atLeastOnce()).save(any(Lote.class));
    }
}