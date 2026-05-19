package es.upm.fi.citas_backend.dto;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DtoValidationTests {

    @Test
    void testUsuarioResponseDtoAtributos() {
        UsuarioResponseDto dto = new UsuarioResponseDto(1L, "test@test.com", "ACTIVO");
        
        assertEquals(1L, dto.getId());
        assertEquals("test@test.com", dto.getEmail());
        assertEquals("ACTIVO", dto.getEstado());
    }

    @Test
    void testSwipeRequestDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        SwipeRequestDto dto = new SwipeRequestDto(1L, 2L, "LIKE", now);
        
        assertEquals(1L, dto.getUsuarioOrigen());
        assertEquals(2L, dto.getUsuarioDestino());
        assertEquals("LIKE", dto.getAccion());
        assertEquals(now, dto.getTimestamp());
    }

    @Test
    void testMatchResponseDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        MatchResponseDto dto = new MatchResponseDto(1L, 10L, 20L, now, "ACTIVO");
        
        assertEquals(1L, dto.getId());
        assertEquals(10L, dto.getUsuario1Id());
        assertEquals(20L, dto.getUsuario2Id());
        assertEquals(now, dto.getFechaCreacion());
        assertEquals("ACTIVO", dto.getEstado());
    }

    @Test
    void testMensajeResponseDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        MensajeResponseDto dto = new MensajeResponseDto(1L, "Hola", now, false);
        
        assertEquals(1L, dto.getMensajeId());
        assertEquals("Hola", dto.getTexto());
        assertEquals(now, dto.getTimestamp());
        assertFalse(dto.isLeido());
    }

    @Test
    void testBloqueoResponseDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        BloqueoResponseDto dto = new BloqueoResponseDto(1L, now);
        
        assertEquals(1L, dto.getBloqueoId());
        assertEquals(now, dto.getFechaBloqueo());
    }

    @Test
    void testDescubrimientoResponseDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        List<PerfilDto> perfiles = List.of();
        DescubrimientoResponseDto dto = new DescubrimientoResponseDto(now, perfiles);
        
        assertEquals(now, dto.getFechaConsulta());
        assertEquals(perfiles, dto.getResultados());
    }

    @Test
    void testPerfilDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        List<String> fotos = List.of("foto1.jpg", "foto2.jpg");
        List<String> intereses = List.of("viajes", "cine");
        
        PerfilDto dto = new PerfilDto(
            1L,
            "Juan",
            28,
            "Viajero amante del cine",
            "Madrid",
            fotos,
            intereses
        );
        
        assertEquals(1L, dto.getId());
        assertEquals("Juan", dto.getNombre());
        assertEquals(28, dto.getEdad());
        assertEquals("Viajero amante del cine", dto.getDescripcion());
        assertEquals("Madrid", dto.getUbicacion());
        assertEquals(fotos, dto.getFotos());
        assertEquals(intereses, dto.getIntereses());
        assertEquals(2, dto.getFotos().size());
    }

    @Test
    void testPerfilRequestDtoAtributos() {
        List<String> fotos = List.of("foto1.jpg");
        List<String> intereses = List.of("tech");
        
        PerfilRequestDto dto = new PerfilRequestDto(
            "Carlos",
            30,
            "Ingeniero",
            "Barcelona",
            fotos,
            intereses
        );
        
        assertEquals("Carlos", dto.getNombre());
        assertEquals(30, dto.getEdad());
        assertEquals("Ingeniero", dto.getDescripcion());
        assertEquals("Barcelona", dto.getUbicacion());
        assertEquals(fotos, dto.getFotos());
        assertEquals(intereses, dto.getIntereses());
    }

    @Test
    void testNotificacionResponseDtoAtributos() {
        LocalDateTime now = LocalDateTime.now();
        NotificacionResponseDto dto = new NotificacionResponseDto(
            1L,
            "MATCH",
            "¡Nuevo match!",
            now,
            false
        );
        
        assertEquals(1L, dto.getId());
        assertEquals("MATCH", dto.getTipo());
        assertEquals("¡Nuevo match!", dto.getContenido());
        assertEquals(now, dto.getTimestamp());
        assertFalse(dto.isLeida());
    }

    @Test
    void testCoherenciaFotosAhora() {
        // Validar que "fotos" es List<String> no String
        PerfilDto dto = new PerfilDto(
            1L,
            "Test",
            25,
            "Test",
            "Test",
            List.of("foto1.jpg", "foto2.jpg"),  // List, no String
            List.of("test")
        );
        
        assertTrue(dto.getFotos() instanceof List, "fotos debe ser List");
        assertEquals(2, dto.getFotos().size(), "fotos debe tener 2 elementos");
    }

    @Test
    void testCoherenciaDescripcion() {
        // Validar que "descripcion" está presente en PerfilDto
        PerfilDto dto = new PerfilDto(
            1L,
            "Juan",
            25,
            "Esta es mi descripción",  // descripcion
            "Madrid",
            List.of(),
            List.of()
        );
        
        assertEquals("Esta es mi descripción", dto.getDescripcion(),
            "descripcion debe estar presente en PerfilDto");
    }

    @Test
    void testCoherenciaTimestampEnSwipe() {
        // Validar que timestamp está en SwipeRequestDto
        LocalDateTime now = LocalDateTime.now();
        SwipeRequestDto dto = new SwipeRequestDto(1L, 2L, "LIKE", now);
        
        assertNotNull(dto.getTimestamp(), "timestamp debe estar presente en SwipeRequestDto");
        assertEquals(now, dto.getTimestamp(), "timestamp debe coincidir");
    }

    @Test
    void testCoherenciaMensajeConTextoYLeido() {
        // Validar que texto y leido están en MensajeResponseDto
        LocalDateTime now = LocalDateTime.now();
        MensajeResponseDto dto = new MensajeResponseDto(1L, "Mensaje", now, true);
        
        assertNotNull(dto.getTexto(), "texto debe estar presente");
        assertEquals("Mensaje", dto.getTexto(), "texto debe coincidir");
        assertTrue(dto.isLeido(), "leido debe ser true");
    }
}
