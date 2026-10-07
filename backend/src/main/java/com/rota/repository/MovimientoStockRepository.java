package com.rota.repository;

import com.rota.entity.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByLoteIdOrderByFechaHoraDesc(Long loteId);
}
