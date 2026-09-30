package com.rota.repository;

import com.rota.entity.Lote;
import com.rota.entity.Ubicacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByProductoIdOrderByFechaVencimientoAsc(Long productoId);

    List<Lote> findByProductoCodigoBarraOrderByFechaVencimientoAsc(String codigoBarra);

    // Método requerido por MotorRiesgoServiceImpl para evaluar todos los lotes
    @Query("SELECT l FROM Lote l JOIN FETCH l.producto p JOIN FETCH p.categoria")
    List<Lote> findAllLotesConProductoYCategoria();

    @Query("SELECT l FROM Lote l " +
           "JOIN FETCH l.producto p " +
           "WHERE l.cantidad > 0 AND " +
           "(LOWER(p.nombre) LIKE LOWER(CONCAT('%', :filtro, '%')) OR p.codigoBarra = :filtro) " +
           "ORDER BY l.fechaVencimiento ASC")
    List<Lote> buscarLotesActivosPorProductoFiltro(@Param("filtro") String filtro);

    List<Lote> findByProductoIdAndCantidadGreaterThanOrderByFechaVencimientoAsc(Long productoId, Integer cantidad);

    Optional<Lote> findFirstByProductoIdAndUbicacionAndCantidadGreaterThanOrderByFechaVencimientoAsc(
            Long productoId, Ubicacion ubicacion, Integer cantidad);

    @Query("SELECT l FROM Lote l " +
           "JOIN FETCH l.producto p " +
           "WHERE l.ubicacion = :ubicacion AND l.cantidad > 0 " +
           "ORDER BY l.fechaVencimiento ASC")
    List<Lote> findLotesActivosPorUbicacion(@Param("ubicacion") Ubicacion ubicacion);
}