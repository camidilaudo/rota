package com.rota.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rota.dto.request.AuthRequestDTO;
import com.rota.dto.request.CategoriaRequestDTO;
import com.rota.dto.request.ComercioRequestDTO;
import com.rota.entity.Categoria;
import com.rota.entity.ModoNotificacion;
import com.rota.entity.Rol;
import com.rota.entity.Usuario;
import com.rota.repository.CategoriaRepository;
import com.rota.repository.UsuarioRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@SuppressWarnings("null")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE;MODE=PostgreSQL",
    "spring.datasource.driver-class-name=org.h2.Driver",
    "spring.datasource.username=sa",
    "spring.datasource.password=",
    "spring.jpa.database-platform=org.hibernate.dialect.H2Dialect",
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
class ComercioIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("HU-01: Dar de alta categoría piloto inicial")
    void testAltaCategoriaPiloto() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Lácteos Piloto");
        
        // ¡Agregar el campo obligatorio!
        dto.setDiasUmbralAlerta(7); 
        dto.setDiasUmbralCritico(3); 
        
        dto.setModoNotificacion(ModoNotificacion.DIARIO);
        dto.setEsPiloto(false);

        mockMvc.perform(post("/api/categorias")
                .with(csrf()) // IMPORTANTE si usas Spring Security
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Lácteos Piloto"));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("Configurar notificación de categoría")
    void testConfigurarNotificacionCategoria() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Fiambrería");
        
        // ¡Agregar el campo obligatorio!
        dto.setDiasUmbralAlerta(5); 
        dto.setDiasUmbralCritico(2); 
        
        dto.setModoNotificacion(ModoNotificacion.PANTALLA);
        dto.setEsPiloto(false);

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.nombre").value("Fiambrería"));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("FALLO: Intentar crear categoría con datos nulos o umbral crítico inválido")
    void testCrearCategoria_DatosInvalidos() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre(""); // Nombre vacío
        dto.setDiasUmbralAlerta(2);
        dto.setDiasUmbralCritico(5); // Umbral crítico mayor que el de alerta (violación de regla de negocio)

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest()); // Espera HTTP 400
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("FALLO: Repositor intenta crear una categoría (Sin Autorización)")
    void testCrearCategoria_SinRolSuficiente() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Bebidas");
        dto.setDiasUmbralAlerta(5);
        dto.setDiasUmbralCritico(2);
        dto.setModoNotificacion(ModoNotificacion.DIARIO);

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden()); // Espera HTTP 403 Forbidden
    }

    @Test
    @DisplayName("FALLO: Usuario no autenticado intenta acceder a la API de categorías")
    void testCrearCategoria_SinAutenticacion() throws Exception {
        CategoriaRequestDTO dto = new CategoriaRequestDTO();
        dto.setNombre("Golosinas");
        dto.setDiasUmbralAlerta(5);
        dto.setDiasUmbralCritico(2);

        mockMvc.perform(post("/api/categorias")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized()); // Espera HTTP 401 Unauthorized
    }

    @Test
    @DisplayName("HU-01: Crear comercio con categoría piloto activa y dueño asociado que puede ingresar al sistema")
    void testAltaComercioConCategoriaPiloto() throws Exception {
        String sufijo = String.valueOf(System.nanoTime());
        String emailDueno = "dueno.hu01." + sufijo + "@comercio.com";
        usuarioRepository.save(Usuario.builder()
                .nombre("Dueño HU-01")
                .email(emailDueno)
                .password(passwordEncoder.encode("clave123"))
                .rol(Rol.ROLE_DUENO)
                .build());

        Categoria categoria = categoriaRepository.save(Categoria.builder()
                .nombre("Congelados HU-01 " + sufijo)
                .diasUmbralAlerta(10)
                .diasUmbralCritico(4)
                .modoNotificacion(ModoNotificacion.DIARIO)
                .esPiloto(false)
                .build());

        ComercioRequestDTO dto = new ComercioRequestDTO();
        dto.setNombre("Kiosco La Esquina");
        dto.setCuit("27-11111111-1");
        dto.setDireccion("Calle Falsa 123");
        dto.setCategoriasPilotoIds(List.of(categoria.getId()));

        // 1) Se crea el comercio y 2) se selecciona la categoría piloto
        String respuesta = mockMvc.perform(post("/api/comercios")
                .with(user(emailDueno).roles("DUENO"))
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.nombre").value("Kiosco La Esquina"))
                .andExpect(jsonPath("$.activo").value(true))
                .andExpect(jsonPath("$.duenoEmail").value(emailDueno))
                .andExpect(jsonPath("$.categoriasPiloto[0].id").value(categoria.getId()))
                .andExpect(jsonPath("$.categoriasPiloto[0].esPiloto").value(true))
                // 3) La categoría queda activa
                .andExpect(jsonPath("$.categoriasPiloto[0].activa").value(true))
                .andReturn().getResponse().getContentAsString();

        Long comercioId = objectMapper.readTree(respuesta).get("id").asLong();

        Categoria categoriaGuardada = categoriaRepository.findById(categoria.getId()).orElseThrow();
        assertTrue(categoriaGuardada.getEsPiloto());
        assertTrue(categoriaGuardada.getActiva());
        assertEquals(comercioId, categoriaGuardada.getComercio().getId());

        // El dueño queda asociado al comercio
        Usuario duenoGuardado = usuarioRepository.findByEmail(emailDueno).orElseThrow();
        assertNotNull(duenoGuardado.getComercio());
        assertEquals(comercioId, duenoGuardado.getComercio().getId());

        // 4) El comercio creado puede ingresar al sistema: el login devuelve token y comercio
        AuthRequestDTO login = new AuthRequestDTO();
        login.setEmail(emailDueno);
        login.setPassword("clave123");

        String loginRespuesta = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.comercioId").value(comercioId))
                .andExpect(jsonPath("$.comercioNombre").value("Kiosco La Esquina"))
                .andReturn().getResponse().getContentAsString();

        JsonNode loginJson = objectMapper.readTree(loginRespuesta);
        mockMvc.perform(get("/api/comercios/" + comercioId)
                .header("Authorization", "Bearer " + loginJson.get("token").asText()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Kiosco La Esquina"))
                .andExpect(jsonPath("$.categoriasPiloto[0].esPiloto").value(true))
                .andExpect(jsonPath("$.categoriasPiloto[0].activa").value(true));
    }

    @Test
    @DisplayName("HU-01: El dueño de los datos de ejemplo queda asociado a su comercio al iniciar sesión")
    void testLoginDuenoSemillaIncluyeComercio() throws Exception {
        AuthRequestDTO login = new AuthRequestDTO();
        login.setEmail("dueno@comercio.com");
        login.setPassword("123456");

        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(login)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comercioId").exists())
                .andExpect(jsonPath("$.comercioNombre").value("Almacén Don Carlos"));
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("FALLO HU-01: Crear comercio sin seleccionar categoría piloto")
    void testCrearComercio_SinCategoriaPiloto() throws Exception {
        ComercioRequestDTO dto = new ComercioRequestDTO();
        dto.setNombre("Comercio Sin Piloto");
        dto.setCategoriasPilotoIds(List.of());

        mockMvc.perform(post("/api/comercios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "repositor@comercio.com", authorities = {"ROLE_REPOSITOR"})
    @DisplayName("FALLO HU-01: Repositor intenta crear un comercio (Sin Autorización)")
    void testCrearComercio_SinRolSuficiente() throws Exception {
        ComercioRequestDTO dto = new ComercioRequestDTO();
        dto.setNombre("Comercio Repositor");
        dto.setCategoriasPilotoIds(List.of(1L));

        mockMvc.perform(post("/api/comercios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "dueno@comercio.com", authorities = {"ROLE_DUENO"})
    @DisplayName("FALLO HU-01: Consultar un comercio inexistente retorna 404")
    void testObtenerComercio_Inexistente() throws Exception {
        mockMvc.perform(get("/api/comercios/999999"))
                .andExpect(status().isNotFound());
    }
}
