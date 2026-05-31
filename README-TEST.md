# README-TEST

Este archivo resume clases de equivalencia y caminos basicos para la entrega de testing.

## Endpoints (REST Assured)

### POST /usuarios/{usuarioId}/perfil

Clases de equivalencia (particion de equivalencias):

| ID | Tipo | Clase de equivalencia | Entrada representativa | Resultado esperado |
| --- | --- | --- | --- | --- |
| CE-PER-01 | Valida | Usuario existente, sin perfil previo, con nombre no vacio y edad no nula | usuarioId = 1, nombre = "Ana", edad = 25 | Perfil creado correctamente |
| CE-PER-02 | Invalida | Usuario inexistente | usuarioId = 9999, body valido | Error por usuario no encontrado |
| CE-PER-03 | Invalida | usuarioId con formato no numerico | usuarioId = "abc" | Error de peticion incorrecta |
| CE-PER-04 | Invalida | nombre nulo | nombre = null, edad = 25 | Error de validacion |
| CE-PER-05 | Invalida | nombre vacio | nombre = "", edad = 25 | Error de validacion |
| CE-PER-06 | Invalida | nombre formado solo por espacios | nombre = " ", edad = 25 | Error de validacion |
| CE-PER-07 | Invalida | edad nula | nombre = "Ana", edad = null | Error de validacion |
| CE-PER-08 | Invalida | Usuario que ya tiene perfil creado | mismo usuarioId usado dos veces | Error por perfil ya existente |
| CE-PER-09 | Invalida | JSON mal formado | JSON sintacticamente incorrecto | Error de peticion incorrecta |

Casos de prueba derivados:

| ID | Clase cubierta | Descripcion del caso | Entrada | Resultado esperado |
| --- | --- | --- | --- | --- |
| CP-PER-01 | CE-PER-01 | Crear perfil correctamente | POST /usuarios/1/perfil con nombre = "Ana" y edad = 25 | Respuesta correcta de creacion |
| CP-PER-02 | CE-PER-02 | Crear perfil para un usuario inexistente | POST /usuarios/9999/perfil con body valido | Error por usuario no encontrado |
| CP-PER-03 | CE-PER-03 | Crear perfil con usuarioId no numerico | POST /usuarios/abc/perfil | Error de peticion incorrecta |
| CP-PER-04 | CE-PER-04 | Crear perfil con nombre nulo | Body con nombre = null y edad = 25 | Error de validacion |
| CP-PER-05 | CE-PER-05 | Crear perfil con nombre vacio | Body con nombre = "" y edad = 25 | Error de validacion |
| CP-PER-06 | CE-PER-06 | Crear perfil con nombre formado solo por espacios | Body con nombre = " " y edad = 25 | Error de validacion |
| CP-PER-07 | CE-PER-07 | Crear perfil con edad nula | Body con nombre = "Ana" y edad = null | Error de validacion |
| CP-PER-08 | CE-PER-08 | Crear perfil duplicado | Crear dos veces el perfil del mismo usuario | Error por perfil ya existente |
| CP-PER-09 | CE-PER-09 | Enviar JSON mal formado | Body JSON incorrecto | Error de peticion incorrecta |

Nota: no se considera como clase invalida una edad negativa porque el DTO actual solo valida que edad no sea nula con @NotNull. Al no existir una anotacion de rango (por ejemplo, @Min o @Positive), no se asume error de validacion.

### POST /usuarios/login

Clases de equivalencia (particion de equivalencias):

| ID | Tipo | Clase de equivalencia | Entrada representativa | Resultado esperado |
| --- | --- | --- | --- | --- |
| CE-LOG-01 | Valida | Usuario existente y contrasena correcta | usuarioId = 1, password correcta | Login correcto |
| CE-LOG-02 | Invalida | Usuario inexistente | usuarioId = 9999, password cualquiera | Error por usuario no encontrado |
| CE-LOG-03 | Invalida | Usuario existente con contrasena incorrecta | usuarioId = 1, password incorrecta | Error de autenticacion |
| CE-LOG-04 | Invalida | usuarioId nulo | usuarioId = null, password no nula | Error de validacion |
| CE-LOG-05 | Invalida | password nula | usuarioId = 1, password = null | Error de validacion |
| CE-LOG-06 | Invalida | usuarioId con tipo incorrecto | usuarioId = "abc" | Error de peticion incorrecta |
| CE-LOG-07 | Invalida | JSON mal formado | JSON sintacticamente incorrecto | Error de peticion incorrecta |

Casos de prueba derivados:

