package com.rota.repository;

import com.rota.entity.Merma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface MermaRepository extends JpaRepository<Merma, Long> {
    List<Merma> findAllByOrderByFechaRegistroDesc();

    // HU-25: mermas del período con producto y categoría ya cargados, para agrupar sin consultas adicionales
    @Query("""
           select m from Merma m
           join fetch m.producto p
           join fetch p.categoria c
           where m.fechaRegistro >= :desde and m.fechaRegistro < :hasta
           order by m.fechaRegistro desc
           """)
    List<Merma> buscarEntreFechas(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}