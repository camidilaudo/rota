package com.rota.repository;

import com.rota.entity.Producto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface ProductoRepository extends JpaRepository<Producto, Long> {

    boolean existsByCodigoBarra(String codigoBarra);

    // Búsqueda por código de barras exacto
    Optional<Producto> findByCodigoBarra(String codigoBarra);

    // Búsqueda flexible por nombre o código de barras
    @Query("SELECT p FROM Producto p " +
           "WHERE (:query IS NULL OR :query = '' OR " +
           "LOWER(p.nombre) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "p.codigoBarra = :query)")
    Page<Producto> buscarPorNombreOCodigo(@Param("query") String query, Pageable pageable);
}