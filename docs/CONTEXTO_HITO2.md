# Contexto Hito 2: Epicentro Gourmet (Spring Boot)

Resumen del enunciado (`Trabajo Practico OO2 Hito 2 V2.pdf`) y de lo que se dijo en las clases del 17/09,
24/09, 01/10 y 08/10: recomendaciones de los profes, correcciones a los diagramas y decisiones de diseño.
Lo que no quedó del todo claro en clase está marcado como dudoso.

---

## 0. Datos clave

- **Grupo 12**: Agustín Dzevecka (adzeveck), Bautista Nuñez (BautistaANun), Santiago Mansilla (santiagomansilla),
  Nahuel Palucito (Paluso). Repo Hito 1: `github.com/adzeveck/TPGrupalOO2Hito1Grupo12`.
- **Repo nuevo** para el Hito 2 (lo dijeron explícitamente): algo como "TP OO2 Hito 2 Grupo 12".
- Cursada de los jueves (turno noche), según las fechas de las clases.
- **Fechas** (turno noche):
  - 15/10: **no hay clase** (marcha universitaria). Consulta virtual opcional el **miércoles 14/10, 9:30**.
  - **22/10: corrección parcial, presencial.** Mínimo tentativo (iban a confirmarlo por mail): **seguridad, registro de
    responsable, login como empleado, cargar un pedido**. Llevar el proyecto andando y las dudas concretas.
  - **Defensa: 29/10 o 5/11.** Si el grupo no elige, la asignan según la corrección parcial. **Recuperatorio: 12/11.**
- La nota es **personal**, tiene un componente grupal y se basa en el repositorio (aportes individuales en Git).
- Para el final (diciembre) van a pedir algo cortito con API REST. **Para el Hito 2 NO va REST ni JWT**: todo MVC +
  Thymeleaf en un solo proyecto Spring.

---

## 1. Enunciado del Hito 2 (resumen)

### Dominio
- **Festival**: nombre, temporada, fecha inicio/fin. Tiene **costos** (por superficie, por montaje, plus electricidad,
  sueldo base, etc.).
- **Unidad de venta**: nombre comercial, responsable, superficie m², **código único de 10 caracteres con validación
  propia**. Subtipos: **FoodTruck** (patente, requiere conexión eléctrica) y **PuestoDesarmable** (cant. carpas,
  tiempo de montaje en min). Cada una tiene su lista de **platos** y su **staff**.
- **Personal**: mayor de edad; nombre, apellido, dni, fecha nacimiento, fecha ingreso (antigüedad). Sueldo base.
  - **Cocina**: tarea especial + plus fijo por categoría (Cocinero $150.000, Cocinero ayudante $100.000, Lavaplatos $50.000).
  - **Cajero**: turno (mañana/noche) + antigüedad ($5.000 por año).
- **Plato**: nombre, precio de venta, costo de producción.
- **Pedido**: fecha, festival, unidad de venta, platos y cantidad de cada uno.

### Inicio (público, sin login)
- Página de inicio con la info de los festivales (precargados). Dos acciones:
  1. **Registrar Unidad de Venta + Responsable** (crea usuario del responsable; usuario y contraseña obligatorios).
  2. **Iniciar sesión** (Administrador precargado, Responsable o Empleado).

### Administrador
1. Ver festivales.
2. Ver unidades de venta de un festival.
3. **Ranking de unidades**: lista ordenada desc. por recaudación para un festival.
4. **Unidades con mayor canon**: top "x" unidades por canon (nombre comercial, código, tipo, canon) → `ReporteMayoresCanonDTO` (no persiste).

### Responsable (sobre SU unidad de venta)
1. **ABM de empleados.** Al dar de alta, **enviar mail** con usuario (su email) y **contraseña aleatoria**. Baja
   **lógica**. Para reactivar a un eliminado, usar un método de actualización específico.
