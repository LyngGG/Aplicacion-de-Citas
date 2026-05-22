package es.upm.fi.citas_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Configuración de seguridad para encriptación de contraseñas.
 */
@Configuration
public class SecurityConfig {

    /**
     * Define el bean de PasswordEncoder usando BCrypt.
     * BCrypt es seguro contra ataques de fuerza bruta con factor de trabajo adaptable.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