| ID | Clase cubierta | Descripcion del caso | Entrada | Resultado esperado |
| --- | --- | --- | --- | --- |
| CP-LOG-01 | CE-LOG-01 | Login correcto | Usuario existente y password correcta | Respuesta correcta de autenticacion |
| CP-LOG-02 | CE-LOG-02 | Login con usuario inexistente | usuarioId = 9999 y password cualquiera | Error por usuario no encontrado |
| CP-LOG-03 | CE-LOG-03 | Login con contrasena incorrecta | Usuario existente y password incorrecta | Error de autenticacion |
| CP-LOG-04 | CE-LOG-04 | Login con usuarioId nulo | Body con usuarioId = null | Error de validacion |
| CP-LOG-05 | CE-LOG-05 | Login con password nula | Body con password = null | Error de validacion |
| CP-LOG-06 | CE-LOG-06 | Login con usuarioId de tipo incorrecto | Body con usuarioId = "abc" | Error de peticion incorrecta |
| CP-LOG-07 | CE-LOG-07 | Login con JSON mal formado | Body JSON incorrecto | Error de peticion incorrecta |

Nota: no se considera password vacio como clase invalida de validacion automatica, ya que el DTO usa @NotNull y no @NotBlank. Una cadena vacia no es nula, por lo que no se asume error de validacion automatica.

## Metodos (JUnit)

### BlockingService.bloquearUsuario(Long bloqueadorId, Long bloqueadoId)

Diagrama de flujo (caminos basicos):

| ID | Camino | Situacion probada | Resultado esperado |
| --- | --- | --- | --- |
| CB-BLOQ-01 | 1-2-3-13 | El bloqueo ya existe | Se lanza BloqueoYaExisteException |
| CB-BLOQ-02 | 1-2-4-5-13 | No existe bloqueo previo, pero no existe el usuario bloqueador | Se lanza UsuarioNotFoundException |
| CB-BLOQ-03 | 1-2-4-6-7-13 | No existe bloqueo previo, existe el bloqueador, pero no existe el bloqueado | Se lanza UsuarioNotFoundException |
| CB-BLOQ-04 | 1-2-4-6-8-9-10-11-12-13 | No existe bloqueo previo y ambos usuarios existen | Se crea el bloqueo y se devuelve BloqueoResponseDto |

Casos implementados en BlockingServiceTest:
- bloquearUsuario_crearBloqueoValido
- bloquearUsuario_rechazaBloqueoYaExistente
- bloquearUsuario_lanzaSiBloqueadorNoExiste
- bloquearUsuario_lanzaSiBloqueadoNoExiste

### SwipeService.crearSwipe(Long remitenteId, Long destinatarioId, AccionSwipe accion)

Diagrama de flujo (caminos basicos):

| ID | Camino | Situacion probada | Resultado esperado |
| --- | --- | --- | --- |
| CB-SWIPE-01 | 1-2-3-14 | El usuario remitente no existe | Se lanza UsuarioNotFoundException |
| CB-SWIPE-02 | 1-2-4-5-14 | El remitente existe, pero el destinatario no existe | Se lanza UsuarioNotFoundException |
| CB-SWIPE-03 | 1-2-4-6-7-8-13-14 | Ambos usuarios existen y la accion no es ACEPTADO | Se guarda el swipe y devuelve false |
| CB-SWIPE-04 | 1-2-4-6-7-8-9-10-13-14 | La accion es ACEPTADO, pero no existe swipe reciproco | Se guarda el swipe y devuelve false |
| CB-SWIPE-05 | 1-2-4-6-7-8-9-10-13-14 | La accion es ACEPTADO, existe swipe reciproco, pero no es ACEPTADO | Se guarda el swipe y devuelve false |
| CB-SWIPE-06 | 1-2-4-6-7-8-9-10-11-12-14 | La accion es ACEPTADO, existe swipe reciproco aceptado | Se crea un match y devuelve true |

Casos implementados en SwipeServiceTest:
- crearSwipe_aceptadoConReciprocidad_creaMatch
- crearSwipe_aceptadoConReciprocidadNoAceptada_noCreaMatch
- crearSwipe_aceptadoSinReciprocidad_noCreaMatch
- crearSwipe_rechazado_noBuscaReciprocidad
- crearSwipe_lanzaSiUsuarioNoExiste
- crearSwipe_lanzaSiDestinatarioNoExiste

## Cobertura (JaCoCo)

El plugin de JaCoCo ya esta configurado en el pom. Para los metodos seleccionados se espera 100% de cobertura.
