# Prueba técnica - API de una entidad financiera

Aplicación para los trabajadores de una entidad financiera. Permite administrar clientes,
crear productos financieros (cuentas de ahorro y corrientes), registrar movimientos
transaccionales sobre esos productos y consultar los estados de cuenta.

---

## Contenido

1. [Tecnologías](#tecnologías)
2. [Cómo ejecutar el proyecto](#cómo-ejecutar-el-proyecto)
3. [Arquitectura](#arquitectura)
4. [Patrones de diseño](#patrones-de-diseño)
5. [Principios SOLID](#principios-solid)
6. [Principio ACID](#principio-acid)
7. [Base de datos: DDL y DML](#base-de-datos-ddl-y-dml)
8. [API REST](#api-rest)
9. [Reglas de negocio](#reglas-de-negocio)
10. [Pruebas](#pruebas)
11. [Control de versiones](#control-de-versiones)
12. [Servicios en la nube](#servicios-en-la-nube)
13. [Front](#front)
14. [Estado del proyecto](#estado-del-proyecto)

---

## Tecnologías

| Componente | Versión |
| --- | --- |
| Java | 21 (LTS) |
| Spring Boot | 4.1.0 |
| Spring Data JPA / Hibernate | 7.4 |
| PostgreSQL | 16 (en contenedor Docker) |
| Maven | 3.9 (incluido con el wrapper `mvnw`) |
| JUnit 5, Mockito, AssertJ | Pruebas |
| H2 | Base de datos en memoria, solo para pruebas |
| Lombok | Reducción de código repetitivo |

---

## Cómo ejecutar el proyecto

### Requisitos previos

- Docker y Docker Compose
- JDK 21, únicamente si se quiere ejecutar la aplicación fuera de contenedor

No es necesario instalar Maven: el proyecto incluye el wrapper (`mvnw`).

### Opción A: todo en contenedores

Desde la raíz del repositorio:

```bash
docker compose up -d
```

Levanta dos contenedores: `banco-postgres` con PostgreSQL 16 y `banco-app` con la
aplicación. La API queda disponible en `http://localhost:8080`.

La aplicación no arranca hasta que la base de datos responde: el servicio `postgres` declara
una comprobación de salud con `pg_isready` y el servicio `app` depende de ella con
`condition: service_healthy`. Esto evita el error típico de que la aplicación intente
conectarse antes de que el motor esté listo.

Comandos útiles:

```bash
docker compose ps                 # estado y salud de los contenedores
docker compose logs -f app        # registros de la aplicación
docker compose build app          # reconstruir la imagen tras cambiar el código
docker compose down               # detener (los datos permanecen en el volumen)
```

### Opción B: base de datos en contenedor y aplicación local

Es la forma cómoda de trabajar mientras se desarrolla, porque no hay que reconstruir la
imagen en cada cambio:

```bash
docker compose up -d postgres
cd banco
./mvnw spring-boot:run
```

### Cómo está construida la imagen

El `Dockerfile` usa una construcción en dos etapas:

1. **Compilación**: parte de `maven:3.9-eclipse-temurin-21`, descarga las dependencias y
   empaqueta el jar. Como el `pom.xml` se copia antes que el código fuente, Docker reutiliza
   la capa de dependencias mientras el pom no cambie.
2. **Ejecución**: parte de `eclipse-temurin:21-jre-alpine` y solo recibe el jar ya
   construido. La imagen final no contiene el código fuente, ni Maven, ni el compilador.

El resultado son 487 MB frente a los 809 MB que ocupa la imagen de compilación.

Otras dos decisiones sobre la imagen:

- El proceso corre con un usuario propio (`banco`), no como `root`. Si alguien lograra
  ejecutar código dentro del contenedor, no tendría privilegios de administrador.
- Se declara un `HEALTHCHECK` que consulta un endpoint real de la API. Responder 200 implica
  que la aplicación arrancó y que la conexión con la base de datos funciona, no solo que el
  proceso existe.

### Ejecutar las pruebas

```bash
cd banco
./mvnw test
```

Las pruebas usan H2 en memoria, así que **no requieren que el contenedor esté levantado**.

### Configuración por variables de entorno

Los datos de conexión tienen valores por defecto que coinciden con el `docker-compose.yml`,
de modo que el proyecto funciona recién clonado. Se pueden sobrescribir sin tocar el código:

| Variable | Valor por defecto |
| --- | --- |
| `DB_URL` | `jdbc:postgresql://localhost:5432/banco_db` |
| `DB_USER` | `banco_user` |
| `DB_PASSWORD` | `banco_pass` |

```bash
DB_URL=jdbc:postgresql://otro-host:5432/banco_db ./mvnw spring-boot:run
```

### CORS

El front se sirve desde un puerto distinto al de la API, y para el navegador eso son dos
orígenes diferentes: sin autorización explícita bloquea la respuesta por la política del
mismo origen. La configuración está en `ConfiguracionCors` y los orígenes autorizados se
declaran por propiedad, no en el código:

```properties
app.cors.origenes-permitidos=http://localhost:4200
```

Se admiten varios separados por coma. Al desplegar basta con cambiar esa propiedad por el
dominio real del front:

```bash
./mvnw spring-boot:run --app.cors.origenes-permitidos=https://mi-front.com
```

Se autorizan los métodos que usa la API (GET, POST, PUT, PATCH, DELETE y OPTIONS) solo bajo
la ruta `/api/**`, y se permite al navegador guardar en caché la verificación previa durante
una hora para que no la repita en cada petición. No se habilita el envío de credenciales
porque la API no usa cookies ni sesiones.

---

## Arquitectura

Se utilizó **arquitectura hexagonal** (puertos y adaptadores), una de las dos opciones que
plantea el enunciado.

### Idea central

El núcleo de la aplicación (dominio y casos de uso) no conoce ni a Spring, ni a JPA, ni a
HTTP. Todo lo externo entra y sale a través de **puertos**, que son interfaces definidas por
el dominio; la infraestructura provee **adaptadores** que las implementan.

```
                    ENTRADA                                    SALIDA
        ┌──────────────────────────┐              ┌──────────────────────────┐
        │  Adaptador REST          │              │  Adaptador de            │
        │  (Controllers, DTOs)     │              │  persistencia (JPA)      │
        └────────────┬─────────────┘              └────────────┬─────────────┘
                     │ implementa                              │ implementa
                     │                                         │
              ┌──────┴───────┐                        ┌────────┴───────┐
              │ Puerto IN    │                        │ Puerto OUT     │
              │ CasosDeUso   │                        │ RepositoryPort │
              └──────┬───────┘                        └────────┬───────┘
                     │                                         │
            ┌────────┴─────────────────────────────────────────┴────────┐
            │                        NÚCLEO                             │
            │   Servicios de aplicación  +  Modelo de dominio           │
            │   (orquestación)              (reglas de negocio)         │
            │   Sin dependencias de frameworks                          │
            └───────────────────────────────────────────────────────────┘
```

La **regla de dependencias** es que las flechas siempre apuntan hacia adentro: la
infraestructura depende del dominio, nunca al revés.

### Estructura de carpetas

```
banco/src/main/java/com/pruebatecnica/banco/
│
├── domain/                          NÚCLEO. No importa Spring ni JPA.
│   ├── model/                       Entidades de negocio con sus reglas:
│   │                                Cliente, Producto, Transaccion y sus enums.
│   ├── port/
│   │   ├── in/                      Qué puede hacer la aplicación (casos de uso).
│   │   └── out/                     Qué necesita de afuera (persistencia).
│   └── exception/                   Excepciones propias del negocio.
│
├── application/
│   └── service/                     Orquesta los casos de uso: valida, coordina
│                                    puertos y delimita las transacciones.
│
└── infrastructure/
    └── adapter/
        ├── in/rest/                 Controladores, DTOs de entrada y salida,
        │                            manejador global de excepciones.
        └── out/persistence/         Entidades JPA, repositorios de Spring Data
                                     y los adaptadores que traducen entidad <-> dominio.
```

### Para qué sirve cada carpeta

- **`domain/model`**: donde vive el negocio. `Producto` sabe que una cuenta de ahorros no
  puede quedar en saldo negativo y que solo se cancela con saldo cero. Estas clases se
  pueden probar sin levantar nada.
- **`domain/port/in`**: el contrato de lo que la aplicación ofrece. El controlador depende
  de esta interfaz, no de la implementación.
- **`domain/port/out`**: el contrato de lo que la aplicación necesita. El dominio dice
  "necesito guardar un producto"; no dice cómo ni dónde.
- **`application/service`**: implementa los casos de uso. Aquí está `@Transactional`, la
  coordinación entre varios puertos y las validaciones que requieren consultar el
  repositorio (por ejemplo, que el número de identificación no esté repetido).
- **`infrastructure/adapter/in/rest`**: traduce HTTP a llamadas de casos de uso. Los DTOs
  evitan exponer el modelo interno en la API.
- **`infrastructure/adapter/out/persistence`**: traduce el modelo de dominio a entidades JPA
  y viceversa. Es el único lugar del proyecto donde aparecen anotaciones de JPA.

### Correspondencia con las capas que pide el enunciado

| Capa solicitada | Ubicación en este proyecto |
| --- | --- |
| Entity | `infrastructure/adapter/out/persistence/*Entity.java` |
| Repository | `infrastructure/adapter/out/persistence/*JpaRepository.java` |
| Service | `application/service/*Service.java` |
| Controller | `infrastructure/adapter/in/rest/*Controller.java` |

### Recorrido de una petición

`POST /api/productos`

1. `ProductoController` recibe el JSON y lo valida con Bean Validation (`ProductoRequest`).
2. Convierte el DTO al modelo de dominio y llama a `ProductoCasosDeUso.crear(...)`.
3. `ProductoService` abre la transacción, verifica que el cliente exista, pide al dominio
   que se inicialice y se valide, y genera el número de cuenta único.
4. Llama a `ProductoRepositoryPort.guardar(...)`.
5. `ProductoRepositoryAdapter` convierte el dominio a `ProductoEntity` y usa Spring Data.
6. La respuesta vuelve convertida a `ProductoResponse`, con estado HTTP 201.

---

## Patrones de diseño

| Patrón | Dónde y para qué |
| --- | --- |
| **Puertos y adaptadores** | Toda la aplicación. Permite cambiar PostgreSQL por otro motor, o REST por mensajería, sin tocar el dominio. |
| **Adapter** | `ClienteRepositoryAdapter`, `ProductoRepositoryAdapter`, `TransaccionRepositoryAdapter` traducen entre el modelo de dominio y las entidades JPA. |
| **Repository** | `*RepositoryPort` abstrae el acceso a datos; el dominio no sabe si detrás hay una base de datos, un archivo o un servicio remoto. |
| **DTO** | `ClienteRequest`, `ProductoResponse`, `TransaccionResponse` y demás. Separan el contrato de la API del modelo interno; se pueden cambiar de forma independiente. |
| **Builder** | Provisto por Lombok (`@Builder`) para construir modelos y entidades sin constructores de muchos parámetros. |
| **Inyección de dependencias por constructor** | Todos los servicios y adaptadores. Deja explícitas las dependencias y permite instanciarlos en pruebas sin Spring. |
| **Strategy (implícito)** | El enum `TipoCuenta` guarda su propio prefijo (`53` / `33`), en lugar de repartir condicionales por el código. |
| **Excepción de dominio** | `ExcepcionDeNegocio` y `ExcepcionDeRecursoNoEncontrado`; el manejador global las traduce a respuestas HTTP. |

---

## Principios SOLID

**S - Responsabilidad única.** Cada clase tiene un motivo para cambiar: el controlador
traduce HTTP, el servicio orquesta, el modelo aplica reglas, el adaptador persiste. El
manejador global de excepciones existe justamente para que ningún controlador tenga que
ocuparse del formato de los errores.

**O - Abierto/cerrado.** Agregar un nuevo tipo de cuenta implica añadir un valor al enum
`TipoCuenta` con su prefijo, sin modificar la lógica de generación de números de cuenta.

**L - Sustitución de Liskov.** Los servicios dependen de las interfaces de puerto. Cualquier
implementación que respete el contrato puede sustituir a la actual; en las pruebas se
sustituyen por dobles de Mockito y el comportamiento no cambia.

**I - Segregación de interfaces.** Los puertos se definieron por agregado
(`ClienteRepositoryPort`, `ProductoRepositoryPort`, `TransaccionRepositoryPort`) en lugar de
una interfaz general de persistencia. Cada consumidor depende solo de los métodos que usa.

**D - Inversión de dependencias.** Es la base de la arquitectura: `ProductoService` (política
de alto nivel) depende de `ProductoRepositoryPort` (abstracción), y es el adaptador de
infraestructura (detalle) el que depende de esa misma abstracción para implementarla.

---

## Principio ACID

**Atomicidad.** Los casos de uso que modifican datos están anotados con `@Transactional`. El
caso claro es la transferencia: se debita la cuenta de origen y se acredita la de destino
dentro de la misma unidad de trabajo. Si el débito falla por saldo insuficiente, el crédito
ya aplicado se revierte y no queda ningún movimiento registrado.

**Consistencia.** Las reglas se aplican en dos niveles. En el dominio (una cuenta de ahorros
nunca queda con saldo negativo, solo se cancela con saldo cero) y en la base de datos
mediante restricciones `NOT NULL`, `UNIQUE`, `CHECK` y llaves foráneas. La validación en Java
mejora los mensajes de error; la garantía real la da el motor de base de datos.

**Aislamiento.** Antes de mover un saldo, el producto se lee con bloqueo pesimista de
escritura:

```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("select p from ProductoEntity p where p.id = :id")
Optional<ProductoEntity> buscarPorIdConBloqueo(@Param("id") Long id);
```

Esto se traduce en un `SELECT ... FOR NO KEY UPDATE` en PostgreSQL. Sin él, dos retiros
simultáneos sobre la misma cuenta podrían leer el mismo saldo y uno sobrescribiría al otro
(*lost update*). En las transferencias las dos cuentas se bloquean siempre en orden
ascendente de identificador, para que dos transferencias cruzadas entre las mismas cuentas no
queden en interbloqueo.

**Durabilidad.** La confirmación de la transacción la garantiza PostgreSQL. El contenedor usa
un volumen de Docker, de modo que los datos sobreviven al reinicio.

Adicionalmente, las consultas se marcan con `@Transactional(readOnly = true)`, lo que permite
optimizaciones y evita escrituras accidentales.

---

## Base de datos: DDL y DML

### Modelo

```
   clientes                 productos                    transacciones
   ---------                ---------                    -------------
   id (PK)      1 ────< N   id (PK)          1 ────< N   id (PK)
   tipo_identificacion      tipo_cuenta                  tipo
   numero_identificacion    numero_cuenta (UK)           naturaleza
   nombres                  estado                       producto_id (FK)
   apellido                 saldo                        producto_relacionado_id (FK)
   correo                   saldo_disponible             monto
   fecha_nacimiento         exenta_gmf                   saldo_resultante
   fecha_creacion           cliente_id (FK)              referencia
   fecha_modificacion       fecha_creacion               descripcion
                            fecha_modificacion           fecha
```

Los scripts están en la carpeta `database/`:

| Archivo | Contenido |
| --- | --- |
| `database/schema.sql` | DDL: creación de las tres tablas con sus restricciones e índices |
| `database/datos-ejemplo.sql` | DML: juego de datos de prueba (dos clientes, tres cuentas, cuatro movimientos) |

```bash
psql -U banco_user -d banco_db -f database/schema.sql
psql -U banco_user -d banco_db -f database/datos-ejemplo.sql
```

En desarrollo la propiedad `spring.jpa.hibernate.ddl-auto=update` mantiene el esquema
sincronizado con las entidades. El script `schema.sql` reproduce ese mismo esquema de forma
explícita: se verificó arrancando la aplicación con `ddl-auto=validate` contra una base
creada únicamente con ese script, y Hibernate lo validó sin diferencias.

### Decisiones de modelado

**Llaves primarias.** Se usa `GENERATED BY DEFAULT AS IDENTITY` (`GenerationType.IDENTITY` en
JPA), que delega la generación en la columna de identidad de PostgreSQL. Es simple y evita
colisiones. Su limitación es que impide que Hibernate agrupe inserciones en lote, porque
necesita ir a la base por cada identificador; si el volumen de escritura lo exigiera, la
alternativa sería `GenerationType.SEQUENCE` con una secuencia propia y asignación por bloques.

**Llaves foráneas e índices.** `productos.cliente_id` y `transacciones.producto_id` tienen
llave foránea con nombre explícito. PostgreSQL **no** crea índices automáticamente sobre las
llaves foráneas, así que se declararon a mano: sin ellos, consultar los productos de un
cliente o el estado de cuenta de un producto obligaría a recorrer la tabla completa. También
hay índice sobre `transacciones.referencia`, que es la columna por la que se recuperan juntos
los dos movimientos de una transferencia.

**Dinero.** Todas las columnas monetarias son `NUMERIC(19,2)` y en Java se manejan con
`BigDecimal`. Nunca `double`: al ser binario de punto flotante, no representa exactamente los
decimales y `0.1 + 0.2` no da `0.3`. Por la misma razón los importes se comparan con
`compareTo(...)` y no con `equals(...)`, que también compara la escala y consideraría
distintos `100.0` y `100.00`.

**Enumeraciones.** Se persisten como texto (`@Enumerated(EnumType.STRING)`) y no como ordinal,
para que reordenar el enum en el código no corrompa los datos existentes. Hibernate genera
además una restricción `CHECK` por cada uno.

**Carga diferida.** Las relaciones `@ManyToOne` usan `FetchType.LAZY` y la propiedad
`spring.jpa.open-in-view` está desactivada. El adaptador convierte las entidades a objetos de
dominio dentro de la transacción, de modo que no hay riesgo de acceder a una relación fuera
de la sesión ni de generar consultas ocultas al serializar la respuesta.

---

## API REST

Base: `http://localhost:8080`

### Clientes

| Método | Ruta | Descripción | Éxito |
| --- | --- | --- | --- |
| POST | `/api/clientes` | Crear cliente | 201 |
| GET | `/api/clientes` | Listar clientes | 200 |
| GET | `/api/clientes/{id}` | Consultar cliente | 200 |
| PUT | `/api/clientes/{id}` | Actualizar cliente | 200 |
| DELETE | `/api/clientes/{id}` | Eliminar cliente | 204 |

### Productos

| Método | Ruta | Descripción | Éxito |
| --- | --- | --- | --- |
| POST | `/api/productos` | Crear cuenta de ahorros o corriente | 201 |
| GET | `/api/productos` | Listar productos | 200 |
| GET | `/api/productos?clienteId={id}` | Productos de un cliente | 200 |
| GET | `/api/productos/{id}` | Consultar producto | 200 |
| PUT | `/api/productos/{id}` | Actualizar la exención del GMF | 200 |
| PATCH | `/api/productos/{id}/estado` | Activar, inactivar o cancelar | 200 |
| DELETE | `/api/productos/{id}` | Eliminar producto | 204 |

### Transacciones

| Método | Ruta | Descripción | Éxito |
| --- | --- | --- | --- |
| POST | `/api/transacciones/consignaciones` | Consignar | 201 |
| POST | `/api/transacciones/retiros` | Retirar | 201 |
| POST | `/api/transacciones/transferencias` | Transferir entre cuentas | 201 |
| GET | `/api/transacciones` | Listar movimientos | 200 |
| GET | `/api/transacciones?productoId={id}` | Estado de cuenta de un producto | 200 |
| GET | `/api/transacciones/{id}` | Consultar movimiento | 200 |

Las transacciones solo se crean y se consultan. No se actualizan ni se eliminan: un
movimiento financiero es un registro inmutable y una corrección se hace con un movimiento que
lo compense, no borrando el original.

### Códigos de estado

| Código | Cuándo |
| --- | --- |
| 200 | Consulta o actualización correcta |
| 201 | Recurso creado |
| 204 | Eliminación correcta, sin contenido |
| 400 | Datos inválidos o regla de negocio incumplida |
| 404 | El recurso solicitado no existe |
| 409 | La operación viola una restricción de la base de datos |

### Formato de los errores

Se usa `ProblemDetail`, el formato estándar de respuesta de error definido por la RFC 7807:

```json
{
  "type": "about:blank",
  "title": "Regla de negocio incumplida",
  "status": 400,
  "detail": "Una cuenta de ahorros no puede tener un saldo menor a $0",
  "instance": "/api/transacciones/retiros"
}
```

Los errores de validación agregan el detalle por campo:

```json
{
  "title": "Error de validación",
  "status": 400,
  "detail": "La petición contiene campos inválidos",
  "errores": {
    "correo": "El correo no tiene un formato válido"
  }
}
```

### Ejemplos

Crear un cliente:

```bash
curl -X POST http://localhost:8080/api/clientes \
  -H 'Content-Type: application/json' \
  -d '{
        "tipoIdentificacion": "CC",
        "numeroIdentificacion": "1020304050",
        "nombres": "Laura",
        "apellido": "Gomez",
        "correo": "laura.gomez@banco.com",
        "fechaNacimiento": "1992-07-08"
      }'
```

Crear una cuenta de ahorros:

```bash
curl -X POST http://localhost:8080/api/productos \
  -H 'Content-Type: application/json' \
  -d '{"tipoCuenta": "AHORROS", "clienteId": 1, "saldoInicial": 100000, "exentaGmf": true}'
```

Transferir entre cuentas. La respuesta contiene los dos movimientos generados, unidos por la
misma referencia:

```bash
curl -X POST http://localhost:8080/api/transacciones/transferencias \
  -H 'Content-Type: application/json' \
  -d '{
        "productoOrigenId": 1,
        "productoDestinoId": 2,
        "monto": 70000,
        "descripcion": "Pago de arriendo"
      }'
```

```json
[
  {
    "id": 3, "tipo": "TRANSFERENCIA", "naturaleza": "DEBITO",
    "productoId": 1, "productoRelacionadoId": 2,
    "monto": 70000, "saldoResultante": 50000,
    "referencia": "66155b9f-a154-4de3-9df1-050be4fe6f0c"
  },
  {
    "id": 4, "tipo": "TRANSFERENCIA", "naturaleza": "CREDITO",
    "productoId": 2, "productoRelacionadoId": 1,
    "monto": 70000, "saldoResultante": 70000,
    "referencia": "66155b9f-a154-4de3-9df1-050be4fe6f0c"
  }
]
```

Consultar el estado de cuenta:

```bash
curl "http://localhost:8080/api/transacciones?productoId=1"
```

---

## Reglas de negocio

### Clientes

- El cliente debe ser mayor de edad; no se crea ni se actualiza si es menor.
- El número de identificación es único.
- El correo debe tener formato `xxxx@xxxxx.xxx`.
- Los nombres y el apellido deben tener al menos dos caracteres.
- La fecha de creación se calcula automáticamente al registrar.
- La fecha de modificación se recalcula en cada actualización.
- No se puede eliminar un cliente que tenga productos vinculados.

### Productos

- Solo existen dos tipos: cuenta de ahorros y cuenta corriente.
- Un producto siempre pertenece a un cliente existente.
- El número de cuenta se genera automáticamente: diez dígitos, iniciando en `53` para
  ahorros y en `33` para corriente. Es único, garantizado por restricción en la base de datos.
- La cuenta se crea en estado activo.
- Las cuentas se pueden activar e inactivar en cualquier momento.
- Una cuenta de ahorros no puede quedar con saldo menor a cero.
- Solo se puede cancelar una cuenta cuyo saldo sea cero. La cancelación es definitiva: una
  cuenta cancelada no vuelve a cambiar de estado.
- La fecha de creación se calcula automáticamente.
- No se elimina un producto con saldo o con movimientos registrados.

### Transacciones

- Tres tipos: consignación, retiro y transferencia entre cuentas.
- El monto siempre debe ser mayor que cero.
- Solo se opera sobre cuentas activas.
- El saldo y el saldo disponible se actualizan con cada movimiento exitoso.
- Las transferencias solo se realizan entre cuentas que existen en el sistema, y el origen no
  puede ser igual al destino.
- Una transferencia genera dos movimientos: un débito en la cuenta de envío y un crédito en la
  cuenta de recepción, ambos con la misma referencia.

---

## Pruebas

```bash
cd banco
./mvnw test
```

103 pruebas automáticas, distribuidas así:

| Clase | Pruebas | Tipo |
| --- | --- | --- |
| `ProductoTest` | 12 | Reglas del modelo de dominio |
| `TransaccionTest` | 4 | Reglas del modelo de dominio |
| `ClienteServiceTest` | 13 | Capa de servicio, con dobles de Mockito |
| `ProductoServiceTest` | 18 | Capa de servicio, con dobles de Mockito |
| `TransaccionServiceTest` | 14 | Capa de servicio, con dobles de Mockito |
| `ClienteControllerTest` | 9 | Capa web con `@WebMvcTest` |
| `ProductoControllerTest` | 16 | Capa web con `@WebMvcTest` |
| `TransaccionControllerTest` | 13 | Capa web con `@WebMvcTest` |
| `ConfiguracionCorsTest` | 3 | Cabeceras de CORS y rechazo de orígenes no autorizados |
| `BancoApplicationTests` | 1 | Arranque del contexto de Spring |

El enunciado pide cobertura de las capas de servicio y de controlador; ambas están cubiertas,
y adicionalmente se probó el modelo de dominio de forma aislada.

Las pruebas de servicio son unitarias puras: sustituyen los puertos por dobles, de modo que
verifican la lógica sin base de datos. Las de controlador levantan solo la capa web con
`@WebMvcTest` y sustituyen el caso de uso, comprobando el contrato HTTP: rutas, códigos de
estado, validaciones y formato de los errores. La prueba de contexto usa H2 en memoria, así
que la ejecución completa no depende de que el contenedor esté levantado.

---

## Control de versiones

Repositorio único en GitHub, con estrategia **Git Flow** y avance evidenciado mediante commits
y push a lo largo del desarrollo.

| Rama | Propósito |
| --- | --- |
| `main` | Versión estable |
| `develop` | Integración del trabajo terminado |
| `feature/clientes` | Módulo de clientes |
| `feature/productos` | Módulo de productos |
| `feature/transacciones` | Módulo de transacciones |
| `fix/revision-codigo` | Correcciones tras una revisión del código |
| `docs/documentacion` | Documentación y scripts de base de datos |

Cada rama de trabajo se integra a `develop` con `merge --no-ff`, de modo que el historial
conserva visible el agrupamiento de cada funcionalidad. Los mensajes siguen la convención
*Conventional Commits* (`feat`, `fix`, `test`, `chore`, `refactor`, `docs`), con commits
pequeños y de un solo propósito.

---

## Servicios en la nube

No se utilizaron servicios en la nube. La aplicación se ejecuta localmente y la base de datos
corre en un contenedor Docker en la misma máquina.

Al no depender de rutas ni credenciales fijas en el código (la conexión se configura por
variables de entorno), el despliegue en un servicio administrado no requeriría cambios en el
código: bastaría con apuntar `DB_URL`, `DB_USER` y `DB_PASSWORD` a la instancia
correspondiente.

---

## Front

El front se desarrollará en **Angular** y consumirá esta API. A la fecha de este documento no
está construido; el alcance entregado corresponde al proyecto backend.

---

## Estado del proyecto

Implementado y verificado:

- CRUD de clientes con todas sus reglas.
- CRUD de productos con generación automática del número de cuenta y control de estados.
- Consignaciones, retiros y transferencias, con actualización de saldos y estado de cuenta.
- Persistencia en PostgreSQL con el esquema y las restricciones necesarias.
- Manejo global de errores con respuestas uniformes.
- 103 pruebas automáticas.
- Scripts DDL y DML versionados.
- Aplicación y base de datos ejecutables en contenedores con un solo comando.
- CORS configurado para el consumo desde el front.

Pendiente:

- Colección de Postman con las peticiones de ejemplo.
- Aplicación front en Angular.
