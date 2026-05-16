package es.upm.fi.citas_backend.service;

import es.upm.fi.citas_backend.repository.SwipeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SwipeService {

    private final SwipeRepository swipeRepository;

    public List<Long> obtenerInteraccionesRealizadas(Long usuarioId) {
        return swipeRepository.findIdsInteractuadosByUsuarioId(usuarioId);
    }
}