2. **Filtros** por uno o varios atributos (sin traer los eliminados lógicamente).
3. **Canon**: FoodTruck = `areaSup*500 + 2000 (si requiere electricidad)`; Puesto = `areaSup*500 - tiempoMontaje*10`.
4. **Liquidación de haberes**:
   - Hasta 15 días: 50%; 16 a 30: 100%; más de 30: `(int(dias/15)+1) * 50% * (base + plus)`.
   - Cocina: base + plus categoría. Cajero: base + antigüedad (años * 5.000).
   - Ej.: 38 días, lavaplatos → (2+1)·0,5·150.000 = **$225.000**. 52 días, cajero 3 años → (3+1)·0,5·115.000 = **$230.000**.
5. **Reporte de recaudación** de un festival → `ReporteVentaDTO` (no persiste).
6. **Rentabilidad neta** de una unidad en un festival: (pedidos − costo de platos) − sueldos − canon. Pedidos precargados.
7. **Rentabilidad neta entre dos fechas** (festival + unidad).
8. **Plato estrella** (unidad + festival) → `PlatoDTO` con más pedidos.

### Empleado
1. Ver su info: festivales y unidades donde trabajó.
2. Sueldo por festival + **descargar recibo en PDF**.
3. Ver si hoy tiene que trabajar en un festival activo y en qué unidad.
4. Cajero: **cerrar caja** al finalizar el día.

### Flujos a mostrar
1. Login. 2. Registro de Responsable con Unidad de Venta. 3. Alta de empleados. 4. Alta de platos.
5. Reportes (admin y responsable). 6. Ver festival y puesto actual (empleado). 7. Descargar recibo de sueldo.

### Obligatorios
- **Script SQL con todos los inserts de prueba en el repo**: festival, usuarios de distintos tipos, unidades con
  responsable, empleados, pedidos con ítems, platos y costos.
- **Nada hardcodeado** (ej. sueldo base $100.000 → en una clase/tabla de costos).
- **Validaciones de carga** (ej. período del festival válido, el pedido solo con fecha de hoy).
- **Por grupo**: ≥1 envío de mail, ≥1 DTO de reporte, ≥1 PDF.
- **Por integrante**: **2 CU extra** (además de los del enunciado) **y 2 validaciones de carga**.
- Stack: **Maven, MySQL, Java, Spring Boot**. IDE: STS / IntelliJ / VS Code.

### Git y README
- README: nº de grupo, integrantes + usuario GitHub, **pasos para correr** (crear BD, variables de entorno, orden de
  los scripts), link/imagen del diagrama de clases, **CU implementados + responsable + link al PR**.
- Cada CU entra a `main` por **Pull Request** desde una rama `CU<Funcionalidad><Responsable>`
  (ej. `CUAltaEmpleadoJuanPerez`). En el PR, comentar qué se hizo (entidades, repos, servicios...).
- Se puede hacer merge directo o **squash** (los commits quedan igual en el PR).

### Criterios de evaluación
Diagrama de clases · Listado de CU · Nivel de solución · Buenas prácticas · BD · README · Vistas y navegación ·
Implementación de CU · Seguridad · Envío de mails · Uso de Git (aportes) · Variables de entorno · Trabajo en equipo.

---

## 2. Decisiones de diseño acordadas en clase (las correcciones que les hicieron a otros grupos)

La frase que más repitieron: **"no se la compliquen"**. Es un laboratorio y hay 3 semanas: cada clase o relación de más
es otra FK y otra fuente de errores.

### Usuarios y roles
- Clase/tabla **User** (credenciales) + clase/tabla **Rol** aparte (como `User`/`UserRole` del proyecto ejemplo).
  El enum de roles puede seguir existiendo, pero los roles van **en tabla aparte**.
- Roles: **ADMIN, RESPONSABLE, EMPLEADO**. En la BD se guardan con prefijo: `ROLE_ADMIN`, `ROLE_RESPONSABLE`,
  `ROLE_EMPLEADO` (Spring Security lo exige).
