package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.domain.Usuario;
import es.upm.fi.citas_backend.dto.UsuarioResponseDto;
import es.upm.fi.citas_backend.exception.UsuarioNotFoundException;
import es.upm.fi.citas_backend.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Servicio de gestión de usuarios.
 * 
 * EXPERTO: UsuarioService conoce toda la lógica de usuario.
 * FABRICACIÓN PURA: Encapsula creación y validación de usuarios.
 * BAJO ACOPLAMIENTO: UsuarioController delega en este servicio.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Registrar un nuevo usuario.
     * 
     * CREADOR: UsuarioService agrega usuarios al sistema.
     */
    @Transactional
    public UsuarioResponseDto registrar(String email, String password) {
        
        // Validar que el email no esté registrado
        if (usuarioRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("El email ya está registrado: " + email);
        }

        // FABRICACIÓN PURA: crear nuevo usuario
        Usuario usuario = Usuario.builder()
            .email(email)
            .passwordHash(passwordEncoder.encode(password))
            .estado(Usuario.EstadoUsuario.ACTIVO)
            .build();

        Usuario saved = usuarioRepository.save(usuario);
        log.info("[UsuarioService] Nuevo usuario registrado → id={}, email={}", saved.getId(), email);

        return new UsuarioResponseDto(saved.getId(), saved.getEmail(), saved.getEstado().toString());
    }

    /**
     * Validar credenciales de login.
     * 
     * EXPERTO: UsuarioService valida autenticación.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDto autenticar(Long usuarioId, String password) {
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        // Validar contraseña
        if (!passwordEncoder.matches(password, usuario.getPasswordHash())) {
            throw new IllegalArgumentException("Contraseña inválida");
        }

        log.info("[UsuarioService] Usuario autenticado → id={}, email={}", usuarioId, usuario.getEmail());
        return new UsuarioResponseDto(usuario.getId(), usuario.getEmail(), usuario.getEstado().toString());
    }

    /**
     * Obtener datos del usuario.
     * 
     * EXPERTO: UsuarioService recupera usuario de BD.
     */
    @Transactional(readOnly = true)
    public UsuarioResponseDto obtenerUsuario(Long usuarioId) {
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        return new UsuarioResponseDto(usuario.getId(), usuario.getEmail(), usuario.getEstado().toString());
    }

    /**
     * Eliminar usuario (desactivar).
     * 
     * EXPERTO: UsuarioService gestiona eliminación de usuario.
     */
    @Transactional
    public void eliminarUsuario(Long usuarioId) {
        
        Usuario usuario = usuarioRepository.findById(usuarioId)
            .orElseThrow(() -> new UsuarioNotFoundException(usuarioId));

        usuario.setEstado(Usuario.EstadoUsuario.INACTIVO);
        usuarioRepository.save(usuario);
        log.info("[UsuarioService] Usuario eliminado (desactivado) → id={}", usuarioId);
    }

    /**
     * Verificar que un usuario existe.
     */
    @Transactional(readOnly = true)
    public boolean existe(Long usuarioId) {
        return usuarioRepository.existsById(usuarioId);
    }
}
