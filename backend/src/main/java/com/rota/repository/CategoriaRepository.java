package com.rota.repository;

import com.rota.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {
    Optional<Categoria> findByNombreIgnoreCase(String nombre);
    List<Categoria> findByEsPilotoTrueAndActivaTrue();
    List<Categoria> findByActivaTrue();
    List<Categoria> findByComercioIdAndEsPilotoTrue(Long comercioId);
}