- El ejemplo usa un `Set` de roles: **se mantiene la colección, pero en la práctica 1 usuario = 1 rol**.
- **username = email** (recomendado). Si separan username y email, el email tiene que estar sí o sí porque se envían mails.
- **`activo` (baja lógica) va en User**, no en Persona/Empleado. Un `activo` en Empleado solo si lo usan para el
  negocio (empleados que ya no trabajan en la unidad); si es solo para la baja lógica, en User alcanza.
- **Persona/Empleado encapsula un User** (atributo tipo "credenciales": User). No todas las personas necesitan usuario.
  El rombo de la composición va del lado de Staff hacia User.
- Contraseñas **siempre hasheadas con `BCryptPasswordEncoder`** antes de guardar (en el script SQL también).

### Responsable
- **El responsable de la unidad es un User con rol RESPONSABLE, no un Staff/Persona.** Así se evita que alguien sea
  cajero/cocinero y responsable a la vez. Hay que agregar la relación **User ↔ UnidadDeVenta**.
- **Un responsable tiene una sola unidad de venta.**
- **Registro**: en un mismo servicio se crea la unidad + el usuario responsable, y después se vinculan.

### Empleados / Staff
- **Un empleado trabaja en una sola unidad de venta** (nada de una tabla intermedia "AsignaciónStaff" con fechas).
  `UnidadDeVenta` tiene un `Set<Staff>`. Staff no necesita "unidadAsignada" (sería bidireccional).
- **Cocinero**: `categoria` como **enum** (COCINERO, AYUDANTE, LAVAPLATOS) sin una clase "CategoríaCocina" intermedia.
  Opcional: atributo `especialidad` (ej. comida italiana) para que se diferencie más de Cajero.
- **Cajero**: `turno` como **enum** (MAÑANA/NOCHE). Nada de una clase Turno: "una entidad nueva tiene sentido cuando
  hay dinamismo".
- Para saber el tipo de unidad/empleado: usar la subclase (`instanceof`), no un atributo "tipo".

### Costos (nada hardcodeado)
- Clase/tabla **Costo** relacionada **1 a 1 con Festival** (en singular, "Costo"). Atributos comunes (no constantes):
  costo por m², costo por montaje, plus electricidad, **sueldo base**, monto por año de antigüedad,
  **plus cocinero, plus ayudante, plus lavaplatos**.
- Según la categoría del cocinero se busca el plus en los costos del festival.
- Los costos son fijos durante todo el festival.
- **Chequear que estén todos los datos para todas las fórmulas** (superficie, electricidad, tiempo de montaje, carpas,
  fecha de ingreso, días del festival, todos los plus). Lo que es un costo fijo va a Costo; lo que es dato de la
  unidad/empleado, a esa entidad.

### Pedidos
- **Pedido tiene un `Set<DetallePedido>`** (como una factura: encabezado + detalle). DetallePedido no muestra pedido en
  el diagrama (la FK existe igual). Dar vuelta el rombo.
- **DetallePedido: solo plato + cantidad.** Sin precio ni costo unitario (ya están en Plato), sin subtotal.
  Como mucho, persistir el **total en Pedido**. Sin historial de precios.
- **UnidadDeVenta tiene la lista de pedidos.** Pedido no necesita festival ni unidad en el diagrama (se llega por la
  unidad). Sacar las relaciones bidireccionales del diagrama.
- No poner el cajero que registró el pedido si el negocio no lo pide. Pedido sin `activo`.
- No llamar al atributo "fechaTransaccion" (término técnico): `fecha`.
- Plato puede tener `activo` (plato que hoy no se hace). UnidadDeVenta **no** necesita `activo`.

### Festival
- Se trabaja con **un solo festival** (precargado). Unidad nueva → se asigna a ese festival (o por script).
- Temporada: un **String** simple (no una clase).
- No hace falta CU de alta de festival (si lo hicieron para practicar está bien, pero no es prioritario).
- Festival tiene la lista de unidades. Sacar `festival` de UnidadDeVenta (bidireccional).

