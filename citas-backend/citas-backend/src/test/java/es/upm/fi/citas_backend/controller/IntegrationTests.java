package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.*;
import es.upm.fi.citas_backend.dto.*;
import es.upm.fi.citas_backend.repository.*;
import es.upm.fi.citas_backend.service.BlockingService;
import es.upm.fi.citas_backend.service.DescubrimientoService;
import es.upm.fi.citas_backend.service.MensajeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class IntegrationTests {

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

    @BeforeEach
    void setUp() {
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
        assertEquals("ACEPTADO", saved.getAccion(), "acción debe ser ACEPTADO");
    }

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
}
