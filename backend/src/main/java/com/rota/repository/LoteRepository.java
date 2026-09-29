package com.rota.repository;

import com.rota.entity.Lote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoteRepository extends JpaRepository<Lote, Long> {

    List<Lote> findByProductoIdOrderByFechaVencimientoAsc(Long productoId);

    List<Lote> findByProductoCodigoBarraOrderByFechaVencimientoAsc(String codigoBarra);

    @Query("SELECT l FROM Lote l " +
           "JOIN FETCH l.producto p " +
           "JOIN FETCH p.categoria c " +
           "WHERE l.cantidad > 0 " +
           "ORDER BY l.ubicacion ASC, l.fechaVencimiento ASC")
    List<Lote> findAllLotesConProductoYCategoria();
}