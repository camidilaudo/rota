package com.rota.unit.service;

import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.response.LoteRevisadoResponseDTO;
import com.rota.dto.response.MermaResponseDTO;
import com.rota.entity.CausaMerma;
import com.rota.entity.Categoria;
import com.rota.entity.Lote;
import com.rota.entity.Merma;
import com.rota.entity.Producto;
import com.rota.entity.Rol;
import com.rota.entity.TipoAccion;
import com.rota.entity.Ubicacion;
import com.rota.entity.Usuario;
import com.rota.exception.ConflictException;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.LoteRepository;
import com.rota.repository.MermaRepository;
import com.rota.repository.MovimientoStockRepository;
import com.rota.repository.ProductoRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.service.HistorialAccionService;
import com.rota.service.VelocidadVentaService;
import com.rota.service.impl.OperacionStockServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** HU-23 (baja con causa) y HU-16 (marcar lote como revisado). */
@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class OperacionStockBajaRevisionTest {

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

    private Lote lote;
    private Usuario encargado;

    @BeforeEach
    void setUp() {
        Categoria categoria = Categoria.builder().id(1L).nombre("Lácteos").build();
        Producto producto = Producto.builder().id(10L).nombre("Yogur").costo(new BigDecimal("300.00")).categoria(categoria).build();
        lote = Lote.builder().id(5L).producto(producto).cantidad(10).ubicacion(Ubicacion.GONDOLA)
                .fechaVencimiento(LocalDate.now().plusDays(3)).fechaRecepcion(LocalDate.now().minusDays(1)).build();

        encargado = Usuario.builder().id(7L).nombre("Juan").email("repositor@comercio.com").rol(Rol.ROLE_REPOSITOR).build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(encargado, null, encargado.getAuthorities()));
        lenient().when(usuarioRepository.findByEmail("repositor@comercio.com")).thenReturn(Optional.of(encargado));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("HU-23: la baja guarda causa, lote y usuario, descuenta el stock y deja el detalle en el historial")
    void bajaConCausa() {
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote));
        when(mermaRepository.save(any(Merma.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrarMermaRequestDTO dto = new RegistrarMermaRequestDTO();
        dto.setLoteId(5L);
        dto.setCantidad(4);
        dto.setCausa(CausaMerma.DANIO);
        dto.setMotivo("se cayó la caja");

        MermaResponseDTO respuesta = service.registrarMerma(dto);

        ArgumentCaptor<Merma> captor = ArgumentCaptor.forClass(Merma.class);
        verify(mermaRepository).save(captor.capture());
        Merma guardada = captor.getValue();
        assertEquals(CausaMerma.DANIO, guardada.getCausa());
        assertSame(lote, guardada.getLote());
        assertSame(encargado, guardada.getUsuario());
        assertEquals("Daño: se cayó la caja", guardada.getMotivo());
        assertEquals(0, new BigDecimal("1200.00").compareTo(guardada.getPerdidaTotal()));

        assertEquals(6, lote.getCantidad());
        assertEquals(CausaMerma.DANIO, respuesta.getCausa());
        assertEquals(5L, respuesta.getLoteId());
        verify(historialAccionService).registrarAccion(eq(lote), eq(TipoAccion.BAJA), eq("Daño: se cayó la caja"), eq(encargado));
    }

    @Test
    @DisplayName("HU-23: sin detalle, el motivo queda con el nombre de la causa")
    void bajaSinDetalle() {
        when(loteRepository.findById(5L)).thenReturn(Optional.of(lote));
        when(mermaRepository.save(any(Merma.class))).thenAnswer(inv -> inv.getArgument(0));

        RegistrarMermaRequestDTO dto = new RegistrarMermaRequestDTO();
        dto.setLoteId(5L);
        dto.setCantidad(1);
        dto.setCausa(CausaMerma.VENCIMIENTO);

        MermaResponseDTO respuesta = service.registrarMerma(dto);

        assertEquals("Vencimiento", respuesta.getMotivo());
        verify(historialAccionService).registrarAccion(eq(lote), eq(TipoAccion.BAJA), eq("Vencimiento"), eq(encargado));
    }

    @Test
    @DisplayName("HU-16: marcar un lote como revisado registra usuario y hora")
    void marcarLoteRevisado() {
        when(loteRepository.findByIdParaActualizar(5L)).thenReturn(Optional.of(lote));

        LoteRevisadoResponseDTO respuesta = service.marcarLoteRevisado(5L);

        assertNotNull(lote.getRevisadoEn());
        assertSame(encargado, lote.getRevisadoPor());
        assertEquals(5L, respuesta.getLoteId());
        assertEquals("Yogur", respuesta.getProductoNombre());
        assertEquals("Juan", respuesta.getRevisadoPor());
        assertEquals(lote.getRevisadoEn(), respuesta.getRevisadoEn());
        verify(loteRepository).save(lote);
        verify(historialAccionService).registrarAccion(eq(lote), eq(TipoAccion.REVISION), isNull(), eq(encargado));
    }

    @Test
    @DisplayName("HU-16: un lote inexistente no se puede marcar como revisado")
    void marcarLoteInexistente() {
        when(loteRepository.findByIdParaActualizar(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.marcarLoteRevisado(99L));
    }

    @Test
    @DisplayName("HU-16: una segunda revisión el mismo día se rechaza y no pisa la primera")
    void marcarLoteRevisadoDosVecesElMismoDia() {
        Usuario otro = Usuario.builder().id(9L).nombre("Ana").email("ana@comercio.com").rol(Rol.ROLE_DUENO).build();
        LocalDateTime primera = LocalDate.now().atTime(8, 5);
        lote.setRevisadoEn(primera);
        lote.setRevisadoPor(otro);
        when(loteRepository.findByIdParaActualizar(5L)).thenReturn(Optional.of(lote));

        ConflictException ex = assertThrows(ConflictException.class, () -> service.marcarLoteRevisado(5L));

        assertEquals("Este lote ya fue revisado hoy por Ana a las 08:05", ex.getMessage());
        assertEquals(primera, lote.getRevisadoEn());
        assertSame(otro, lote.getRevisadoPor());
        verify(loteRepository, never()).save(any());
        verify(historialAccionService, never()).registrarAccion(any(), any(), any(), any());
    }

    @Test
    @DisplayName("HU-16: un lote revisado otro día se puede volver a revisar")
    void marcarLoteRevisadoAyerYHoy() {
        lote.setRevisadoEn(LocalDate.now().minusDays(1).atTime(18, 0));
        when(loteRepository.findByIdParaActualizar(5L)).thenReturn(Optional.of(lote));

        LoteRevisadoResponseDTO respuesta = service.marcarLoteRevisado(5L);

        assertEquals(LocalDate.now(), respuesta.getRevisadoEn().toLocalDate());
        assertSame(encargado, lote.getRevisadoPor());
        verify(historialAccionService).registrarAccion(eq(lote), eq(TipoAccion.REVISION), isNull(), eq(encargado));
    }
}
