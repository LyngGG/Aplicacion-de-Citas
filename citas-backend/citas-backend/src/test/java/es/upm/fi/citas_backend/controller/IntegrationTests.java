package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.CitasBackendApplication;
import es.upm.fi.citas_backend.domain.*;
import es.upm.fi.citas_backend.dto.*;
import es.upm.fi.citas_backend.repository.*;
import es.upm.fi.citas_backend.service.BlockingService;
import es.upm.fi.citas_backend.service.DescubrimientoService;
import es.upm.fi.citas_backend.service.MensajeService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    classes = { CitasBackendApplication.class, IntegrationTests.TestSecurityConfig.class }
)
@Transactional
class IntegrationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private BlockingService blockingService;

    @Autowired
    private MensajeService mensajeService;

    @Autowired
    private DescubrimientoService descubrimientoService;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private SwipeRepository swipeRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MensajeRepository mensajeRepository;

    @Autowired
    private BloqueoRepository bloqueoRepository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Perfil perfil1;
    private Perfil perfil2;

    @TestConfiguration
    static class TestSecurityConfig {
        @Bean
        SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
            return http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
        }
    }

    /*
     * Prepara el entorno de pruebas: configura RestAssured, limpia repositorios y
     * crea dos usuarios con sus perfiles para un estado base consistente.
     */
    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        // Limpiar datos
        mensajeRepository.deleteAll();
        matchRepository.deleteAll();
        swipeRepository.deleteAll();
        bloqueoRepository.deleteAll();
        perfilRepository.deleteAll();
        usuarioRepository.deleteAll();

        // Crear usuarios de prueba
        usuario1 = Usuario.builder()
            .email("usuario1@test.com")
            .passwordHash("hash1")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        usuario2 = Usuario.builder()
            .email("usuario2@test.com")
            .passwordHash("hash2")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        usuario1 = usuarioRepository.save(usuario1);
        usuario2 = usuarioRepository.save(usuario2);

        // Crear perfiles
        perfil1 = Perfil.builder()
            .usuario(usuario1)
            .nombre("Juan")
            .edad(28)
            .descripcion("Amante de viajes")
            .ubicacion("Madrid")
            .fotos(List.of("foto1.jpg", "foto2.jpg"))
            .intereses(List.of("viajes", "cine"))
            .build();

        perfil2 = Perfil.builder()
            .usuario(usuario2)
            .nombre("María")
            .edad(26)
            .descripcion("Aventurera")
            .ubicacion("Barcelona")
            .fotos(List.of("foto3.jpg"))
            .intereses(List.of("senderismo"))
            .build();

        perfil1 = perfilRepository.save(perfil1);
        perfil2 = perfilRepository.save(perfil2);
    }

    /*
     * Verifica que un perfil persistido expone todos los atributos del dominio
     * y que las listas contienen al menos un elemento.
     */
    @Test
    void testPerfilDtoConTodosAtributos() {
        // Obtener perfil de la BD
        Perfil perfil = perfilRepository.findByUsuarioId(usuario1.getId())
            .orElseThrow();

        // Validar que todos los atributos están presentes según el dominio
        assertNotNull(perfil.getId(), "id no debe ser null");
        assertNotNull(perfil.getNombre(), "nombre no debe ser null");
        assertNotNull(perfil.getEdad(), "edad no debe ser null");
        assertNotNull(perfil.getDescripcion(), "descripción no debe ser null");
        assertNotNull(perfil.getUbicacion(), "ubicación no debe ser null");
        assertNotNull(perfil.getFotos(), "fotos no debe ser null");
        assertNotNull(perfil.getIntereses(), "intereses no debe ser null");

        assertTrue(perfil.getFotos().size() > 0, "fotos debe ser una lista");
        assertTrue(perfil.getIntereses().size() > 0, "intereses debe ser una lista");
    }

    /*
     * Persiste un swipe con timestamp y comprueba que id, timestamp y accion
     * se guardan correctamente.
     */
    @Test
    void testSwipeRequestDtoConTimestamp() {
        Swipe swipe = Swipe.builder()
            .remitente(usuario1)
            .destinatario(usuario2)
            .accion(Swipe.AccionSwipe.ACEPTADO)
            .timestamp(LocalDateTime.now())
            .build();

        Swipe saved = swipeRepository.save(swipe);

        assertNotNull(saved.getId(), "swipe id no debe ser null");
        assertNotNull(saved.getTimestamp(), "swipe timestamp no debe ser null");
        assertEquals(Swipe.AccionSwipe.ACEPTADO, saved.getAccion(), "acción debe ser ACEPTADO");
    }

    /*
     * Crea un match y un mensaje, y verifica que texto, timestamp y estado de leido
     * se persisten correctamente.
     */
    @Test
    void testMensajeResponseDtoConTodoAtributo() {
        // Crear un match
        Match match = Match.builder()
            .usuario1(usuario1)
            .usuario2(usuario2)
            .fechaCreacion(LocalDateTime.now())
            .estado(Match.EstadoMatch.ACTIVO)
            .build();
        match = matchRepository.save(match);

        // Crear un mensaje
        Mensaje mensaje = Mensaje.builder()
            .match(match)
            .remitente(usuario1)
            .texto("Hola, ¿qué tal?")
            .timestamp(LocalDateTime.now())
            .leido(false)
            .build();
        mensaje = mensajeRepository.save(mensaje);

        // Validar que todos los atributos del mensaje están correctos
        assertNotNull(mensaje.getId(), "mensajeId no debe ser null");
        assertEquals("Hola, ¿qué tal?", mensaje.getTexto(), "texto debe coincidir");
        assertNotNull(mensaje.getTimestamp(), "timestamp no debe ser null");
        assertFalse(mensaje.isLeido(), "leido debe ser false inicialmente");
    }

    /*
     * Crea un bloqueo y valida que tiene id y una fecha de bloqueo reciente.
     */
    @Test
    void testBloqueoResponseDtoConFechaBloqueo() {
        // Crear bloqueo
        Bloqueo bloqueo = Bloqueo.builder()
            .bloqueador(usuario1)
            .bloqueado(usuario2)
            .fechaBloqueo(LocalDateTime.now())
            .build();
        bloqueo = bloqueoRepository.save(bloqueo);

        // Validar atributos del DTO
        assertNotNull(bloqueo.getId(), "bloqueoId no debe ser null");
        assertNotNull(bloqueo.getFechaBloqueo(), "fechaBloqueo no debe ser null");
        assertTrue(bloqueo.getFechaBloqueo().isBefore(LocalDateTime.now().plusMinutes(1)));
    }

    /*
     * Crea un match y valida que estado y fecha de creacion estan presentes y correctos.
     */
    @Test
    void testMatchResponseDtoConEstadoYFecha() {
        // Crear match
        Match match = Match.builder()
            .usuario1(usuario1)
            .usuario2(usuario2)
            .fechaCreacion(LocalDateTime.now())
            .estado(Match.EstadoMatch.ACTIVO)
            .build();
        match = matchRepository.save(match);

        // Validar atributos
        assertNotNull(match.getId(), "id no debe ser null");
        assertNotNull(match.getFechaCreacion(), "fechaCreacion no debe ser null");
        assertEquals(Match.EstadoMatch.ACTIVO, match.getEstado(), "estado debe ser ACTIVO");
    }

    /*
     * Llama a descubrimiento y comprueba que la respuesta incluye fecha de consulta
     * y resultados.
     */
    @Test
    void testDescubrimientoResponseDtoConFechaConsulta() {
        // Llamar al service
        DescubrimientoResponseDto response = descubrimientoService.buscarCandidatos(
            perfil1,
            List.of(),
            0,
            20
        );

        // Validar que fechaConsulta está presente
        assertNotNull(response.getFechaConsulta(), "fechaConsulta no debe ser null");
        assertNotNull(response.getResultados(), "resultados no debe ser null");
    }

    // Notificacion test omitido porque la clase Notificacion no existe en el dominio
    // (Solo existe NotificacionResponseDto, pero no la entidad)
    // Sería necesario crear la clase Notificacion.java primero

    /*
     * Asegura que los resultados de descubrimiento reflejan campos clave del perfil.
     */
    @Test
    void testCoherenciaPerfilRequestYResponse() {
        // Simular coherencia con el servicio
        DescubrimientoResponseDto response = descubrimientoService.buscarCandidatos(
            perfil1,
            List.of(),
            0,
            20
        );

        for (PerfilDto perfilDto : response.getResultados()) {
            assertEquals(perfil2.getNombre(), perfilDto.getNombre());
            assertEquals(perfil2.getEdad(), perfilDto.getEdad());
            assertEquals(perfil2.getDescripcion(), perfilDto.getDescripcion());
            assertEquals(perfil2.getUbicacion(), perfilDto.getUbicacion());
        }
    }

    /*
     * Valida que SwipeRequestDto define todos los campos requeridos del dominio.
     */
    @Test
    void testCoherenciaSwipeRequestConDominio() {
        // Validar que SwipeRequestDto tiene todos los atributos del dominio
        SwipeRequestDto swipeDto = new SwipeRequestDto();
        
        assertTrue(tieneAtributo(swipeDto, "usuarioOrigen"), "SwipeRequestDto debe tener usuarioOrigen");
        assertTrue(tieneAtributo(swipeDto, "usuarioDestino"), "SwipeRequestDto debe tener usuarioDestino");
        assertTrue(tieneAtributo(swipeDto, "accion"), "SwipeRequestDto debe tener accion");
        assertTrue(tieneAtributo(swipeDto, "timestamp"), "SwipeRequestDto debe tener timestamp");
    }

    private boolean tieneAtributo(Object obj, String atributo) {
        try {
            obj.getClass().getDeclaredField(atributo);
            return true;
        } catch (NoSuchFieldException e) {
            return false;
        }
    }

    /*
     * Registra un usuario y verifica que el login devuelve datos con estado 200.
     */
    @Test
    void login_valido_devuelveUsuario() {
        String email = "login_" + UUID.randomUUID() + "@test.com";
        String password = "secreto123";

        Long usuarioId = registrarUsuario(email, password);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("usuarioId", usuarioId, "password", password))
        .when()
            .post("/usuarios/login")
        .then()
            .statusCode(200)
            .body("id", equalTo(usuarioId.intValue()))
            .body("email", equalTo(email))
            .body("estado", notNullValue());
    }

    /*
     * Intenta login con un usuario inexistente y espera error 404.
     */
    @Test
    void login_usuarioNoExiste_devuelve404() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("usuarioId", 99999, "password", "cualquiera"))
        .when()
            .post("/usuarios/login")
        .then()
            .statusCode(404)
            .body("error", containsString("Usuario no encontrado"));
    }

    /*
     * Intenta login con usuario valido y password incorrecta y espera error.
     */
    @Test
    void login_passwordIncorrecta_devuelve500() {
        String email = "login_bad_" + UUID.randomUUID() + "@test.com";
        String password = "secreto123";

        Long usuarioId = registrarUsuario(email, password);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("usuarioId", usuarioId, "password", "mal"))
        .when()
            .post("/usuarios/login")
        .then()
            .statusCode(500);
    }

    /*
     * Intenta login sin campos obligatorios y espera error de validacion 400.
     */
    @Test
    void login_camposFaltantes_devuelve400() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("password", "secreto123"))
        .when()
            .post("/usuarios/login")
        .then()
            .statusCode(400);
    }

    /*
     * Crea un perfil para un usuario nuevo y espera respuesta 201.
     */
    @Test
    void crearPerfil_valido_devuelve201() {
        String email = "perfil_" + UUID.randomUUID() + "@test.com";
        Long usuarioId = registrarUsuario(email, "secreto123");

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("nombre", "Paula", "edad", 24))
        .when()
            .post("/usuarios/{usuarioId}/perfil", usuarioId)
        .then()
            .statusCode(201)
            .body("id", notNullValue())
            .body("nombre", equalTo("Paula"))
            .body("edad", equalTo(24));
    }

    /*
     * Intenta crear un perfil para un usuario inexistente y espera 404.
     */
    @Test
    void crearPerfil_usuarioNoExiste_devuelve404() {
        given()
            .contentType(ContentType.JSON)
            .body(Map.of("nombre", "Paula", "edad", 24))
        .when()
            .post("/usuarios/{usuarioId}/perfil", 99999)
        .then()
            .statusCode(404)
            .body("error", containsString("Usuario no encontrado"));
    }

    /*
     * Intenta crear un perfil para un usuario que ya tiene y espera error.
     */
    @Test
    void crearPerfil_yaExiste_devuelve500() {
        String email = "perfil_dup_" + UUID.randomUUID() + "@test.com";
        Long usuarioId = registrarUsuario(email, "secreto123");

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("nombre", "Paula", "edad", 24))
        .when()
            .post("/usuarios/{usuarioId}/perfil", usuarioId)
        .then()
            .statusCode(201);

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("nombre", "Otro", "edad", 30))
        .when()
            .post("/usuarios/{usuarioId}/perfil", usuarioId)
        .then()
            .statusCode(500);
    }

    /*
     * Intenta crear un perfil con nombre vacio y espera error de validacion 400.
     */
    @Test
    void crearPerfil_nombreVacio_devuelve400() {
        String email = "perfil_bad_" + UUID.randomUUID() + "@test.com";
        Long usuarioId = registrarUsuario(email, "secreto123");

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("nombre", "", "edad", 24))
        .when()
            .post("/usuarios/{usuarioId}/perfil", usuarioId)
        .then()
            .statusCode(400);
    }

    /*
     * Intenta crear un perfil sin edad y espera error de validacion 400.
     */
    @Test
    void crearPerfil_edadNula_devuelve400() {
        String email = "perfil_bad2_" + UUID.randomUUID() + "@test.com";
        Long usuarioId = registrarUsuario(email, "secreto123");

        given()
            .contentType(ContentType.JSON)
            .body(Map.of("nombre", "Paula"))
        .when()
            .post("/usuarios/{usuarioId}/perfil", usuarioId)
        .then()
            .statusCode(400);
    }

    private Long registrarUsuario(String email, String password) {
        Number id = given()
            .contentType(ContentType.JSON)
            .body(Map.of("email", email, "password", password))
        .when()
            .post("/usuarios/registro")
        .then()
            .statusCode(201)
            .extract()
            .path("id");
        return id.longValue();
    }
}
