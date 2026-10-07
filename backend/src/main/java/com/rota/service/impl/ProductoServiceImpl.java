package com.rota.service.impl;

import com.rota.dto.request.ProductoRequestDTO;
import com.rota.dto.response.ProductoResponseDTO;
import com.rota.entity.Categoria;
import com.rota.entity.Producto;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.ProductoRepository;
import com.rota.security.SeguridadUtils;
import com.rota.service.ProductoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
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
        Producto producto = productoRepository.findByCodigoBarra(codigoBarra.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con código de barras: " + codigoBarra));
        return mapToDTO(producto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductoResponseDTO> obtenerTodos() {
        return productoRepository.findAll().stream().map(this::mapToDTO).toList();
    }

    private ProductoResponseDTO mapToDTO(Producto p) {
        // HU-05: el usuario operativo (ROLE_REPOSITOR) no visualiza costos ni márgenes
        boolean ocultarFinancieros = SeguridadUtils.esRepositor();
        return ProductoResponseDTO.builder()
                .id(p.getId())
                .codigoBarra(p.getCodigoBarra())
                .nombre(p.getNombre())
                .precioVenta(p.getPrecioVenta())
                .costo(ocultarFinancieros ? null : p.getCosto())
                .margenGanancia(ocultarFinancieros ? null : p.getMargenGanancia())
                .porcentajeMargen(ocultarFinancieros ? null : p.getPorcentajeMargen())
                .categoriaId(p.getCategoria().getId())
                .categoriaNombre(p.getCategoria().getNombre())
                .diasUmbralCriticoCategoria(p.getCategoria().getDiasUmbralCritico())
                .build();
    }
}