### DTOs y reportes
- Los reportes son **DTOs** (se pueden dibujar aparte en el diagrama).
- `ReporteVentaDTO`: no un solo atributo → **festival + total recaudado**.
- `PlatoDTO` (plato estrella): algo **visual**, no copia de Plato → nombre del plato, cantidad pedida, festival,
  unidad. **Sin id, precio ni costo**.
- **Cierre de caja**: preferible como **DTO calculado en el momento** (query con pedidos/platos del día), no entidad.
  Si es entidad, que sea consistente. Es a nivel unidad de venta, lo hace un cajero. Nada de cajas o entradas a nivel festival.
- **Liquidación de haberes**: 2 opciones válidas:
  - Calcularla en vivo y generar el PDF directo.
  - Persistirla (histórico) → nombre específico, `LiquidacionHaberes`, con empleado, **unidad de venta** (no festival:
    se llega desde la unidad), fecha, categoría/rol, días, plus, **total a cobrar**. No guardar quién la calculó.
    Si está guardada, el empleado baja el PDF directo.

### Diagrama de clases
- **Sin métodos ni casos de uso** en el diagrama: los CU van en una **planilla aparte** (Excel/Sheets) con
  responsable y link al PR.
- Notación: `nombre: Tipo`, colecciones como `roles: Set<Rol>` (no "set role" sin nombre).
- Flechas de herencia con el triángulo **hacia la superclase**.
- IDs como **`Integer` o `Long`** (no `int`: JpaRepository necesita la clase).
- **Dinero en `BigDecimal`** (no double).
- Sin nivel de detalle extra (ingredientes, historial de precios, etc.).

---

## 3. Arquitectura Spring (proyecto ejemplo de Gustavo Siciliano)

- **Opciones**: (1) renombrar el proyecto ejemplo y arrancar ahí (recomendado) o (2) crear uno nuevo con
  **Spring Initializr** (start.spring.io; IntelliJ Community no lo trae integrado) y copiar las dependencias del POM.
  Del ejemplo hay que traerse sí o sí **la seguridad, las vistas y el layout** (mismo estilo en todas las vistas).
- El POM del ejemplo cubre ~80% de lo que hace falta; **faltan las dependencias de mail y de PDF** (buscar en Maven Repository).
- **Lombok**: instalar el plugin en el IDE (`@Getter @Setter @NoArgsConstructor`).
- **Variables de entorno** para URL/usuario/password de la BD **y la contraseña del mail**. En IntelliJ:
  Edit Configurations → Environment variables. Documentarlas en el README.
- Levanta en `localhost:8080`; la primera ruta es `/index`.

### Capas
- **controllers** (`@Controller`, `@RequestMapping`, `@GetMapping`/`@PostMapping`) → devuelven `ModelAndView` o
  `RedirectView`. Parámetros por `@PathVariable` o `@RequestParam`; formularios con `@ModelAttribute` (+ `@Valid`).
- **helpers/ViewRouteHelper**: constantes con carpeta/nombre de vista (`templates/home/index.html`, etc.).
- **dtos**: reducción de una entidad; puede haber un DTO de request y otro de respuesta. **ModelMapper** (bean en
  `configuration/ModelMapperConfig`) mapea entidad ↔ DTO (necesita al menos un atributo en común).
- **services**: interfaz (contrato) + implementación `@Service`. **Toda la lógica y las validaciones de negocio van acá.**
  Inyección por constructor.
- **repositories**: interfaces `@Repository` que extienden `JpaRepository<Entidad, Integer|Long>` (`save`,
  `deleteById`, `findAll`... vienen solos). Consultas por nombre (`findByNombre`, `findByInstitutionAndYear`,
  `...OrderByYearDesc`); si no alcanza, `@Query` (JPQL, `join fetch` para lazy). Una herencia = un solo repo (el de la superclase).
