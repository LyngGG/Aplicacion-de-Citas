package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Perfil;
import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.PerfilDto;
import es.upm.fi.citas_backend.dto.PerfilRequestDto;
import es.upm.fi.citas_backend.exception.PerfilNotFoundException;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.fake.FakeCacheService;
import es.upm.fi.citas_backend.repository.PerfilRepository;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para PerfilService.
 * 
 * PATRÓN GRASP: EXPERTO + INDIRECCIÓN
 * PerfilService es experto en lógica de perfil.
 * FakeCacheService media entre PerfilService y almacenamiento.
 * 
 * CU3: Descubrimiento de Perfiles (parcial - obtener perfil)
 */
@ExtendWith(MockitoExtension.class)
class PerfilServiceTest {

    @Mock
    private PerfilRepository perfilRepository;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private FakeCacheService cache;

    @InjectMocks
    private PerfilService perfilService;

    /**
     * CU3: Obtener perfil (con caché)
     * 
     * Validar que:
     * - Si hay hit en caché, retorna inmediatamente sin consultar BD
     * - Caché mejora performance para consultas frecuentes
     */
    @Test
    void obtenerPerfil_retornaDelCacheSiExiste() {
        Long usuarioId = 1L;
        Perfil perfilEnCache = Perfil.builder()
            .id(5L)
            .nombre("Juan")
            .edad(28)
            .descripcion("Amante de viajes")
            .ubicacion("Madrid")
            .fotos(List.of("foto1.jpg"))
            .intereses(List.of("viajes"))
            .build();

        // Mock: caché tiene el perfil
        when(cache.get("perfil:1")).thenReturn(Optional.of(perfilEnCache));

        // Ejecutar
        Perfil resultado = perfilService.obtenerPerfil(usuarioId);

        // Validar
        assertEquals(perfilEnCache, resultado);
        assertEquals("Juan", resultado.getNombre());
        assertEquals(28, resultado.getEdad());

        // Validar que NO se consultó BD (caché hit)
        verify(perfilRepository, never()).findByUsuarioId(usuarioId);
    }

    /**
     * CU3: Descubrimiento - Cache miss
     * 
     * Validar que:
     * - Si NO hay en caché, consulta BD
     * - Guarda resultado en caché para futuras consultas
     * - TTL de 1 hora (aquí simulado)
     */
    @Test
    void obtenerPerfil_cargaDesdeBDYCachea() {
        Long usuarioId = 1L;
        Perfil perfilBD = Perfil.builder()
            .id(5L)
            .nombre("María")
            .edad(26)
            .descripcion("Aventurera")
            .ubicacion("Barcelona")
            .fotos(List.of("foto2.jpg"))
            .intereses(List.of("senderismo"))
            .build();

        // Mock: caché no tiene nada
        when(cache.get("perfil:1")).thenReturn(Optional.empty());
        
        // Mock: BD tiene el perfil
        when(perfilRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(perfilBD));

        // Ejecutar
        Perfil resultado = perfilService.obtenerPerfil(usuarioId);

        // Validar
        assertEquals(perfilBD, resultado);
        assertEquals("María", resultado.getNombre());

        // Validar que se guardó en caché
        verify(cache).set("perfil:1", perfilBD);
    }

    /**
     * Validar que lanza excepción si perfil no existe
     */
    @Test
    void obtenerPerfil_lanzaSiNoExiste() {
        Long usuarioId = 404L;

        when(cache.get("perfil:404")).thenReturn(Optional.empty());
        when(perfilRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThrows(PerfilNotFoundException.class, () ->
            perfilService.obtenerPerfil(usuarioId));
    }

    /**
     * CU: Obtener perfil como DTO
     * 
     * Validar que:
     * - Convierte entidad Perfil a DTO
     * - DTO contiene todos los atributos necesarios
     */
    @Test
    void obtenerPerfilDto_retornaConTodosAtributos() {
        Long usuarioId = 1L;
        Perfil perfil = Perfil.builder()
            .id(5L)
            .nombre("Juan")
            .edad(28)
            .descripcion("Amante de viajes")
            .ubicacion("Madrid")
            .fotos(List.of("foto1.jpg", "foto2.jpg"))
            .intereses(List.of("viajes", "cine"))
            .build();

        when(cache.get("perfil:1")).thenReturn(Optional.of(perfil));

        // Ejecutar
        PerfilDto dto = perfilService.obtenerPerfilDto(usuarioId);

        // Validar
        assertNotNull(dto);
        assertEquals("Juan", dto.getNombre());
        assertEquals(28, dto.getEdad());
        assertEquals("Amante de viajes", dto.getDescripcion());
        assertEquals("Madrid", dto.getUbicacion());
        assertEquals(2, dto.getFotos().size());
        assertEquals(2, dto.getIntereses().size());
    }

    /**
     * CU: Actualizar perfil
     * 
     * Validar que:
     * - Se actualiza correctamente los campos del perfil
     * - Se invalida caché después de actualizar
     * - Se retorna DTO actualizado
     */
    @Test
    void actualizarPerfil_actualizaCamposYInvalidaCache() {
        Long usuarioId = 1L;
        
        Usuario usuario = Usuario.builder()
            .id(usuarioId)
            .email("usuario@test.com")
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        Perfil perfilAntiguo = Perfil.builder()
            .id(5L)
            .usuario(usuario)
            .nombre("Juan Antiguo")
            .edad(28)
            .descripcion("Descripción vieja")
            .ubicacion("Madrid")
            .fotos(List.of("foto1.jpg"))
            .intereses(List.of("viajes"))
            .build();

        PerfilRequestDto request = new PerfilRequestDto(
            "Juan Nuevo",
            29,
            "Descripción nueva",
            "Barcelona",
            List.of("nuevafoto.jpg"),
            List.of("senderismo", "música")
        );

        // Mock: usuario existe
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        
        // Mock: perfil existe
        when(perfilRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.of(perfilAntiguo));
        
        // Mock: save retorna perfil actualizado
        when(perfilRepository.save(any(Perfil.class))).thenAnswer(invocation -> {
            return invocation.getArgument(0);
        });

        // Ejecutar
        PerfilDto resultado = perfilService.actualizarPerfil(usuarioId, request);

        // Validar
        assertNotNull(resultado);
        assertEquals("Juan Nuevo", resultado.getNombre());
        assertEquals(29, resultado.getEdad());
        assertEquals("Descripción nueva", resultado.getDescripcion());
        assertEquals("Barcelona", resultado.getUbicacion());
        assertEquals(1, resultado.getFotos().size());
        assertEquals(2, resultado.getIntereses().size());

        // Validar que se invalidó el caché
        verify(cache).evict("perfil:1");
    }

    /**
     * Validar que lanza excepción si usuario no existe
     */
    @Test
    void actualizarPerfil_lanzaSiUsuarioNoExiste() {
        PerfilRequestDto request = new PerfilRequestDto();
        
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            perfilService.actualizarPerfil(404L, request));
    }

    /**
     * Validar que lanza excepción si perfil no existe
     */
    @Test
    void actualizarPerfil_lanzaSiPerfilNoExiste() {
        Long usuarioId = 1L;
        Usuario usuario = Usuario.builder()
            .id(usuarioId)
            .email("usuario@test.com")
            .build();

        PerfilRequestDto request = new PerfilRequestDto();

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(perfilRepository.findByUsuarioId(usuarioId)).thenReturn(Optional.empty());

        assertThrows(PerfilNotFoundException.class, () ->
            perfilService.actualizarPerfil(usuarioId, request));
    }
}
