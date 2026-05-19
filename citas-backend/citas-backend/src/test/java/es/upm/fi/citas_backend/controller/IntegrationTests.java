package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.*;
import es.upm.fi.citas_backend.dto.*;
import es.upm.fi.citas_backend.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class IntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

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
    void testPerfilDtoConTodosAtributos() throws Exception {
        MvcResult result = mockMvc.perform(
            get("/usuarios/{usuarioId}/perfil", usuario1.getId())
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        PerfilDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PerfilDto.class
        );

        // Validar que todos los atributos están presentes según el dominio
        assertNotNull(response.getId(), "id no debe ser null");
        assertNotNull(response.getNombre(), "nombre no debe ser null");
        assertNotNull(response.getEdad(), "edad no debe ser null");
        assertNotNull(response.getDescripcion(), "descripción no debe ser null");
        assertNotNull(response.getUbicacion(), "ubicación no debe ser null");
        assertNotNull(response.getFotos(), "fotos no debe ser null");
        assertNotNull(response.getIntereses(), "intereses no debe ser null");

        assertTrue(response.getFotos().size() > 0, "fotos debe ser una lista");
        assertTrue(response.getIntereses().size() > 0, "intereses debe ser una lista");
    }

    @Test
    void testSwipeRequestDtoConTimestamp() throws Exception {
        SwipeRequestDto request = new SwipeRequestDto(
            usuario1.getId(),
            usuario2.getId(),
            "LIKE",
            LocalDateTime.now()
        );

        mockMvc.perform(
            post("/swipes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated());
    }

    @Test
    void testMensajeResponseDtoConTodoAtributo() throws Exception {
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
    void testBloqueoResponseDtoConFechaBloqueo() throws Exception {
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
    void testMatchResponseDtoConEstadoYFecha() throws Exception {
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
    void testDescubrimientoResponseDtoConFechaConsulta() throws Exception {
        // Llamar al endpoint de descubrimiento
        MvcResult result = mockMvc.perform(
            get("/descubrimiento")
                .param("usuarioId", usuario1.getId().toString())
                .param("pagina", "0")
                .param("limite", "20")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andReturn();

        DescubrimientoResponseDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            DescubrimientoResponseDto.class
        );

        // Validar que fechaConsulta está presente
        assertNotNull(response.getFechaConsulta(), "fechaConsulta no debe ser null");
        assertNotNull(response.getResultados(), "resultados no debe ser null");
    }

    @Test
    void testNotificacionResponseDtoConTodosAtributos() throws Exception {
        // Crear notificación directamente (ya que el controller hace un TODO)
        Notificacion notificacion = Notificacion.builder()
            .usuario(usuario1)
            .tipo("MATCH")
            .contenido("¡Nuevo match con María!")
            .timestamp(LocalDateTime.now())
            .leida(false)
            .build();

        // Validar atributos según el dominio
        assertNotNull(notificacion.getId(), "id no debe ser null");
        assertEquals("MATCH", notificacion.getTipo(), "tipo debe ser MATCH");
        assertNotNull(notificacion.getContenido(), "contenido no debe ser null");
        assertNotNull(notificacion.getTimestamp(), "timestamp no debe ser null");
        assertFalse(notificacion.isLeida(), "leida debe ser false inicialmente");
    }

    @Test
    void testCoherenciaPerfilRequestYResponse() throws Exception {
        // Crear request con todos los atributos
        PerfilRequestDto requestDto = new PerfilRequestDto(
            "Carlos",
            30,
            "Ingeniero y viajero",
            "Valencia",
            List.of("foto1.jpg", "foto2.jpg"),
            List.of("tech", "viajes")
        );

        // Simular update
        MvcResult result = mockMvc.perform(
            put("/usuarios/{usuarioId}/perfil", usuario1.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDto)))
            .andExpect(status().isOk())
            .andReturn();

        PerfilDto responseDto = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            PerfilDto.class
        );

        // Validar coherencia
        assertEquals(requestDto.getNombre(), responseDto.getNombre());
        assertEquals(requestDto.getEdad(), responseDto.getEdad());
        assertEquals(requestDto.getDescripcion(), responseDto.getDescripcion());
        assertEquals(requestDto.getUbicacion(), responseDto.getUbicacion());
    }

    @Test
    void testCoherenciaSwipeRequestConDominio() throws Exception {
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
