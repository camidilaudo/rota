package com.rota.unit.service;

import com.rota.dto.request.ProductoRequestDTO;
import com.rota.dto.response.BusquedaProductoLotesResponseDTO;
import com.rota.dto.response.ProductoResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Producto;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.LoteRepository;
import com.rota.repository.ProductoRepository;
import com.rota.service.impl.BusquedaServiceImpl;
import com.rota.service.impl.ProductoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SuppressWarnings("null")
@ExtendWith(MockitoExtension.class)
class ProductoServiceTest {

    @Mock
    private ProductoRepository productoRepository;

    @Mock
    private CategoriaRepository categoriaRepository;

    @Mock
    private LoteRepository loteRepository;

    @InjectMocks
    private ProductoServiceImpl productoService;

    @InjectMocks
    private BusquedaServiceImpl busquedaService;

    private Categoria categoria;
    private Producto producto;

    @BeforeEach
    void setUp() {
        categoria = Categoria.builder()
                .id(1L)
                .nombre("Lácteos")
                .diasUmbralCritico(7)
                .build();

        producto = Producto.builder()
                .id(10L)
                .nombre("Yogur Bebible")
                .codigoBarra("7791234567890")
                .costo(new BigDecimal("100.00"))
                .precioVenta(new BigDecimal("150.00"))
                .categoria(categoria)
                .build();
    }

    @Test
    @DisplayName("HU-02 & HU-03: Crear producto y verificar cálculo automático de margen y umbral")
    void testCrearProductoConMargenYUmbral() {
        ProductoRequestDTO request = new ProductoRequestDTO();
        request.setNombre("Yogur Bebible");
        request.setCodigoBarra("7791234567890");
        request.setCosto(new BigDecimal("100.00"));
        request.setPrecioVenta(new BigDecimal("150.00"));
        request.setCategoriaId(1L);

        when(productoRepository.existsByCodigoBarra("7791234567890")).thenReturn(false);
        when(categoriaRepository.findById(1L)).thenReturn(Optional.of(categoria));
        when(productoRepository.save(any(Producto.class))).thenReturn(producto);

        // Llamada corregida a productoService.crear(request)
        ProductoResponseDTO respuesta = productoService.crear(request);

        assertNotNull(respuesta);
        assertEquals(new BigDecimal("50.00"), respuesta.getMargenGanancia());
        assertEquals("Lácteos", respuesta.getCategoriaNombre());
        verify(productoRepository, times(1)).save(any(Producto.class));
    }

    @Test
    @DisplayName("HU-06: Buscar producto por código de barras para escaneo")
    void testBuscarPorCodigoBarraEscaneo() {
        when(productoRepository.findByCodigoBarra("7791234567890")).thenReturn(Optional.of(producto));

        ProductoResponseDTO respuesta = productoService.obtenerPorCodigoBarra("7791234567890");

        assertNotNull(respuesta);
        assertEquals("Yogur Bebible", respuesta.getNombre());
        assertEquals("7791234567890", respuesta.getCodigoBarra());
    }

    @Test
    @DisplayName("HU-09: Consulta rápida paginada de productos y lotes")
    void testBuscarProductosPaginados() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Producto> listaProductos = Collections.singletonList(producto);
        Page<Producto> page = new PageImpl<>(listaProductos, pageable, 1);

        when(productoRepository.buscarPorNombreOCodigo("Yogur", pageable)).thenReturn(page);
        when(loteRepository.findByProductoIdOrderByFechaVencimientoAsc(10L)).thenReturn(Collections.emptyList());

        Page<BusquedaProductoLotesResponseDTO> resultado = busquedaService.buscarProductosConLotes("Yogur", null, pageable);

        assertNotNull(resultado);
        assertEquals(1, resultado.getTotalElements());
        assertEquals("Yogur Bebible", resultado.getContent().get(0).getNombreProducto());
    }
}