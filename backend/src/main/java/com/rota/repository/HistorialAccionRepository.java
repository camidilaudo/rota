package com.rota.repository;

import com.rota.entity.HistorialAccion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface HistorialAccionRepository extends JpaRepository<HistorialAccion, Long> {
    List<HistorialAccion> findByLoteIdOrderByFechaHoraDesc(Long loteId);
}
