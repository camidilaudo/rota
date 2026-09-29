package com.rota.service;

import com.rota.dto.request.ProductoRequestDTO;
import com.rota.dto.response.ProductoResponseDTO;

import java.util.List;

public interface ProductoService {
    ProductoResponseDTO crear(ProductoRequestDTO dto);
    ProductoResponseDTO obtenerPorCodigoBarra(String codigoBarra);
    List<ProductoResponseDTO> obtenerTodos();
}