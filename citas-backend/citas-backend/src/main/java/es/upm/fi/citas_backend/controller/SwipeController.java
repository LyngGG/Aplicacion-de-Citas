package es.upm.fi.citas_backend.controller;

import es.upm.fi.citas_backend.dto.SwipeRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/swipes")
@RequiredArgsConstructor
public class SwipeController {

    @PostMapping
    public ResponseEntity<Object> crearSwipe(@Valid @RequestBody SwipeRequestDto req) {
        // TODO: Registrar swipe, verificar reciprocidad y crear match si aplica
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new Object() {
                public Long id = 1L;
                public String mensaje = "Swipe registrado correctamente";
            });
    }
}