- **entities**: `@Entity`, `@Id @GeneratedValue(strategy = IDENTITY)`, `@Column(unique, nullable, length)`, `@Table`
  (+ `uniqueConstraints`), `@Inheritance(strategy = JOINED)`, `@OneToMany(mappedBy, fetch = LAZY)` /
  `@ManyToOne @JoinColumn`, `@CreationTimestamp`/`@UpdateTimestamp`. Ya no hay `.hbm.xml`.
- `Optional`: `isEmpty()`/`ifPresent()` o `orElseThrow()` para evitar NullPointerException.
- **Validaciones en 3 niveles**: de negocio (service), de modelo (anotaciones en DTOs + `@Valid`) y en entidades.
- **Vistas Thymeleaf** (`templates/`): `th:each`, `th:text`, `th:field`, `th:action`, `th:object`, `th:href`. Carpeta
  `templates/error/` para páginas de error (404, etc.).
- Hay un ejemplo de `@RestController` en `controllers/api/v1`: **no se usa en el Hito 2**.

### Seguridad (ya resuelta en el ejemplo: adaptarla)
- `spring-boot-starter-security` + `SecurityConfiguration` (`@Configuration @EnableWebSecurity @EnableMethodSecurity`).
- `SecurityFilterChain`: rutas públicas con `permitAll` (resources/css/imgs, index, login, **registro**) y el resto
  `authenticated()`. `formLogin` con `loginPage("/login")`, `loginProcessingUrl("/loginprocess")`, `defaultSuccessUrl`;
  `logout`. **CSRF y CORS deshabilitados** (está bien para el TP).
- `AuthenticationManager` + `DaoAuthenticationProvider` con `UserService` y `BCryptPasswordEncoder` (bean).
- `UserService implements UserDetailsService` → `loadUserByUsername` usa `findByUsernameAndFetchUserRoles` (@Query con
  fetch de roles) y arma un `org.springframework.security.core.userdetails.User` con `SimpleGrantedAuthority`.
- Por endpoint: `@PreAuthorize("hasRole('ADMIN')")`, etc. Usuario logueado: `SecurityContextHolder...getPrincipal()`.
- Para el admin precargado: hashear la contraseña con la clase de test del ejemplo (BCrypt) y meterla en el script.
- No hace falta vencimiento ni cambio periódico de contraseña.

---

## 4. Flujos clave (lo que explicaron con más detalle)

### Alta de empleado + mail (el más importante: "de ahí sale todo lo demás")
1. Responsable inicia sesión → ya tiene su unidad asignada.
2. Completa el formulario (nombre, apellido, dni, email...).
3. El servicio **primero manda el mail** con el usuario (email) y la contraseña aleatoria, **chequea que se envió y
   recién ahí persiste** User + Empleado (con la contraseña hasheada). Si el mail falla → mensaje "no se pudo generar
   el empleado", **sin persistir** (si no, el email queda "quemado" por el unique y el empleado nunca recibe sus credenciales).
4. Alternativa válida: persistir con un estado "no activado" que se activa en el primer login, o borrar los que nunca se activaron.
5. Mostrar el listado de empleados activos.

También son flujos los casos de error: login incorrecto, mail inválido en el formulario, falla al guardar o al enviar el mail.
"Pensar todos los escenarios posibles" de cada funcionalidad.

### Registro de unidad de venta + responsable
- El formulario público carga datos de la unidad + datos/credenciales del responsable. El servicio crea la unidad, el
  usuario RESPONSABLE (password hasheada), los vincula y asigna la unidad al festival.

### Vistas por rol
- **Index público**: logo/nombre de la app, botones **Registrar unidad de venta** y **Login** arriba a la derecha,
  info del festival (imagen genérica, fechas de inicio/fin, puestos). Lo ve cualquiera, como visitante.
