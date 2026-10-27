package com.rota.service.impl;

import com.rota.dto.request.AplicarDescuentoRequestDTO;
import com.rota.dto.request.RegistrarMermaRequestDTO;
import com.rota.dto.request.RegistrarVentaRequestDTO;
import com.rota.dto.response.ComparacionEscenariosResponseDTO;
import com.rota.dto.response.DescuentoAplicadoResponseDTO;
import com.rota.dto.response.DescuentoSugeridoResponseDTO;
import com.rota.dto.response.EscenarioProyectadoDTO;
import com.rota.dto.response.HistorialAccionResponseDTO;
import com.rota.dto.response.LoteAfectadoVentaDTO;
import com.rota.dto.response.LoteRevisadoResponseDTO;
import com.rota.dto.response.MermaResponseDTO;
import com.rota.dto.response.VentaResponseDTO;
import com.rota.entity.Lote;
import com.rota.entity.Merma;
import com.rota.entity.MovimientoStock;
import com.rota.entity.Producto;
import com.rota.entity.TipoAccion;
import com.rota.entity.TipoMovimiento;
import com.rota.entity.Usuario;
import com.rota.exception.BadRequestException;
import com.rota.exception.ConflictException;
import com.rota.exception.ResourceNotFoundException;
import com.rota.repository.LoteRepository;
import com.rota.repository.MermaRepository;
import com.rota.repository.MovimientoStockRepository;
import com.rota.repository.ProductoRepository;
import com.rota.repository.UsuarioRepository;
import com.rota.security.SeguridadUtils;
import com.rota.service.HistorialAccionService;
import com.rota.service.OperacionStockService;
import com.rota.service.VelocidadVentaService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class OperacionStockServiceImpl implements OperacionStockService {

    // HU-19: piso de margen por defecto cuando el comercio todavía no configuró uno propio
    private static final BigDecimal MARGEN_MINIMO_POR_DEFECTO = new BigDecimal("15.00");

    // Rango de descuento válido para el sistema (coincide con AplicarDescuentoRequestDTO)
    private static final BigDecimal DESCUENTO_MAXIMO_SISTEMA = new BigDecimal("90");

    private final LoteRepository loteRepository;
    private final MermaRepository mermaRepository;
    private final ProductoRepository productoRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final UsuarioRepository usuarioRepository;
    private final VelocidadVentaService velocidadVentaService;
    private final HistorialAccionService historialAccionService;

    @Override
    @Transactional
    public DescuentoAplicadoResponseDTO aplicarDescuento(AplicarDescuentoRequestDTO dto) {
        Lote lote = loteRepository.findById(dto.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + dto.getLoteId()));

        Producto producto = lote.getProducto();
        BigDecimal precioOriginal = producto.getPrecioVenta();
        BigDecimal factorDescuento = BigDecimal.ONE.subtract(
                dto.getPorcentajeDescuento().divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)
        );
        BigDecimal precioConDescuento = precioOriginal.multiply(factorDescuento).setScale(2, RoundingMode.HALF_UP);

        // HU-19: ningún descuento puede perforar el piso de margen configurado
        BigDecimal margenMinimo = obtenerMargenMinimoPorcentaje(producto);
        BigDecimal precioMinimoPermitido = calcularPrecioMinimoPermitido(producto, margenMinimo);
        if (precioConDescuento.compareTo(precioMinimoPermitido) < 0) {
            BigDecimal descuentoMaximoPermitido = calcularDescuentoMaximoPorMargen(producto, precioMinimoPermitido);
            throw new BadRequestException(String.format(
                    "El descuento del %s%% perfora el piso de margen mínimo (%s%%) configurado para el comercio. " +
                    "Precio mínimo permitido: $%s. Descuento máximo permitido para este producto: %s%%.",
                    dto.getPorcentajeDescuento(), margenMinimo, precioMinimoPermitido, descuentoMaximoPermitido));
        }

        lote.setPorcentajeDescuento(dto.getPorcentajeDescuento());
        loteRepository.save(lote);

        // HU-22: trazabilidad de quién aplicó el descuento, cuándo y por qué
        Usuario usuario = obtenerUsuarioAutenticado();
        String motivo = (dto.getMotivo() != null && !dto.getMotivo().isBlank())
                ? dto.getMotivo().trim()
                : "Descuento aplicado manualmente";
        historialAccionService.registrarAccion(lote, TipoAccion.DESCUENTO, motivo, usuario);

        return DescuentoAplicadoResponseDTO.builder()
                .loteId(lote.getId())
                .productoNombre(lote.getProducto().getNombre())
                .precioOriginal(precioOriginal)
                .porcentajeDescuento(dto.getPorcentajeDescuento())
                .precioConDescuento(precioConDescuento)
                .mensaje("Descuento del " + dto.getPorcentajeDescuento() + "% aplicado correctamente al lote")
                .build();
    }

    @Override
    @Transactional
    public MermaResponseDTO registrarMerma(RegistrarMermaRequestDTO dto) {
        Lote lote = loteRepository.findById(dto.getLoteId())
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + dto.getLoteId()));

        if (dto.getCantidad() > lote.getCantidad()) {
            throw new IllegalArgumentException("La cantidad a mermar (" + dto.getCantidad() +
                    ") supera el stock disponible en el lote (" + lote.getCantidad() + ")");
        }

        Producto producto = lote.getProducto();
        BigDecimal costoUnitario = producto.getCosto();
        BigDecimal perdidaTotal = costoUnitario.multiply(BigDecimal.valueOf(dto.getCantidad()));

        // Descontamos las unidades del lote
        lote.setCantidad(lote.getCantidad() - dto.getCantidad());
        loteRepository.save(lote);

        // HU-23: la causa es obligatoria; el detalle es opcional y se agrega a la causa
        String detalle = dto.getMotivo() != null ? dto.getMotivo().trim() : "";
        String motivoCompleto = detalle.isEmpty()
                ? dto.getCausa().getEtiqueta()
                : dto.getCausa().getEtiqueta() + ": " + detalle;
        Usuario usuario = obtenerUsuarioAutenticado();

        // Registramos la merma monetaria
        Merma merma = Merma.builder()
                .producto(producto)
                .lote(lote)
                .causa(dto.getCausa())
                .usuario(usuario)
                .cantidad(dto.getCantidad())
                .motivo(motivoCompleto)
                .costoUnitario(costoUnitario)
                .perdidaTotal(perdidaTotal)
                .build();

        Merma mermaGuardada = mermaRepository.save(merma);

        // HU-22: trazabilidad de la baja
        historialAccionService.registrarAccion(lote, TipoAccion.BAJA, motivoCompleto, usuario);

        return mapToDTO(mermaGuardada);
    }

    @Override
    @Transactional
    public LoteRevisadoResponseDTO marcarLoteRevisado(Long loteId) {
        Lote lote = loteRepository.findByIdParaActualizar(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));

        // Una sola revisión por lote y por día: la primera no se pisa
        if (lote.getRevisadoEn() != null && lote.getRevisadoEn().toLocalDate().equals(LocalDate.now())) {
            String revisor = lote.getRevisadoPor() != null ? lote.getRevisadoPor().getNombre() : "otro usuario";
            throw new ConflictException(String.format("Este lote ya fue revisado hoy por %s a las %s",
                    revisor, lote.getRevisadoEn().format(DateTimeFormatter.ofPattern("HH:mm"))));
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        lote.setRevisadoEn(LocalDateTime.now());
        lote.setRevisadoPor(usuario);
        loteRepository.save(lote);
        historialAccionService.registrarAccion(lote, TipoAccion.REVISION, null, usuario);

        return LoteRevisadoResponseDTO.builder()
                .loteId(lote.getId())
                .productoNombre(lote.getProducto().getNombre())
                .revisadoEn(lote.getRevisadoEn())
                .revisadoPor(usuario.getNombre())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<MermaResponseDTO> obtenerHistorialMermas() {
        return mermaRepository.findAllByOrderByFechaRegistroDesc()
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    @Override
    @Transactional
    public VentaResponseDTO registrarVenta(RegistrarVentaRequestDTO dto) {
        Producto producto = productoRepository.findById(dto.getProductoId())
                .orElseThrow(() -> new ResourceNotFoundException("Producto no encontrado con ID: " + dto.getProductoId()));

        // HU-20: el stock se descuenta por orden FEFO (primero vence, primero sale)
        List<Lote> lotesFefo = loteRepository.findByProductoIdAndCantidadGreaterThanOrderByFechaVencimientoAsc(
                dto.getProductoId(), 0);

        int stockDisponible = lotesFefo.stream().mapToInt(Lote::getCantidad).sum();
        if (dto.getCantidad() > stockDisponible) {
            throw new BadRequestException(String.format(
                    "Stock insuficiente para vender %d unidad(es) de '%s'. Stock disponible: %d unidad(es).",
                    dto.getCantidad(), producto.getNombre(), stockDisponible));
        }

        Usuario usuario = obtenerUsuarioAutenticado();
        int restante = dto.getCantidad();
        List<LoteAfectadoVentaDTO> lotesAfectados = new ArrayList<>();

        for (Lote lote : lotesFefo) {
            if (restante <= 0) break;
            int aDescontar = Math.min(restante, lote.getCantidad());

            lote.setCantidad(lote.getCantidad() - aDescontar);
            loteRepository.save(lote);

            // Historial de consumo diario: cada venta queda registrada como MovimientoStock de tipo VENTA
            movimientoStockRepository.save(MovimientoStock.builder()
                    .lote(lote)
                    .tipo(TipoMovimiento.VENTA)
                    .cantidad(aDescontar)
                    .ubicacionOrigen(lote.getUbicacion())
                    .ubicacionDestino(lote.getUbicacion())
                    .usuario(usuario)
                    .build());

            lotesAfectados.add(LoteAfectadoVentaDTO.builder()
                    .loteId(lote.getId())
                    .cantidadDescontada(aDescontar)
                    .cantidadRestanteEnLote(lote.getCantidad())
                    .build());

            restante -= aDescontar;
        }

        // Impacta el motor de cobertura (HU-15): la rotación diaria queda actualizada con esta venta
        BigDecimal rotacionActualizada = velocidadVentaService.calcularRotacionDiaria(producto.getId());

        return VentaResponseDTO.builder()
                .productoId(producto.getId())
                .productoNombre(producto.getNombre())
                .cantidadVendida(dto.getCantidad())
                .lotesAfectados(lotesAfectados)
                .rotacionDiariaActualizada(rotacionActualizada)
                .mensaje(String.format("Venta registrada: %d unidad(es) de '%s' descontadas de %d lote(s) por orden FEFO.",
                        dto.getCantidad(), producto.getNombre(), lotesAfectados.size()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DescuentoSugeridoResponseDTO calcularDescuentoSugerido(Long loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        Producto producto = lote.getProducto();

        int diasRestantes = (int) ChronoUnit.DAYS.between(LocalDate.now(), lote.getFechaVencimiento());
        BigDecimal margenMinimo = obtenerMargenMinimoPorcentaje(producto);
        BigDecimal precioMinimoPermitido = calcularPrecioMinimoPermitido(producto, margenMinimo);
        BigDecimal descuentoMaximoPorMargen = calcularDescuentoMaximoPorMargen(producto, precioMinimoPermitido);
        BigDecimal velocidadActual = velocidadVentaService.calcularRotacionDiaria(producto.getId());

        if (diasRestantes <= 0) {
            return DescuentoSugeridoResponseDTO.builder()
                    .loteId(lote.getId())
                    .productoNombre(producto.getNombre())
                    .cantidad(lote.getCantidad())
                    .diasHastaVencimiento(diasRestantes)
                    .velocidadVentaDiariaActual(velocidadActual)
                    .velocidadVentaDiariaNecesaria(null)
                    .porcentajeDescuentoSugerido(BigDecimal.ZERO)
                    .margenMinimoPorcentaje(margenMinimo)
                    .porcentajeDescuentoMaximoPorMargen(descuentoMaximoPorMargen)
                    .porcentajeDescuentoRecomendado(BigDecimal.ZERO)
                    .descuentoInsuficientePorMargen(false)
                    .mensaje("El lote ya está vencido: no corresponde aplicar un descuento, se recomienda registrar la baja.")
                    .build();
        }

        BigDecimal velocidadNecesaria = BigDecimal.valueOf(lote.getCantidad())
                .divide(BigDecimal.valueOf(diasRestantes), 4, RoundingMode.HALF_UP);

        BigDecimal porcentajeSugerido = calcularPorcentajeSugerido(velocidadActual, velocidadNecesaria);

        boolean insuficiente = porcentajeSugerido.compareTo(descuentoMaximoPorMargen) > 0;
        BigDecimal porcentajeRecomendado = insuficiente ? descuentoMaximoPorMargen : porcentajeSugerido;

        String mensaje;
        if (porcentajeSugerido.compareTo(BigDecimal.ZERO) == 0) {
            mensaje = "No es necesario aplicar un descuento: al ritmo de venta actual, el lote se agota antes de vencer.";
        } else if (insuficiente) {
            mensaje = String.format(
                    "Para liquidar el lote a tiempo haría falta un %s%% de descuento, pero el piso de margen (%s%%) solo permite hasta un %s%%. " +
                    "El descuento recomendado puede no alcanzar a vender todo el lote antes del vencimiento.",
                    porcentajeSugerido, margenMinimo, descuentoMaximoPorMargen);
        } else {
            mensaje = String.format("Se recomienda un descuento del %s%% para vender el lote completo antes de la fecha de vencimiento.",
                    porcentajeRecomendado);
        }

        return DescuentoSugeridoResponseDTO.builder()
                .loteId(lote.getId())
                .productoNombre(producto.getNombre())
                .cantidad(lote.getCantidad())
                .diasHastaVencimiento(diasRestantes)
                .velocidadVentaDiariaActual(velocidadActual)
                .velocidadVentaDiariaNecesaria(velocidadNecesaria)
                .porcentajeDescuentoSugerido(porcentajeSugerido)
                .margenMinimoPorcentaje(margenMinimo)
                .porcentajeDescuentoMaximoPorMargen(descuentoMaximoPorMargen)
                .porcentajeDescuentoRecomendado(porcentajeRecomendado)
                .descuentoInsuficientePorMargen(insuficiente)
                .mensaje(mensaje)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public ComparacionEscenariosResponseDTO compararEscenarios(Long loteId) {
        Lote lote = loteRepository.findById(loteId)
                .orElseThrow(() -> new ResourceNotFoundException("Lote no encontrado con ID: " + loteId));
        Producto producto = lote.getProducto();
        int cantidad = lote.getCantidad();
        int diasRestantes = Math.max(0, (int) ChronoUnit.DAYS.between(LocalDate.now(), lote.getFechaVencimiento()));

        BigDecimal velocidadActual = velocidadVentaService.calcularRotacionDiaria(producto.getId());
        BigDecimal velocidadNecesaria = diasRestantes > 0
                ? BigDecimal.valueOf(cantidad).divide(BigDecimal.valueOf(diasRestantes), 4, RoundingMode.HALF_UP)
                : BigDecimal.valueOf(cantidad);

        // Escenario SIN descuento: proyección al precio de lista con la velocidad de venta actual
        EscenarioProyectadoDTO sinDescuento = proyectarEscenario(
                producto, cantidad, diasRestantes, velocidadActual, BigDecimal.ZERO, producto.getPrecioVenta());

        // Escenario CON descuento: usa la recomendación de HU-17/HU-19 (ya topeada por el piso de margen)
        DescuentoSugeridoResponseDTO sugerido = calcularDescuentoSugerido(loteId);
        BigDecimal pctDescuento = sugerido.getPorcentajeDescuentoRecomendado();
        BigDecimal precioFinal = producto.getPrecioVenta()
                .multiply(BigDecimal.ONE.subtract(pctDescuento.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal velocidadLograda = estimarVelocidadLograda(velocidadActual, velocidadNecesaria, pctDescuento);

        EscenarioProyectadoDTO conDescuento = proyectarEscenario(
                producto, cantidad, diasRestantes, velocidadLograda, pctDescuento, precioFinal);

        boolean convieneDescuento = pctDescuento.compareTo(BigDecimal.ZERO) > 0
                && conDescuento.getMargenTotalProyectado().compareTo(sinDescuento.getMargenTotalProyectado()) >= 0;
        String recomendacion = convieneDescuento ? "CON_DESCUENTO" : "SIN_DESCUENTO";
        String mensaje = convieneDescuento
                ? String.format("Aplicar el descuento del %s%% proyecta un mejor resultado ($%s) que no hacer nada ($%s).",
                        pctDescuento, conDescuento.getMargenTotalProyectado(), sinDescuento.getMargenTotalProyectado())
                : String.format("No aplicar descuento proyecta un mejor resultado ($%s) que descontar un %s%% ($%s).",
                        sinDescuento.getMargenTotalProyectado(), pctDescuento, conDescuento.getMargenTotalProyectado());

        return ComparacionEscenariosResponseDTO.builder()
                .loteId(lote.getId())
                .productoNombre(producto.getNombre())
                .cantidad(cantidad)
                .diasHastaVencimiento(diasRestantes)
                .escenarioSinDescuento(sinDescuento)
                .escenarioConDescuento(conDescuento)
                .recomendacion(recomendacion)
                .mensaje(mensaje)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HistorialAccionResponseDTO> obtenerHistorialDeLote(Long loteId) {
        if (!loteRepository.existsById(loteId)) {
            throw new ResourceNotFoundException("Lote no encontrado con ID: " + loteId);
        }
        return historialAccionService.obtenerHistorialPorLote(loteId);
    }

    // ---------------------------------------------------------------------
    // Helpers privados
    // ---------------------------------------------------------------------

    private Usuario obtenerUsuarioAutenticado() {
        return usuarioRepository.findByEmail(SeguridadUtils.obtenerEmailAutenticado())
                .orElseThrow(() -> new BadRequestException("No se pudo identificar al usuario autenticado para registrar la operación."));
    }

    // HU-19: margen mínimo configurado a nivel comercio; si todavía no fue configurado, se usa un valor por defecto
    private BigDecimal obtenerMargenMinimoPorcentaje(Producto producto) {
        if (producto.getCategoria() != null
                && producto.getCategoria().getComercio() != null
                && producto.getCategoria().getComercio().getMargenMinimoPorcentaje() != null) {
            return producto.getCategoria().getComercio().getMargenMinimoPorcentaje();
        }
        return MARGEN_MINIMO_POR_DEFECTO;
    }

    private BigDecimal calcularPrecioMinimoPermitido(Producto producto, BigDecimal margenMinimo) {
        return producto.getCosto()
                .multiply(BigDecimal.ONE.add(margenMinimo.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calcularDescuentoMaximoPorMargen(Producto producto, BigDecimal precioMinimoPermitido) {
        BigDecimal precioVenta = producto.getPrecioVenta();
        if (precioVenta.compareTo(precioMinimoPermitido) <= 0) {
            return BigDecimal.ZERO;
        }
        // Este valor es un TOPE: redondear hacia arriba (HALF_UP) podía devolver, por ej., 26.67%
        // cuando el límite real era 26.666...%, y aplicar ese 26.67% perforaba el piso por centavos.
        // CEILING en el cociente + FLOOR en el resultado final garantizan que el % devuelto nunca
        // supere el límite real, así siempre es seguro aplicarlo tal cual.
        BigDecimal ratio = precioMinimoPermitido.divide(precioVenta, 6, RoundingMode.CEILING);
        return BigDecimal.ONE.subtract(ratio)
                .multiply(BigDecimal.valueOf(100))
                .setScale(2, RoundingMode.FLOOR);
    }

    // HU-17: supone elasticidad simple 1:1 (cada 1% de descuento incrementa ~1% la velocidad de venta)
    private BigDecimal calcularPorcentajeSugerido(BigDecimal velocidadActual, BigDecimal velocidadNecesaria) {
        if (velocidadActual.compareTo(velocidadNecesaria) >= 0) {
            return BigDecimal.ZERO;
        }
        if (velocidadActual.compareTo(BigDecimal.ZERO) <= 0) {
            // Sin historial de ventas: se recomienda el máximo descuento permitido por el sistema
            return DESCUENTO_MAXIMO_SISTEMA;
        }
        BigDecimal incremento = velocidadNecesaria.divide(velocidadActual, 4, RoundingMode.HALF_UP).subtract(BigDecimal.ONE);
        BigDecimal porcentaje = incremento.multiply(BigDecimal.valueOf(100)).setScale(2, RoundingMode.HALF_UP);
        if (porcentaje.compareTo(DESCUENTO_MAXIMO_SISTEMA) > 0) return DESCUENTO_MAXIMO_SISTEMA;
        if (porcentaje.compareTo(BigDecimal.ZERO) < 0) return BigDecimal.ZERO;
        return porcentaje;
    }

    private EscenarioProyectadoDTO proyectarEscenario(Producto producto, int cantidad, int diasRestantes,
                                                        BigDecimal velocidadDiaria, BigDecimal pctDescuento, BigDecimal precioFinal) {
        int unidadesVendidas = Math.min(cantidad,
                velocidadDiaria.multiply(BigDecimal.valueOf(diasRestantes)).setScale(0, RoundingMode.DOWN).intValue());
        int unidadesDescartadas = cantidad - unidadesVendidas;

        BigDecimal margenUnitario = precioFinal.subtract(producto.getCosto());
        BigDecimal margenGanancia = margenUnitario.multiply(BigDecimal.valueOf(unidadesVendidas)).setScale(2, RoundingMode.HALF_UP);
        BigDecimal perdidaPorMerma = producto.getCosto().multiply(BigDecimal.valueOf(unidadesDescartadas)).setScale(2, RoundingMode.HALF_UP);

        return EscenarioProyectadoDTO.builder()
                .porcentajeDescuento(pctDescuento)
                .precioFinal(precioFinal)
                .unidadesEstimadasVendidas(unidadesVendidas)
                .unidadesEstimadasDescartadas(unidadesDescartadas)
                .margenGananciaEstimado(margenGanancia)
                .perdidaPorMermaEstimada(perdidaPorMerma)
                .margenTotalProyectado(margenGanancia.subtract(perdidaPorMerma))
                .build();
    }

    // HU-18: cuando no hay historial de ventas, se asume una mejora proporcional al esfuerzo de descuento aplicado,
    // tomando el descuento máximo del sistema como el que lograría vender todo el lote a tiempo
    private BigDecimal estimarVelocidadLograda(BigDecimal velocidadActual, BigDecimal velocidadNecesaria, BigDecimal pctAplicado) {
        if (velocidadActual.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal factor = BigDecimal.ONE.add(pctAplicado.divide(BigDecimal.valueOf(100), 4, RoundingMode.HALF_UP));
            return velocidadActual.multiply(factor);
        }
        if (pctAplicado.compareTo(BigDecimal.ZERO) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal proporcion = pctAplicado.divide(DESCUENTO_MAXIMO_SISTEMA, 4, RoundingMode.HALF_UP);
        if (proporcion.compareTo(BigDecimal.ONE) > 0) proporcion = BigDecimal.ONE;
        return velocidadNecesaria.multiply(proporcion);
    }

    private MermaResponseDTO mapToDTO(Merma m) {
        return MermaResponseDTO.builder()
                .id(m.getId())
                .productoId(m.getProducto().getId())
                .productoNombre(m.getProducto().getNombre())
                .productoCodigoBarra(m.getProducto().getCodigoBarra())
                .cantidadMermada(m.getCantidad())
                .motivo(m.getMotivo())
                .causa(m.getCausa())
                .loteId(m.getLote() != null ? m.getLote().getId() : null)
                .costoUnitario(m.getCostoUnitario())
                .perdidaTotal(m.getPerdidaTotal())
                .fechaRegistro(m.getFechaRegistro())
                .build();
    }
}
