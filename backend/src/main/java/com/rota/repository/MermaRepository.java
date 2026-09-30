package com.rota.repository;

import com.rota.entity.Merma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MermaRepository extends JpaRepository<Merma, Long> {
    List<Merma> findAllByOrderByFechaRegistroDesc();
}