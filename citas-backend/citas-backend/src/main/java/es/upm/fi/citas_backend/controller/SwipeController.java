package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.domain.Swipe;
import es.upm.fi.citas_backend.dto.SwipeRequestDto;
import es.upm.fi.citas_backend.service.SwipeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;

@RestController
@RequestMapping("/swipes")
@RequiredArgsConstructor
public class SwipeController {

    private final SwipeService swipeService;

    @PostMapping
    public ResponseEntity<Object> crearSwipe(@Valid @RequestBody SwipeRequestDto req) {
        Swipe.AccionSwipe accion = Swipe.AccionSwipe.valueOf(req.getAccion().toUpperCase());
        
        boolean matchCreado = swipeService.crearSwipe(
            req.getUsuarioOrigen(), 
            req.getUsuarioDestino(), 
            accion
        );

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of(
                    "mensaje", "Swipe registrado correctamente",
                    "matchCreado", matchCreado
                ));
    }
}
