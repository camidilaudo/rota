package com.rota.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusquedaProductoLotesResponseDTO {
    private Long productoId;
    private String codigoBarra;
    private String nombreProducto;
    private String categoriaNombre;
    private Integer stockTotal;
    private List<LoteResponseDTO> lotes;
}