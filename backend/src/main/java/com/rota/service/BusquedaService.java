package com.rota.service;

import com.rota.dto.response.BusquedaProductoLotesResponseDTO;
import com.rota.entity.Ubicacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BusquedaService {
    Page<BusquedaProductoLotesResponseDTO> buscarProductosConLotes(String query, Ubicacion ubicacion, Pageable pageable);
}