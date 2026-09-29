package com.rota.service.impl;

import com.rota.dto.request.ProductoRequestDTO;
import com.rota.dto.response.ProductoResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Producto;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.ProductoRepository;
import com.rota.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;

    @Override
    @Transactional
    public ProductoResponseDTO crear(ProductoRequestDTO dto) {
        if (productoRepository.existsByCodigoBarra(dto.getCodigoBarra())) {
            throw new IllegalArgumentException("Ya existe un producto con el código de barras: " + dto.getCodigoBarra());
        }

        Categoria categoria = categoriaRepository.findById(dto.getCategoriaId())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con ID: " + dto.getCategoriaId()));

        Producto producto = Producto.builder()
                .codigoBarra(dto.getCodigoBarra().trim())
                .nombre(dto.getNombre().trim())
                .precioVenta(dto.getPrecioVenta())
                .costo(dto.getCosto())
                .categoria(categoria)
                .build();

        return mapToDTO(productoRepository.save(producto));
    }

    @Override
    @Transactional(readOnly = true)
    public ProductoResponseDTO obtenerPorCodigoBarra(String codigoBarra) {
        Producto producto = productoRepository.findByCodigoBarra(codigoBarra)
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código de barras: " + codigoBarra));
        return mapToDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    private ProductoResponseDTO mapToDTO(Producto p) {
        return ProductoResponseDTO.builder()
                .id(p.getId())
                .codigoBarra(p.getCodigoBarra())
                .nombre(p.getNombre())
                .precioVenta(p.getPrecioVenta())
                .costo(p.getCosto())
                .margenGanancia(p.getMargenGanancia())
                .porcentajeMargen(p.getPorcentajeMargen())
                .categoriaId(p.getCategoria().getId())
                .categoriaNombre(p.getCategoria().getNombre())
                .diasUmbralCriticoCategoria(p.getCategoria().getDiasUmbralCritico())
                .build();
    }
}