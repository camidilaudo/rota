package com.rota.integration;

import com.rota.entity.Categoria;
import com.rota.entity.Lote;
import com.rota.entity.ModoNotificacion;
import com.rota.entity.Producto;
import com.rota.entity.Ubicacion;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.LoteRepository;
import com.rota.repository.ProductoRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class BusquedaOrdenIntegrationTest {

    @Autowired
    private ProductoRepository productoRepository;

    @Autowired
    private LoteRepository loteRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    private Producto producto(String nombre, String codigo, Categoria categoria) {
        return productoRepository.save(Producto.builder()
                .codigoBarra(codigo)
                .nombre(nombre)
                .costo(new BigDecimal("100.00"))
                .precioVenta(new BigDecimal("150.00"))
                .categoria(categoria)
                .build());
    }

    private void lote(Producto producto, LocalDate recepcion) {
        loteRepository.save(Lote.builder()
                .producto(producto)
                .cantidad(10)
                .fechaVencimiento(LocalDate.now().plusDays(30))
                .fechaRecepcion(recepcion)
                .ubicacion(Ubicacion.DEPOSITO)
                .build());
    }

    @Test
    @DisplayName("La búsqueda ordena los productos por la recepción de su lote más reciente; sin lotes van al final")
    void ordenaPorUltimaCarga() {
        Categoria categoria = categoriaRepository.save(Categoria.builder().nombre("Orden carga").diasUmbralAlerta(7).diasUmbralCritico(3).modoNotificacion(ModoNotificacion.values()[0]).build());
        Producto viejo = producto("OrdenCarga Viejo", "9990000000011", categoria);
        Producto nuevo = producto("OrdenCarga Nuevo", "9990000000028", categoria);
        producto("OrdenCarga SinLotes", "9990000000035", categoria);
        lote(viejo, LocalDate.of(2026, 1, 1));
        lote(nuevo, LocalDate.of(2026, 3, 1));
        lote(viejo, LocalDate.of(2026, 2, 1));

        List<String> nombres = productoRepository.buscarPorNombreOCodigo("OrdenCarga", PageRequest.of(0, 10))
                .map(Producto::getNombre).getContent();

        assertThat(nombres).containsExactly("OrdenCarga Nuevo", "OrdenCarga Viejo", "OrdenCarga SinLotes");
    }
}
