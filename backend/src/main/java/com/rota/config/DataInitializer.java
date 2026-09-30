package com.rota.config;

import com.rota.entity.*;
import com.rota.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
@SuppressWarnings("null")
public class DataInitializer implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;
    private final LoteRepository loteRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info("Semilla de datos omitida: la base de datos ya contiene registros.");
            return;
        }

        log.info("Cargando datos de prueba (DataInitializer)...");

        // 1. Usuarios de Prueba
        Usuario dueno = usuarioRepository.save(Usuario.builder()
                .nombre("Carlos Dueño")
                .email("dueno@comercio.com")
                .password(passwordEncoder.encode("123456"))
                .rol(Rol.ROLE_DUENO)
                .build());

        Usuario repositor = usuarioRepository.save(Usuario.builder()
                .nombre("Juan Repositor")
                .email("repositor@comercio.com")
                .password(passwordEncoder.encode("123456"))
                .rol(Rol.ROLE_REPOSITOR)
                .build());

// 2. Categorías
        Categoria lacteos = categoriaRepository.save(Categoria.builder()
                .nombre("Lácteos")
                .diasUmbralAlerta(7)
                .diasUmbralCritico(3) // <-- ¡Agregado!
                .modoNotificacion(ModoNotificacion.DIARIO)
                .activa(true)
                .esPiloto(false)
                .build());
                
        Categoria panaderia = categoriaRepository.save(Categoria.builder()
                .nombre("Panadería")
                .diasUmbralAlerta(3)
                .diasUmbralCritico(1) // <-- ¡Agregado!
                .modoNotificacion(ModoNotificacion.CADA_2_DIAS)
                .activa(true)
                .esPiloto(false)
                .build());

        Categoria fiambres = categoriaRepository.save(Categoria.builder()
                .nombre("Fiambres y Embutidos")
                .diasUmbralAlerta(10)
                .diasUmbralCritico(5) // <-- ¡Agregado!
                .modoNotificacion(ModoNotificacion.DIARIO)
                .activa(true)
                .esPiloto(false)
                .build());

        // 3. Productos
        Producto leche = productoRepository.save(Producto.builder()
                .codigoBarra("7790001000011")
                .nombre("Leche Entera 1L")
                .costo(new BigDecimal("850.00"))
                .precioVenta(new BigDecimal("1200.00"))
                .categoria(lacteos)
                .build());

        Producto yogur = productoRepository.save(Producto.builder()
                .codigoBarra("7790001000028")
                .nombre("Yogur Frutilla 500g")
                .costo(new BigDecimal("600.00"))
                .precioVenta(new BigDecimal("950.00"))
                .categoria(lacteos)
                .build());

        Producto panLactal = productoRepository.save(Producto.builder()
                .codigoBarra("7790002000018")
                .nombre("Pan Lactal Blanco 500g")
                .costo(new BigDecimal("1100.00"))
                .precioVenta(new BigDecimal("1800.00"))
                .categoria(panaderia)
                .build());

        Producto quesoCremoso = productoRepository.save(Producto.builder()
                .codigoBarra("7790002000020")
                .nombre("Queso Cremoso 1kg")
                .costo(new BigDecimal("3500.00"))
                .precioVenta(new BigDecimal("5200.00"))
                .categoria(fiambres)
                .build());

        // 4. Lotes
        LocalDate hoy = LocalDate.now();

        Lote loteCrítico1 = Lote.builder()
                .producto(leche)
                .cantidad(25)
                .fechaVencimiento(hoy.plusDays(1))
                .ubicacion(Ubicacion.GONDOLA)
                .porcentajeDescuento(BigDecimal.ZERO)
                .build();

        Lote loteAlerta1 = Lote.builder()
                .producto(yogur)
                .cantidad(15)
                .fechaVencimiento(hoy.plusDays(5))
                .ubicacion(Ubicacion.DEPOSITO)
                .porcentajeDescuento(BigDecimal.ZERO)
                .build();

        Lote loteAlerta2 = Lote.builder()
                .producto(panLactal)
                .cantidad(10)
                .fechaVencimiento(hoy.plusDays(2))
                .ubicacion(Ubicacion.GONDOLA)
                .porcentajeDescuento(BigDecimal.ZERO)
                .build();

        Lote loteOk1 = Lote.builder()
                .producto(leche)
                .cantidad(50)
                .fechaVencimiento(hoy.plusDays(25))
                .ubicacion(Ubicacion.DEPOSITO)
                .porcentajeDescuento(BigDecimal.ZERO)
                .build();

        Lote loteOk2 = Lote.builder()
                .producto(quesoCremoso)
                .cantidad(8)
                .fechaVencimiento(hoy.plusDays(40))
                .ubicacion(Ubicacion.GONDOLA)
                .porcentajeDescuento(BigDecimal.ZERO)
                .build();

        loteRepository.saveAll(List.of(loteCrítico1, loteAlerta1, loteAlerta2, loteOk1, loteOk2));

        log.info("¡Datos semilla cargados exitosamente!");
        log.info("Credenciales cargadas -> Dueño: {} / 123456", dueno.getEmail());
        log.info("Credenciales cargadas -> Repositor: {} / 123456", repositor.getEmail());
    }
}