- **Admin**: ve todo (festival, todas las unidades, todos los empleados, reportes).
- **Responsable**: solo su unidad, sus empleados (ABM) y sus reportes.
- **Empleado**: solo su propia información.
- Sugerencia: primero que las vistas funcionen y después darles estilo (o usar el layout del ejemplo desde el principio).

### Pedidos
- **No hace falta un CU de alta de pedidos**: van **precargados por script** (ej. ~20 pedidos por día para varias
  unidades), así los reportes tienen datos en la defensa. Si los hacen como CU, el cajero los carga (solo con fecha de hoy).
  Nota: el mínimo tentativo del 22/10 menciona "cargar un pedido", a confirmar por mail.
- Los platos de esos pedidos también precargados, **pero el CU de alta de platos se hace igual**.
- Si el script depende de datos creados desde la app (ej. unidades), aclarar el orden en el README. Lo más simple:
  **un script que cargue todo** (CommandLineRunner es otra opción, pero hay que cuidar los duplicados en cada arranque).
- "Nos pasó otros años que venían con la BD vacía y la defensa llevó mucho tiempo": **llevar todo precargado**.

---

## 5. Consejos generales de los profes

- Simplificar: si una clase encapsula solo un id y un atributo que nunca cambia → enum/String.
- Acotar el laboratorio ustedes mismos si ven que el detalle se va de las manos ("esto lo acotamos porque ya aplico lo
  mismo en tal CU").
- Usar el **foro del campus** para las consultas (las respuestas les sirven a todos).
- Ir a las clases de consulta con el proyecto andando y las dudas puntuales; avanzar en el código ya.
- Para la defensa: cada integrante tiene que poder explicar lo que hizo ("hice este controller con estos métodos").

---

## 6. Resumen por clase

- **Clase 1 (17/09): Spring sin persistencia.** Setup en IntelliJ (Lombok, BD, variables de entorno, run), recorrido de
  controllers/ModelAndView/Thymeleaf con el CRUD de Person, PathVariable vs RequestParam, DTOs + ModelMapper,
  services con interfaz, inyección por constructor, beans (`@Configuration` + `@Bean`), validaciones con `@Valid`,
  BCrypt para el usuario de prueba. Faltan dependencias de mail y PDF. Ejemplo REST (no se usa).
- **Clase 2 (24/09): persistencia + Spring Security + presentación del Hito 2.** Entities, repositories
  (JpaRepository, query methods, @Query), toda la config de seguridad, roles `ROLE_*`, repo nuevo, recorrida del
  enunciado, pedidos por script, un solo festival, una unidad por responsable, Git/PR/README/squash.
- **Clase 3 (01/10): consulta de diagramas + V2 del enunciado.** La V2 agrega: script con datos de prueba, validaciones
  (pedido solo con fecha de hoy, período válido) y 2 validaciones por integrante. Debate de roles (enum vs tabla) y del
  responsable como rol, liquidación (histórico vs PDF), cierre de caja, flujo de alta de empleado con mail,
  baja lógica. Faltaba agregar costos + User/Rol al diagrama.
- **Clase 4 (08/10): cierre de diagramas** (correcciones a varios grupos, ver sección 2), modelo de index, Spring
  Initializr, layout del ejemplo, anuncio de la corrección parcial del 22/10 y de las fechas de defensa.

---

## 7. Puntos ambiguos para decidir como grupo

- ¿Liquidación de haberes persistida o calculada al vuelo para el PDF?
- ¿Cierre de caja como DTO (recomendado) o entidad?
- ¿Alta de pedidos como CU (cajero) o solo script? (ojo con el mínimo del 22/10)
- "Ver festivales y unidades donde trabajó" con un empleado = una unidad: el reporte devuelve esa unidad y su festival.
- "Rentabilidad entre dos fechas": filtrar pedidos por rango; ¿sueldos y canon prorrateados o completos? (no lo aclararon)
- Los 2 CU extra y las 2 validaciones de cada integrante.
