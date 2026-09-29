package com.rota.repository;

import com.rota.entity.Merma;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MermaRepository extends JpaRepository<Merma, Long> {
    List<Merma> findAllByOrderByFechaRegistroDesc();
}