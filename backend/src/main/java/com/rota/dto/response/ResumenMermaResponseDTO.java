package com.rota.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** HU-25: merma del período valorizada a costo, agrupada por categoría, causa y mes. */
@Data
@Builder
public class ResumenMermaResponseDTO {

    private LocalDate desde;
    private LocalDate hasta;
    private BigDecimal perdidaTotal;
    private Integer unidadesTotales;
    private Integer registros;
    private List<Grupo> porCategoria;
    private List<Grupo> porCausa;
    private List<Mes> porMes;

    @Data
    @Builder
    public static class Grupo {
        private String clave;
        private String nombre;
        private Integer unidades;
        private BigDecimal perdidaTotal;
        private BigDecimal porcentaje;
    }

    @Data
    @Builder
    public static class Mes {
        private String mes;
        private Integer unidades;
        private BigDecimal perdidaTotal;
    }
}
