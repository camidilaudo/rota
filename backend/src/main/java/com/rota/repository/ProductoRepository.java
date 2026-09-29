package com.rota.repository;

import com.rota.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    Optional<Producto> findByCodigoBarra(String codigoBarra);
    boolean existsByCodigoBarra(String codigoBarra);
}