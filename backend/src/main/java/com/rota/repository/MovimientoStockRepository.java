package com.rota.repository;

import com.rota.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDateTime;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByLoteIdOrderByFechaHoraDesc(Long loteId);

    // HU-15 / HU-20: ventas acumuladas de un producto desde una fecha, para calcular la velocidad de venta
    @Query("SELECT COALESCE(SUM(m.cantidad), 0) FROM MovimientoStock m " +
           "WHERE m.tipo = com.rota.entity.TipoMovimiento.VENTA " +
           "AND m.lote.producto.id = :productoId AND m.fechaHora >= :desde")
    Integer sumarVentasPorProductoDesde(@Param("productoId") Long productoId, @Param("desde") LocalDateTime desde);
}
