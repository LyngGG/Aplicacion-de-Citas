package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.UsuarioResponseDto;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Tests unitarios para UsuarioService.
 * 
 * PATRÓN GRASP: EXPERTO
 * UsuarioService es experto en reglas de negocio de usuario (registro, autenticación).
 * 
 * Tests validados contra especificaciones de dominio.
 */
@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    /**
     * CU: Registrar un nuevo usuario
     * 
     * Validar que:
     * - Se crea usuario con email y contraseña hasheada
     * - Se guarda en repositorio
     * - Se retorna DTO con los datos correctos
     */
    @Test
    void registrar_crearUsuarioNuevo() {
        String email = "usuario@test.com";
        String password = "password123";
        String passwordHash = "hashedPassword123";

        // Mock: email no existe
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.empty());
        
        // Mock: passwordEncoder hashea la contraseña
        when(passwordEncoder.encode(password)).thenReturn(passwordHash);
        
        // Mock: save retorna usuario con ID asignado
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario usuario = invocation.getArgument(0);
            usuario.setId(1L);
            return usuario;
        });

        // Ejecutar
        UsuarioResponseDto response = usuarioService.registrar(email, password);

        // Validar
        assertNotNull(response);
        assertEquals(1L, response.getId());
        assertEquals(email, response.getEmail());
        assertEquals("ACTIVO", response.getEstado());

        // Validar que se guardó correctamente
        ArgumentCaptor<Usuario> captor = ArgumentCaptor.forClass(Usuario.class);
        verify(usuarioRepository).save(captor.capture());
        
        Usuario saved = captor.getValue();
        assertEquals(email, saved.getEmail());
        assertEquals(passwordHash, saved.getPasswordHash());
        assertEquals(Usuario.EstadoUsuario.ACTIVO, saved.getEstado());
    }

    /**
     * Validar que se rechaza registro con email duplicado
     * PATRÓN: BAJO ACOPLAMIENTO - Excepción clara
     */
    @Test
    void registrar_rechazaEmailDuplicado() {
        String email = "duplicado@test.com";
        
        // Mock: email ya existe
        Usuario existente = Usuario.builder()
            .id(5L)
            .email(email)
            .passwordHash("hash")
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();
        
        when(usuarioRepository.findByEmail(email)).thenReturn(Optional.of(existente));

        // Validar que lanza excepción
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.registrar(email, "password123"));

        // Validar que NO se guardó nada
        verify(usuarioRepository, never()).save(any());
    }

    /**
     * CU: Autenticar usuario
     * 
     * Validar que:
     * - Se verifica contraseña correctamente
     * - Se retorna DTO del usuario autenticado
     */
    @Test
    void autenticar_usuarioConCredencialesValidas() {
        Long usuarioId = 1L;
        String password = "password123";
        String passwordHash = "hashedPassword123";

        Usuario usuario = Usuario.builder()
            .id(usuarioId)
            .email("usuario@test.com")
            .passwordHash(passwordHash)
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        // Mock: usuario existe
        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        
        // Mock: contraseña coincide
        when(passwordEncoder.matches(password, passwordHash)).thenReturn(true);

        // Ejecutar
        UsuarioResponseDto response = usuarioService.autenticar(usuarioId, password);

        // Validar
        assertNotNull(response);
        assertEquals(usuarioId, response.getId());
        assertEquals("usuario@test.com", response.getEmail());
        assertEquals("ACTIVO", response.getEstado());
    }

    /**
     * Validar que se rechaza autenticación con contraseña inválida
     */
    @Test
    void autenticar_rechazaContraseñaInvalida() {
        Long usuarioId = 1L;
        String passwordIncorrecta = "wrongPassword";
        String passwordHash = "hashedPassword123";

        Usuario usuario = Usuario.builder()
            .id(usuarioId)
            .email("usuario@test.com")
            .passwordHash(passwordHash)
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        when(usuarioRepository.findById(usuarioId)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches(passwordIncorrecta, passwordHash)).thenReturn(false);

        // Validar que lanza excepción
        assertThrows(IllegalArgumentException.class, () ->
            usuarioService.autenticar(usuarioId, passwordIncorrecta));
    }

    /**
     * Validar que se rechaza autenticación si usuario no existe
     */
    @Test
    void autenticar_rechazaSiUsuarioNoExiste() {
        when(usuarioRepository.findById(404L)).thenReturn(Optional.empty());

        assertThrows(UsuarioNotFoundException.class, () ->
            usuarioService.autenticar(404L, "password123"));
    }
}
