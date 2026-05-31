# Aplicación de Citas — Backend

**Proyecto de Ingeniería del Software — UPM**

Backend de una aplicación de citas basada en arquitectura multicapa con Spring Boot 4.0.6 y Java 21.

---

## Tabla de Contenidos

- [Descripción General](#descripción-general)
- [Implementaciones para cada caso de uso](#implementaciones-para-cada-caso-de-uso)
- [Tecnologías y Dependencias](#tecnologías-y-dependencias)
- [Arquitectura del Proyecto](#arquitectura-del-proyecto)
- [Estructura de Directorios](#estructura-de-directorios)
- [Instalación y Configuración](#instalación-y-configuración)
- [Ejecución](#ejecución)
- [Endpoints de la API](#endpoints-de-la-api)
- [Base de Datos](#base-de-datos)
- [Seguridad](#seguridad)
- [Testing](#testing)

---

## Descripción General

Este backend implementa un sistema completo de matching de perfiles de citas con las siguientes funcionalidades:

- Autenticación y registro de usuarios
- Descubrimiento de perfiles con filtros y búsqueda
- Sistema de swipes (likes y rechazos)
- Matching automático cuando dos usuarios se dan like mutuamente
- Chat en tiempo real mediante WebSocket
- Sistema de notificaciones
- Bloqueo de usuarios
- Gestión de perfiles personalizados

---

## Implementaciones para cada caso de uso

### CU1: Registro y Login

- Registro de nuevos usuarios con validación de email
- Login con autenticación mediante contraseña
- Encriptación BCrypt de contraseñas
- Gestión de estados de usuario (ACTIVO, INACTIVO, BANEADO)

### CU2: Conversación en Tiempo Real

- Chat mediante WebSocket (/ws/chat)
- Mensajes persistentes en base de datos
- Notificaciones de mensajes nuevos
- Solo usuarios con match pueden chatear

### CU3: Swipes y Matching

- Sistema de swipes: LIKE o REJECT
- Descubrimiento de perfiles disponibles
- Matching automático cuando hay like mutuo
- Historial de swipes

### CU4: Gestión de Perfil y Bloqueos

- Crear y editar perfil personal (fotos, bio, intereses)
- Bloquear/desbloquear usuarios
- Ver perfil de otros usuarios
- Filtros de búsqueda (edad, ubicación, intereses)

---

## Tecnologías y Dependencias

Componentes principales del stack tecnológico:

Dependencias principales:

- spring-boot-starter-web
- spring-boot-starter-websocket
- spring-boot-starter-security
- spring-boot-starter-data-jpa
- spring-boot-starter-validation
- h2 (base de datos en memoria)
- jackson (serialización JSON)
- lombok (reducción de boilerplate)
- spring-boot-starter-test (testing)

Versiones Clave:
- Java: 21 (LTS)
- Spring Boot: 4.0.6
- Maven: 3.9+
- Base de Datos: H2 (en memoria para desarrollo)

---

## Arquitectura del Proyecto

El backend sigue una arquitectura multicapa limpia y escalable:

CAPA DE PRESENTACIÓN
(Controllers + REST Endpoints + WebSocket)
         |
         v
CAPA DE SERVICIOS
(Business Logic + Validación)
         |
         v
CAPA DE DATOS
(Repositories + JPA Entities)
         |
         v
BASE DE DATOS (H2)

Patrones Utilizados:

1. MVC (Model-View-Controller): Separación clara de responsabilidades
2. DTO (Data Transfer Object): Encapsulación de datos en requests/responses
3. Repository Pattern: Abstracción de acceso a datos
4. Service Layer: Lógica de negocio centralizada
5. Async Processing: Pool de threads para operaciones no bloqueantes
6. WebSocket Handler: Comunicación bidireccional en tiempo real

---

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
    │   │                   │       SecurityConfig.java
    │   │                   │       WebSocketConfig.java
    │   │                   │       
    │   │                   ├───controller
    │   │                   │       BlockingController.java         ← CU4
    │   │                   │       ChatController.java             ← CU1
    │   │                   │       DiscoveryController.java        ← CU3
    │   │                   │       MatchController.java
    │   │                   │       NotificacionController.java
    │   │                   │       PerfilController.java
    │   │                   │       SwipeController.java
    │   │                   │       UsuarioController.java
    │   │                   │       WebSocketChatController.java
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
    │   │                   │       MatchResponseDto.java
    │   │                   │       MensajeResponseDto.java
    │   │                   │       NotificacionResponseDto.java
    │   │                   │       PerfilDto.java
    │   │                   │       PerfilRequestDto.java
    │   │                   │       SwipeRequestDto.java
    │   │                   │       UsuarioLoginRequestDto.java
    │   │                   │       UsuarioRegistroRequestDto.java
    │   │                   │       UsuarioResponseDto.java
    │   │                   │       WebSocketMensajeDto.java
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
    │   │                   │       UsuarioService.java
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
    └───test
        ├───java
        │   └───es
        │       └───upm
        │           └───fi
        │               └───citas_backend
        │                   │   CitasBackendApplicationTests.java
        │                   │   
        │                   ├───controller
        │                   │       IntegrationTests.java
        │                   │       
        │                   ├───dto
        │                   │       DtoValidationTests.java
        │                   │       
        │                   └───service
        │                           BlockingServiceTest.java
        │                           MatchServiceTest.java
        │                           MensajeServiceTest.java
        │                           NotificationServiceTest.java
        │                           PerfilServiceTest.java
        │                           ServiceDtoCoherenceTests.java
        │                           SwipeServiceTest.java
        │                           UsuarioServiceTest.java
        │                           
        └───resources
                application.properties
```
---

## Instalación y Configuración

### Requisitos Previos

- Java Development Kit (JDK) 21 o superior
- Maven 3.9 o superior
- Git
- IDE recomendado: IntelliJ IDEA o VS Code con Java extensions

### Pasos de Instalación

#### 1. Clonar el repositorio

```bash
git clone https://github.com/LyngGG/Aplicacion-de-Citas.git
cd AplicacionCitas/citas-backend/citas-backend
```

#### 2. Verificar instalación de Java

```bash
java -version
# Debe mostrar: openjdk version "21" o superior
```

#### 3. Descargar dependencias con Maven

```bash
# Opción 1: Usando Maven Wrapper (incluido)
./mvnw clean install

# Opción 2: Si tienes Maven instalado
mvn clean install
```

#### 4. (Opcional) Limpiar caché de Maven si hay problemas

```bash
./mvnw clean
rm -rf ~/.m2/repository/  # Linux/Mac
rmdir /s %USERPROFILE%\.m2\repository  # Windows
```

---

## Ejecución

### Opción 1: Ejecutar con Maven (Recomendado)

```bash
# Terminal en: AplicacionCitas/citas-backend/citas-backend/

./mvnw spring-boot:run
# o en Windows:
mvnw.cmd spring-boot:run
```

Salida esperada:
```
  .   ____          _            __ _ _
 /\\ / ___'_ __ _ _(_)_ __  __ _ \ \ \ \
( ( )\___ | '_ | '_| | '_ \/ _` | \ \ \ \
 \\/  ___)| |_)| | | | | || (_| |  ) ) ) )
  '  |____| .__|_| |_|_| |_|\__, | / / / /
 =========|_|==============|___/=/_/_/_/
 :: Spring Boot ::                (v4.0.6)

2026-05-24 10:30:45.123  INFO 12345 --- [  main] e.u.f.citas_backend.CitasBackendApplication : Starting CitasBackendApplication...
2026-05-24 10:30:46.456  INFO 12345 --- [  main] e.u.f.citas_backend.CitasBackendApplication : Started CitasBackendApplication in 1.2 seconds
```

### Opción 2: Compilar y ejecutar JAR

```bash
# Compilar
./mvnw clean package

# Ejecutar el JAR
java -jar target/citas-backend-0.0.1-SNAPSHOT.jar
```

### Verificar que está ejecutando

```bash
# En otra terminal:
curl http://localhost:8080/usuarios/health
# Deberías recibir: 200 OK
```

---

## Endpoints de la API

### **Autenticación (CU1)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/usuarios/registro` | Registrar nuevo usuario |
| `POST` | `/usuarios/login` | Login de usuario |
| `GET` | `/usuarios/{id}` | Obtener datos usuario |
| `PUT` | `/usuarios/{id}` | Actualizar usuario |

**Ejemplo: Registro**
```bash
curl -X POST http://localhost:8080/usuarios/registro \
  -H "Content-Type: application/json" \
  -d '{
    "email": "juan@example.com",
    "password": "Password123!",
    "nombre": "Juan"
  }'
```

### **Perfil (CU4)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/perfil/{usuarioId}` | Obtener perfil |
| `PUT` | `/perfil/{usuarioId}` | Editar perfil |
| `POST` | `/perfil/{usuarioId}/foto` | Subir foto |

### **Descubrimiento (CU3)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/descubrimiento/{usuarioId}` | Obtener perfiles para swipear |
| `GET` | `/descubrimiento/filtros` | Aplicar filtros |

### **Swipes (CU3)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/swipes` | Enviar like/reject |
| `GET` | `/swipes/{usuarioId}` | Historial de swipes |

### **Matches (CU3)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/matches/{usuarioId}` | Obtener matches del usuario |
| `GET` | `/matches/{usuarioId}/{otroUsuarioId}` | Verificar si hay match |

### **Chat y Mensajes (CU2)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/chat/{usuarioId}/{otroUsuarioId}` | Historial de mensajes |
| `POST` | `/chat/enviar` | Enviar mensaje HTTP |
| **WS** | `/ws/chat` | WebSocket para chat en tiempo real |

**Ejemplo: Conexión WebSocket**
```javascript
const ws = new WebSocket('ws://localhost:8080/ws/chat');

ws.onopen = () => {
  ws.send(JSON.stringify({
    usuarioId: 1,
    otroUsuarioId: 2,
    contenido: "¡Hola!"
  }));
};

ws.onmessage = (event) => {
  console.log("Mensaje recibido:", event.data);
};
```

### **Bloqueos (CU4)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `POST` | `/bloqueos` | Bloquear usuario |
| `DELETE` | `/bloqueos/{usuarioId}/{usuarioBloqueadoId}` | Desbloquear |
| `GET` | `/bloqueos/{usuarioId}` | Lista de bloqueados |

### **Notificaciones (CU4)**

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/notificaciones/{usuarioId}` | Obtener notificaciones |
| `PUT` | `/notificaciones/{id}/leida` | Marcar como leída |

---

## Base de Datos

### **Motor de BD: H2 (En Memoria)**

Por desarrollo, usamos **H2 Database** que se ejecuta en memoria:

- **URL**: `jdbc:h2:mem:citasdb`
- **Usuario**: `sa`
- **Contraseña**: (vacía)

### **Consola H2**

Accede a la consola gráfica en: **http://localhost:8080/h2-console**

```
Driver Class: org.h2.Driver
JDBC URL: jdbc:h2:mem:citasdb
User Name: sa
Password: (dejar en blanco)
```

### **Entidades y Relaciones**

```
┌─────────────┐
│  USUARIOS   │ (Usuarios registrados)
├─────────────┤
│ id (PK)     │◄────────┐
│ email       │         │
│ password    │         │
│ estado      │         │
└─────────────┘         │
      │                 │
      │◄────────────────┼────────────────────┐
      │                 │                    │
      ▼                 │                    │
┌──────────────┐  ┌─────────────┐  ┌────────────┐
│  PERFILES    │  │  SWIPES     │  │  BLOQUES   │
├──────────────┤  ├─────────────┤  ├────────────┤
│ id (PK)      │  │ id (PK)     │  │ id (PK)    │
│ usuarioId(FK)│  │ usuarioId   │  │ usuarioId  │
│ bio          │  │ perfilId    │  │ bloqueadoId│
│ fotos        │  │ tipo (LIKE) │  │ fecha      │
│ ubicación    │  │ fecha       │  └────────────┘
└──────────────┘  └─────────────┘
      │                 │
      │                 ▼
      │           ┌─────────────┐
      │           │  MATCHES    │
      │           ├─────────────┤
      │           │ id (PK)     │
      │           │ usuario1Id  │
      │           │ usuario2Id  │
      │           │ fechaMatch  │
      │           └─────────────┘
      │                 │
      └────────────┬────┘
                   │
                   ▼
            ┌────────────────┐
            │  MENSAJES      │
            ├────────────────┤
            │ id (PK)        │
            │ matchId (FK)   │
            │ usuarioId      │
            │ contenido      │
            │ timestamp      │
            └────────────────┘

            ┌──────────────────┐
            │  DESCUBRIMIENTO  │
            ├──────────────────┤
            │ id (PK)          │
            │ usuarioId (FK)   │
            │ perfilVisto      │
            │ fecha            │
            └──────────────────┘
```

### **Script de Inicialización (data.sql)**

El archivo `src/main/resources/data.sql` contiene:

- Usuarios de prueba preinsertados
- Perfiles de ejemplo
- Relaciones de matches y swipes

Se ejecuta automáticamente en el startup gracias a:
```properties
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
```

---

## 🔒 Seguridad

### **Autenticación y Autorización**

```
┌──────────────────────────────────────────────┐
│  REQUEST CON CREDENCIALES                   │
└────────────────┬─────────────────────────────┘
                 │
                 ▼
         ┌──────────────────┐
         │  Validación      │
         │  de email/pwd    │
         └────────┬─────────┘
                  │
                  ▼
         ┌──────────────────┐
         │  Encriptación    │
         │  BCrypt          │
         └────────┬─────────┘
                  │
                  ▼
         ┌──────────────────┐
         │  Retornar Token  │
         │  de sesión       │
         └──────────────────┘
```

### **Encriptación de Contraseñas**

Usamos **BCrypt** (implementado en `SecurityConfig.java`):

```java
PasswordEncoder encoder = new BCryptPasswordEncoder();
String hashedPassword = encoder.encode("miContraseña");
```

**Ventajas**:
- Función hash adaptativa (es más lenta con el tiempo)
- Salt automático para cada contraseña
- Resistencia contra ataques de fuerza bruta

### **Validación de DTOs**

Se validan automáticamente usando `@Valid`:

```java
@PostMapping("/usuarios/registro")
public ResponseEntity<?> registro(@Valid @RequestBody UsuarioRegistroRequestDto dto) {
    // Spring valida automáticamente:
    // - @NotBlank
    // - @Email
    // - @Size
    // - Custom validators
}
```

---

## 🧪 Testing

### **Cobertura de Tests**

El proyecto incluye **tests**:

```
Cobertura: ~95%

Tests unitarios: Services
Tests de integración: Controllers REST
Tests de DTOs: Validación
Tests de coherencia: DTO-Entity
```

### **Ejecutar Tests**

```bash
# Ejecutar todos los tests
./mvnw test

# Ejecutar una clase de test específica
./mvnw test -Dtest=UsuarioServiceTest

# Ejecutar un método de test específico
./mvnw test -Dtest=UsuarioServiceTest#testRegistroValido

# Generar reporte de cobertura (JaCoCo)
./mvnw clean test jacoco:report
# Abrirá: target/site/jacoco/index.html
```

### **Clases de Test Principales**

| Test | Descripción |
|------|-------------|
| `CitasBackendApplicationTests` | Test de contexto Spring |
| `IntegrationTests` | Tests de REST controllers |
| `DtoValidationTests` | Validación de DTOs |
| `UsuarioServiceTest` | Lógica de usuarios |
| `MatchServiceTest` | Algoritmo de matching |
| `SwipeServiceTest` | Sistema de swipes |
| `MensajeServiceTest` | Persistencia de mensajes |
| `NotificationServiceTest` | Notificaciones |
| `BlockingServiceTest` | Sistema de bloqueos |
| `PerfilServiceTest` | Gestión de perfiles |

### **Ejemplo: Ejecutar test y ver reporte**

```bash
./mvnw clean test jacoco:report
open target/site/jacoco/index.html  # Mac
start target/site/jacoco/index.html # Windows
xdg-open target/site/jacoco/index.html  # Linux
```

---

## Propiedades de Configuración

Todas las propiedades están en `src/main/resources/application.properties`:

```properties
# ── Servidor ─────────────────────────
spring.application.name=citas-backend
server.port=8080

# ── Base de Datos (H2) ───────────────
spring.datasource.url=jdbc:h2:mem:citasdb;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

# Consola H2
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# ── JPA / Hibernate ─────────────────
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true

# ── Thread Pool Async ──────────────
spring.task.execution.pool.core-size=4
spring.task.execution.pool.max-size=8
```

---

## Configuración de Componentes Clave

### **1. AsyncConfig.java**

Define el pool de threads para operaciones asíncronas:

```java
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {
    
    @Bean(name = "taskExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(100);
        return executor;
    }
}
```

**Uso**: Procesamiento no bloqueante de notificaciones y mensajes.

### **2. JacksonConfig.java**

Configura serialización/deserialización JSON:

```java
@Configuration
public class JacksonConfig {
    
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return mapper;
    }
}
```

**Beneficio**: Las fechas se serializan como ISO-8601 strings.

### **3. SecurityConfig.java**

Define el encoder de contraseñas:

```java
@Configuration
public class SecurityConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### **4. WebSocketConfig.java**

Configura el endpoint WebSocket:

```java
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {
    
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(webSocketChatController, "/ws/chat")
            .setAllowedOrigins("*");
    }
}
```

**Endpoint**: `ws://localhost:8080/ws/chat`

---

## Troubleshooting

### **Problema: "Port 8080 already in use"**

```bash
# Encontrar proceso en puerto 8080
# Windows:
netstat -ano | findstr :8080

# Mac/Linux:
lsof -i :8080

# Cambiar puerto (en application.properties):
server.port=8081
```

### **Problema: "Maven build fails"**

```bash
# Limpiar Maven cache
./mvnw clean

# Descargar dependencias nuevamente
./mvnw dependency:resolve
```

### **Problema: "H2 console no accesible"**

Verifica que `spring.h2.console.enabled=true` esté en `application.properties`

### **Problema: "Tests fallan"**

```bash
# Ejecutar tests con más detalles
./mvnw test -X

# Ejecutar un test específico
./mvnw test -Dtest=UsuarioServiceTest -X
```

---

## Documentación Adicional

- [Documentos PlantUML](../): Diagramas de casos de uso
- `CU1.puml` - Diagrama caso de uso: Registro y login
- `CU2.puml` - Diagrama caso de uso: Chat en tiempo real
- `CU3.puml` - Diagrama caso de uso: Swipes y matching
- `CU4.puml` - Diagrama caso de uso: Gestión de perfil
- `dominio.puml` - Diagrama de entidades




                
---

**Última actualización**: 24 de Mayo, 2026
**Versión**: 0.0.1-SNAPSHOT