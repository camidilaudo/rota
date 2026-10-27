package com.rota.service.impl;

import com.rota.dto.response.ComparacionLineaBaseResponseDTO;
import com.rota.dto.response.ResumenMermaResponseDTO;
import com.rota.entity.CausaMerma;
import com.rota.entity.Comercio;
import com.rota.entity.Merma;
import com.rota.entity.Usuario;
import com.rota.exception.BadRequestException;
import com.rota.repository.MermaRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.security.SeguridadUtils;
import com.rota.service.MermaReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class MermaReporteServiceImpl implements MermaReporteService {

    private static final BigDecimal CIEN = new BigDecimal("100");
    private static final BigDecimal DIAS_POR_MES = new BigDecimal("30");
    private static final String SIN_CAUSA = "SIN_CAUSA";

    private final MermaRepository mermaRepository;
    private final UsuarioRepository usuarioRepository;

    @Override
    @Transactional(readOnly = true)
    public ResumenMermaResponseDTO obtenerResumen(LocalDate desde, LocalDate hasta, Long categoriaId, CausaMerma causa) {
        LocalDate inicio = desde != null ? desde : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        validarPeriodo(inicio, fin);

        List<Merma> mermas = mermasDelPeriodo(inicio, fin).stream()
                .filter(m -> categoriaId == null || categoriaId.equals(m.getProducto().getCategoria().getId()))
                .filter(m -> causa == null || causa == m.getCausa())
                .toList();

        BigDecimal total = sumarPerdida(mermas);

        List<ResumenMermaResponseDTO.Grupo> porCategoria = mermas.stream()
                .collect(Collectors.groupingBy(m -> m.getProducto().getCategoria().getId()))
                .values().stream()
                .map(lista -> armarGrupo(
                        String.valueOf(lista.get(0).getProducto().getCategoria().getId()),
                        lista.get(0).getProducto().getCategoria().getNombre(),
                        lista, total))
                .sorted(Comparator.comparing(ResumenMermaResponseDTO.Grupo::getPerdidaTotal).reversed())
                .toList();

        List<ResumenMermaResponseDTO.Grupo> porCausa = mermas.stream()
                .collect(Collectors.groupingBy(m -> m.getCausa() != null ? m.getCausa().name() : SIN_CAUSA))
                .entrySet().stream()
                .map(e -> armarGrupo(
                        e.getKey(),
                        SIN_CAUSA.equals(e.getKey()) ? "Sin causa registrada" : CausaMerma.valueOf(e.getKey()).getEtiqueta(),
                        e.getValue(), total))
                .sorted(Comparator.comparing(ResumenMermaResponseDTO.Grupo::getPerdidaTotal).reversed())
                .toList();

        Map<String, List<Merma>> agrupadoPorMes = mermas.stream()
                .collect(Collectors.groupingBy(m -> YearMonth.from(m.getFechaRegistro()).toString(), TreeMap::new, Collectors.toList()));
        List<ResumenMermaResponseDTO.Mes> porMes = agrupadoPorMes.entrySet().stream()
                .map(e -> ResumenMermaResponseDTO.Mes.builder()
                        .mes(e.getKey())
                        .unidades(sumarUnidades(e.getValue()))
                        .perdidaTotal(sumarPerdida(e.getValue()))
                        .build())
                .toList();

        return ResumenMermaResponseDTO.builder()
                .desde(inicio)
                .hasta(fin)
                .perdidaTotal(total)
                .unidadesTotales(sumarUnidades(mermas))
                .registros(mermas.size())
                .porCategoria(porCategoria)
                .porCausa(porCausa)
                .porMes(porMes)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ComparacionLineaBaseResponseDTO compararContraLineaBase(LocalDate desde, LocalDate hasta) {
        LocalDate inicio = desde != null ? desde : LocalDate.now().withDayOfMonth(1);
        LocalDate fin = hasta != null ? hasta : LocalDate.now();
        validarPeriodo(inicio, fin);

        BigDecimal mermaPeriodo = sumarPerdida(mermasDelPeriodo(inicio, fin));
        Comercio comercio = obtenerComercio();
        BigDecimal base = comercio != null ? comercio.getLineaBaseMermaMensual() : null;

        if (base == null) {
            return ComparacionLineaBaseResponseDTO.builder()
                    .desde(inicio)
                    .hasta(fin)
                    .hayLineaBase(false)
                    .mermaPeriodo(mermaPeriodo)
                    .mensaje("Todavía no se cargó la línea base de merma mensual del comercio.")
                    .build();
        }

        // La línea base es mensual: se prorratea a los días del período consultado (mes de 30 días)
        long dias = ChronoUnit.DAYS.between(inicio, fin) + 1;
        BigDecimal basePeriodo = base.multiply(BigDecimal.valueOf(dias)).divide(DIAS_POR_MES, 2, RoundingMode.HALF_UP);
        BigDecimal ahorro = basePeriodo.subtract(mermaPeriodo);
        BigDecimal porcentaje = basePeriodo.signum() == 0
                ? BigDecimal.ZERO
                : ahorro.multiply(CIEN).divide(basePeriodo, 2, RoundingMode.HALF_UP);
        boolean mejoro = ahorro.signum() > 0;

        String mensaje = mejoro
                ? "La merma del período es menor que la línea base: el ahorro estimado es de $" + ahorro.toPlainString() + "."
                : ahorro.signum() == 0
                        ? "La merma del período es igual a la línea base."
                        : "La merma del período supera la línea base en $" + ahorro.negate().toPlainString() + ".";

        return ComparacionLineaBaseResponseDTO.builder()
                .desde(inicio)
                .hasta(fin)
                .hayLineaBase(true)
                .lineaBaseMensual(base)
                .lineaBasePeriodo(basePeriodo)
                .mermaPeriodo(mermaPeriodo)
                .ahorroPesos(ahorro)
                .ahorroPorcentaje(porcentaje)
                .mejoro(mejoro)
                .mensaje(mensaje)
                .build();
    }

    // ---------- auxiliares ----------

    private void validarPeriodo(LocalDate desde, LocalDate hasta) {
        if (desde.isAfter(hasta)) {
            throw new BadRequestException("La fecha de inicio no puede ser posterior a la fecha de fin.");
        }
    }

    private List<Merma> mermasDelPeriodo(LocalDate desde, LocalDate hasta) {
        Comercio comercio = obtenerComercio();
        Long comercioId = comercio != null ? comercio.getId() : null;
        return mermaRepository.buscarEntreFechas(desde.atStartOfDay(), hasta.plusDays(1).atStartOfDay()).stream()
                .filter(m -> comercioId == null || perteneceAlComercio(m, comercioId))
                .toList();
    }

    private boolean perteneceAlComercio(Merma m, Long comercioId) {
        return m.getProducto().getCategoria().getComercio() != null
                && comercioId.equals(m.getProducto().getCategoria().getComercio().getId());
    }

    private Comercio obtenerComercio() {
        return usuarioRepository.findByEmail(SeguridadUtils.obtenerEmailAutenticado())
                .map(Usuario::getComercio)
                .orElse(null);
    }

    private ResumenMermaResponseDTO.Grupo armarGrupo(String clave, String nombre, List<Merma> mermas, BigDecimal totalGeneral) {
        BigDecimal perdida = sumarPerdida(mermas);
        BigDecimal porcentaje = totalGeneral.signum() == 0
                ? BigDecimal.ZERO
                : perdida.multiply(CIEN).divide(totalGeneral, 2, RoundingMode.HALF_UP);
        return ResumenMermaResponseDTO.Grupo.builder()
                .clave(clave)
                .nombre(nombre)
                .unidades(sumarUnidades(mermas))
                .perdidaTotal(perdida)
                .porcentaje(porcentaje)
                .build();
    }

    private BigDecimal sumarPerdida(List<Merma> mermas) {
        return mermas.stream().map(Merma::getPerdidaTotal).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private int sumarUnidades(List<Merma> mermas) {
        return mermas.stream().mapToInt(Merma::getCantidad).sum();
    }
}
