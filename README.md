# Aplicacion-de-Citas

```text
citas_backend/
├── broker/events      → Gestión de eventos y mensajería asíncrona (MessageBroker)
├── config             → Configuración de la aplicación (seguridad, WebSocket, etc.)
├── controller         → Capa de controladores (API REST y WebSocket)
├── domain             → Entidades del modelo de dominio (Usuario, Match, Mensaje, etc.)
├── dto                → Objetos de transferencia de datos (DTOs)
├── exception          → Manejadores de excepciones y errores personalizados
├── fake               → Datos de prueba / implementaciones falsas para testing
├── repository         → Acceso a datos (repositorios JPA / interfaces DAO)
├── service            → Lógica de negocio (servicios de aplicación)
└── websocket          → Gestión de sesiones WebSocket (en tiempo real)
```

## Estructura del proyecto
Resumen de carpetas y archivos principales del backend.

```text
citas-backend/
├── pom.xml
└── src/
    ├── main/
    │   ├── java/
    │   │   └── es/upm/fi/citasbackend/
    │   │                   │   CitasBackendApplication.java         ← @SpringBootApplication
    │   │                   │   
    │   │                   ├───broker
    │   │                   │   │   MessageBroker.java
    │   │                   │   │   
    │   │                   │   └───events
    │   │                   │           MensajeNuevoEvent.java
    │   │                   │           UsuarioBloqueadoEvent.java
    │   │                   │           
    │   │                   ├───config
    │   │                   │       AsyncConfig.java
    │   │                   │       JacksonConfig.java
    │   │                   │       
    │   │                   ├───controller
    │   │                   │       BlockingController.java         ← CU4
    │   │                   │       ChatController.java             ← CU1
    │   │                   │       DiscoveryController.java        ← CU3
    │   │                   │       
    │   │                   ├───domain
    │   │                   │       Bloqueo.java
    │   │                   │       Descubrimiento.java
    │   │                   │       Match.java
    │   │                   │       Mensaje.java
    │   │                   │       Perfil.java
    │   │                   │       Swipe.java
    │   │                   │       Usuario.java
    │   │                   │       
    │   │                   ├───dto                     ← Data Transfer Object
    │   │                   │       BloqueoRequestDto.java
    │   │                   │       BloqueoResponseDto.java
    │   │                   │       DescubrimientoResponseDto.java
    │   │                   │       EnviarMensajeRequestDto.java
    │   │                   │       MensajeResponseDto.java
    │   │                   │       PerfilDto.java
    │   │                   │       WsMensajeRequestDto.java
    │   │                   │       
    │   │                   ├───exception
    │   │                   │       BloqueoYaExisteException.java
    │   │                   │       GlobalExceptionHandler.java
    │   │                   │       MatchNoActivoException.java
    │   │                   │       MatchNotFoundException.java
    │   │                   │       PerfilNotFoundException.java
    │   │                   │       UsuarioNotFoundException.java
    │   │                   │       
    │   │                   ├───fake
    │   │                   │       FakeCacheService.java           ← simula Redis
    │   │                   │       FakePushService.java            ← simula FCM/APNs
    │   │                   │       
    │   │                   ├───repository
    │   │                   │       BloqueoRepository.java
    │   │                   │       DescubrimientoRepository.java
    │   │                   │       MatchRepository.java
    │   │                   │       MensajeRepository.java
    │   │                   │       PerfilRepository.java
    │   │                   │       SwipeRepository.java
    │   │                   │       UsuarioRepository.java
    │   │                   │       
    │   │                   ├───service
    │   │                   │       BlockingService.java            ← CU4
    │   │                   │       ChatService.java                ← CU4 async
    │   │                   │       DescubrimientoService.java      ← CU3
    │   │                   │       DiscoveryService.java           ← CU4 async
    │   │                   │       MatchService.java               ← CU1/CU2/CU4
    │   │                   │       MensajeService.java             ← CU1/CU2
    │   │                   │       NotificationService.java        ← CU1/CU2/CU4
    │   │                   │       PerfilService.java              ← CU3
    │   │                   │       SwipeService.java               ← CU3
    │   │                   │       
    │   │                   └───websocket
    │   │                           ChatWebSocketHandler.java       ← CU2
    │   │                           JwtHandshakeInterceptor.java    ← CU2 (FAKE)
    │   │                           WebSocketSessionManager.java    ← CU2
    │   │                           WebSocketConfig.java
    │   │                           
    │   └───resources
    │           application.properties
    │           data.sql
    │
    └── test/
        └── java/
            └── es/upm/fi/citasbackend/
                └── CitasBackendApplicationTests.java
