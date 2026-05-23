package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.*;
import es.upm.fi.citas_backend.dto.BloqueoResponseDto;
import es.upm.fi.citas_backend.dto.DescubrimientoResponseDto;
import es.upm.fi.citas_backend.dto.MensajeResponseDto;
import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.repository.*;
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
class ServiceDtoCoherenceTests {

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
    private MatchRepository matchRepository;

    private Usuario usuario1;
    private Usuario usuario2;
    private Perfil perfil1;
    private Perfil perfil2;
    private Match match;

    @BeforeEach
    void setUp() {
        // Crear usuarios
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
            .descripcion("Viajero")
            .ubicacion("Madrid")
            .fotos(List.of("foto1.jpg"))
            .intereses(List.of("viajes"))
            .build();

        perfil2 = Perfil.builder()
            .usuario(usuario2)
            .nombre("María")
            .edad(26)
            .descripcion("Aventurera")
            .ubicacion("Barcelona")
            .fotos(List.of("foto2.jpg"))
            .intereses(List.of("senderismo"))
            .build();

        perfil1 = perfilRepository.save(perfil1);
        perfil2 = perfilRepository.save(perfil2);

        // Crear match
        match = Match.builder()
            .usuario1(usuario1)
            .usuario2(usuario2)
            .fechaCreacion(LocalDateTime.now())
            .estado(Match.EstadoMatch.ACTIVO)
            .build();
        match = matchRepository.save(match);
    }

    @Test
    void testBlockingServiceRetornaBloqueoResponseDtoConFechaBloqueo() {
        BloqueoResponseDto response = blockingService.bloquearUsuario(usuario1.getId(), usuario2.getId());

        // Validar estructura del DTO
        assertNotNull(response, "BloqueoResponseDto no debe ser null");
        assertNotNull(response.getBloqueoId(), "bloqueoId no debe ser null");
        assertNotNull(response.getFechaBloqueo(), "fechaBloqueo no debe ser null");
        
        // Validar que fechaBloqueo es reciente (dentro de 1 segundo)
        assertTrue(response.getFechaBloqueo().isBefore(LocalDateTime.now().plusSeconds(1)),
            "fechaBloqueo debe ser cercana a ahora");
        assertTrue(response.getFechaBloqueo().isAfter(LocalDateTime.now().minusMinutes(1)),
            "fechaBloqueo debe ser reciente");
    }

    @Test
    void testMensajeServiceRetornaMensajeResponseDtoConTextoYLeido() {
        MensajeResponseDto response = mensajeService.crearMensaje(
            match,
            usuario1.getId(),
            "Hola María!"
        );

        // Validar estructura del DTO
        assertNotNull(response, "MensajeResponseDto no debe ser null");
        assertNotNull(response.getMensajeId(), "mensajeId no debe ser null");
        assertNotNull(response.getTexto(), "texto no debe ser null");
        assertNotNull(response.getTimestamp(), "timestamp no debe ser null");
        assertFalse(response.isLeido(), "leido debe ser false inicialmente");

        // Validar coherencia
        assertEquals("Hola María!", response.getTexto(), "texto debe coincidir con lo enviado");
    }

    @Test
    void testDescubrimientoServiceRetornaFechaConsultaYResultados() {
        DescubrimientoResponseDto response = descubrimientoService.buscarCandidatos(
            perfil1,
            List.of(),
            0,
            20
        );

        // Validar estructura del DTO
        assertNotNull(response, "DescubrimientoResponseDto no debe ser null");
        assertNotNull(response.getFechaConsulta(), "fechaConsulta no debe ser null");
        assertNotNull(response.getResultados(), "resultados no debe ser null");

        // Validar que fechaConsulta es reciente
        assertTrue(response.getFechaConsulta().isBefore(LocalDateTime.now().plusSeconds(1)),
            "fechaConsulta debe ser reciente");
    }

    @Test
    void testDescubrimientoServiceResultadosConTodosAtributosDePerfilDto() {
        DescubrimientoResponseDto response = descubrimientoService.buscarCandidatos(
            perfil1,
            List.of(),
            0,
            20
        );

        // Validar que cada resultado tiene todos los atributos de PerfilDto
        for (PerfilDto perfil : response.getResultados()) {
            assertNotNull(perfil.getId(), "id del perfil no debe ser null");
            assertNotNull(perfil.getNombre(), "nombre del perfil no debe ser null");
            assertNotNull(perfil.getEdad(), "edad del perfil no debe ser null");
            assertNotNull(perfil.getDescripcion(), "descripción del perfil no debe ser null");
            assertNotNull(perfil.getUbicacion(), "ubicación del perfil no debe ser null");
            assertNotNull(perfil.getFotos(), "fotos del perfil no debe ser null");
            assertNotNull(perfil.getIntereses(), "intereses del perfil no debe ser null");
        }
    }

    @Test
    void testPerfilDtoConFotosComoLista() {
        DescubrimientoResponseDto response = descubrimientoService.buscarCandidatos(
            perfil1,
            List.of(),
            0,
            20
        );

        for (PerfilDto perfil : response.getResultados()) {
            // Validar que fotos es una lista, no un string
            assertTrue(perfil.getFotos() instanceof List, "fotos debe ser una List");
            if (!perfil.getFotos().isEmpty()) {
                assertTrue(perfil.getFotos().get(0) instanceof String,
                    "elementos de fotos deben ser String");
            }
        }
    }

    @Test
    void testCoherenciaTimestampEnMensaje() {
        MensajeResponseDto response = mensajeService.crearMensaje(
            match,
            usuario1.getId(),
            "Mensaje de prueba"
        );

        // Validar que timestamp está presente y es reciente
        assertNotNull(response.getTimestamp(), "timestamp no debe ser null");
        assertTrue(response.getTimestamp().isBefore(LocalDateTime.now().plusSeconds(1)),
            "timestamp debe ser reciente");
        assertTrue(response.getTimestamp().isAfter(LocalDateTime.now().minusSeconds(1)),
            "timestamp debe ser reciente");
    }

    @Test
    void testCoherenciaFechaBloqueoEnBloqueo() {
        LocalDateTime antes = LocalDateTime.now();
        BloqueoResponseDto response = blockingService.bloquearUsuario(usuario1.getId(), usuario2.getId());
        LocalDateTime despues = LocalDateTime.now();

        // Validar que fechaBloqueo está en el rango de tiempo correcto
        assertTrue(response.getFechaBloqueo().isAfter(antes.minusSeconds(1)),
            "fechaBloqueo debe estar después de antes");
        assertTrue(response.getFechaBloqueo().isBefore(despues.plusSeconds(1)),
            "fechaBloqueo debe estar antes de después");
    }

    @Test
    void testCoherenciaFechaConsultaEnDescubrimiento() {
        LocalDateTime antes = LocalDateTime.now();
        DescubrimientoResponseDto response = descubrimientoService.buscarCandidatos(
            perfil1,
            List.of(),
            0,
            20
        );
        LocalDateTime despues = LocalDateTime.now();

        // Validar que fechaConsulta está en el rango de tiempo correcto
        assertTrue(response.getFechaConsulta().isAfter(antes.minusSeconds(1)),
            "fechaConsulta debe estar después de antes");
        assertTrue(response.getFechaConsulta().isBefore(despues.plusSeconds(1)),
            "fechaConsulta debe estar antes de después");
    }
}
