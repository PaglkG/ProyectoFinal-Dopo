# Lógica y decisiones del proyecto

> Estado: borrador en construcción. Este documento es la fuente de verdad para las reglas de negocio acordadas. No se debe deducir una regla a partir de un método vacío, una implementación provisional o una prueba antigua.

## Cómo mantener este documento

- Consultarlo antes de cambiar la lógica o crear pruebas que dependan de reglas de negocio.
- Preguntar a Pablo cuando falte una regla o admita más de una interpretación.
- Al confirmar o cambiar una regla, actualizar este documento en el mismo trabajo que el código y sus pruebas.
- Mantener separados los contratos confirmados, el estado de implementación y las decisiones pendientes.

## Reglas confirmadas por Pablo

### Identidad y lado de juego

- En `PlayerVsPlayer`, el jugador 1 y el jugador 2 ocupan posiciones fijas; esas posiciones no se intercambian entre partidas.
- En `PlayerVsBot`, las posiciones también son fijas. El parámetro `player` selecciona cuál jugador humano participa; no selecciona el lado.
- En `PlayerVsBot`, el jugador humano ocupa siempre la derecha y el bot la izquierda, incluso si se selecciona al jugador 2.

### Tiempo y modalidad por tiempo

- El tiempo del juego representa tiempo transcurrido y comienza en cero.
- En `ByTime.check`, `true` significa que la partida continúa.
- Si se alcanza el límite y el marcador está empatado, la partida continúa en muerte súbita.
- Si se alcanza el límite y el marcador está desigual, `ByTime.check` devuelve `false` y el partido termina.
- Cuando se alcanza el límite con el marcador empatado, `ByTime.suddenDeath` se activa y `check` devuelve `true`.
- Cuando un marcador desigual termina la muerte súbita, `ByTime.suddenDeath` vuelve a `false`.

### Balón y cancha

- El balón que crea `SoccerField` tiene radio 10 unidades.

### Modalidad por goles

- `ByGoals` termina el partido cuando uno de los marcadores iguala exactamente el límite configurado. Superarlo no cuenta como igualdad con el límite.
- La comparación de goles permanece en `ByGoals`; las comprobaciones de tiempo y muerte súbita permanecen en `ByTime`, aunque el diagrama ubica esos helpers bajo `ByGoals`.

### Reinicio del balón

- `Ball.resetBall(width, height)` sitúa el balón en el centro geométrico de la cancha (`x = width / 2`, `y = height / 2`) y deja `velocityX` y `velocityY` en cero.
- `SoccerField.resetBall(x, y)` ubica el centro de cada balón en esas coordenadas y deja sus velocidades en cero.

### Actualización, contactos y goles

- `GameSession.updateGame()` avanza en pasos fijos de `1/60` segundo. En cada paso, el balón suma `40 × paso` a su velocidad vertical y luego se desplaza usando las velocidades resultantes.
- Al tocar el suelo, el balón se coloca con su parte inferior sobre el suelo, invierte la velocidad vertical y conserva el 70 % de su magnitud. Si la nueva magnitud es menor que `0.5`, la velocidad vertical queda en cero. La velocidad horizontal se conserva.
- El balón no permanece superpuesto con un participante: al detectarse el contacto, se reposiciona horizontalmente hasta quedar fuera de su hitbox y del lado que lo aleja, eligiendo el otro lado si el primero lo sacaría de la cancha. En el primer paso del contacto se suma `Δvx = ±4` hacia fuera y `Δvy = -1`; la velocidad horizontal se redirige hacia fuera si aún apuntaba hacia el participante. El impulso se aplica una vez por contacto continuo y puede repetirse tras separarse y volver a tocarse.
- Un gol ocurre cuando el centro del balón entra en el rectángulo de gol dentro de la cancha. Las hitboxes de gol conservan ancho `cancha/10` y alto `cancha/3`, y quedan apoyadas sobre la línea del suelo dentro del límite inferior. La entrada frontal del arco permanece libre para permitir el gol; los bordes del arco rebotan el balón. El arco izquierdo concede un punto al lado derecho y viceversa. La detección del gol precede a la comprobación de salida del balón.
- Después de un gol se reinicia el balón al centro y se ubican los participantes de forma simétrica: hitbox de ancho `cancha/20`, alto `cancha/3`, centros horizontales en `1/4` y `3/4` del ancho, apoyados sobre el suelo.
- Cualquier parte de la hitbox de un participante o balón que salga del rectángulo exterior de la cancha produce `GameException` con mensaje `objeto fuera de la partida`. Un participante que toque el límite superior o los límites laterales también produce esa excepción. El contacto participante-participante produce `GameException` con mensaje `dos objetos super puestos`.
- El balón puede tocar límites laterales o superior sin efecto; toca el suelo para rebotar. El contacto participante-balón aplica el impulso descrito. Otros contactos con la cancha no tienen efecto.
- La sesión conserva goles izquierdo y derecho. En `ByGoals`, alcanzar el límite termina la sesión; en `ByTime`, el tiempo transcurrido se cuenta en segundos y se pasan a `check` los minutos enteros completos. La modalidad temporal continúa en empate (muerte súbita) y termina al quedar un ganador una vez agotado el límite. Una sesión terminada no avanza más.

### Hitboxes y medidas provisionales

- Las intersecciones se consultan entre entidades del dominio que tienen hitboxes asociadas; las hitboxes geométricas se crean como parte de esas entidades y no se tratan como objetos de juego independientes.
- Dos hitboxes cuentan como intersecadas cuando sus bordes se tocan. Dos círculos intersectan si la distancia entre centros es menor o igual a la suma de sus radios; dos rectángulos intersectan si sus intervalos se solapan incluyendo el contacto; círculo-rectángulo se resuelve por la distancia entre el centro del círculo y el punto más cercano del rectángulo.
- `Hitbox.intersects(hitbox)` devuelve un `HitboxContact` con el nombre simple de la entidad contactada y la hitbox asociada que participó. Sin contacto devuelve un resultado con `name = ""` y `hitbox = null`. Los nombres usados son `Ball`, `SessionParticipant` y `SoccerField`.
- `Ball` y `SessionParticipant` comprueban intersección mediante sus hitboxes asociadas. `SoccerField.intersects(hitbox)` busca contacto con sus límites, arcos y balones; si la cancha es el objeto contactado, devuelve la parte concreta de la cancha o el balón contenido que intervino.
- `Hitbox.reposition(x, y)` reposiciona la referencia de cada forma: centro para círculos y esquina superior izquierda para rectángulos. `Ball` y `SessionParticipant` delegan en su hitbox; `SoccerField` reposiciona las formas que contiene.
- Como propuesta provisional, la cancha mide 450 × 150 unidades, los límites tienen grosor `alto / 100`, y cada arco mide `ancho / 10` por `alto / 3` y queda apoyado sobre el límite inferior dentro de la cancha.
- Como propuesta provisional, la hitbox del participante mide `ancho / 20` por `alto / 3`.

### Configuración del jugador

- El subsistema de configuración fue retirado por decisión de Pablo. El modelo ya no gestiona preferencias de sonido o música ni asignaciones de teclas por jugador.

### Estadísticas actuales

- `Stats.action` es un `Map<String, Integer>` con `speed`, `goal`, `kick` y `jump` inicializados en 1; `points` inicia en 0.

### Reglas futuras fuera del diagrama actual

- Se conserva para una versión futura la fórmula de supervivencia `vidaMáxima = 100 × (1 + 0,10 × (supervivencia - 1))`. `survival` no forma parte del `Map` de estadísticas del diagrama actual.

## Estado actual del código (no equivale a una regla confirmada)

- `Game.startSession` crea reflectivamente una subclase de `GameSession`, le pasa `player`, el nombre de la modalidad y sus parámetros, y agrega la instancia a la lista de sesiones.
- `PlayerVsBot` construye un jugador humano y un bot; `PlayerVsPlayer` construye dos jugadores humanos.
- En la implementación actual, `PlayerVsPlayer` coloca al jugador 1 a la derecha y al jugador 2 a la izquierda. `PlayerVsBot` coloca al jugador humano a la derecha y al bot a la izquierda, tanto cuando el humano es el jugador 1 como cuando es el jugador 2; esto coincide con la regla confirmada.
- `GameSession` instancia una subclase de `TypeOfMatch` mediante reflexión.
- `ByGoals` recibe el límite de goles como primer parámetro de su lista. Su método `check` recibe dos marcadores.
- `ByTime` recibe los minutos del partido como primer parámetro de su lista. Su método `check` lee los marcadores desde las posiciones 0 y 1 y el tiempo transcurrido desde la posición 2.
- `ByTime.suddenDeath` se activa cuando se agota el tiempo con empate; en ese caso `check` devuelve `true` y, si un marcador queda desigual, se apaga al terminar.
- Para ajustarse al diagrama, se retiraron de `domain` `Scoreboard`, `Outfit`, `Head`, `Shoe`, `Rule` y `Side`. `Action` se conserva porque aparece en el tipo de retorno de `BotPlayer.decideAction`.
- Se conservan los parámetros de jugador y reglas en los constructores de `GameSession`, `PlayerVsBot` y `PlayerVsPlayer`, y la lista de parámetros en las fábricas reflectivas. `TypeOfMatch.check` conserva su contrato `boolean` con parámetros, de acuerdo con la regla de continuidad confirmada.
- `GameSession` coordina el paso de juego, aplica contactos balón-participante, registra el marcador, reposiciona a los participantes tras gol y detiene sesiones terminadas conforme a la modalidad. `SoccerField` recorre sus balones para avanzar su estado y coordina los rebotes contra límites, arcos y suelo; `Ball` conserva estado físico y ejecuta las operaciones delegadas.
- `GameSession.kickPlayer` permite patear solo a participantes registrados, en sesiones activas y cuando la superficie del balón está a 8 unidades o menos del borde de su hitbox. El dominio aplica un impulso horizontal de 150 unidades/s hacia el arco contrario y un impulso vertical de -50 unidades/s. La pantalla de prueba solo envía la acción; no consulta ni modifica la estadística `kick`.

## Decisiones pendientes

### Inicio y participantes

- Confirmar el mapeo exacto de `player`: ¿`true` identifica al jugador 1 y `false` al jugador 2?
- ¿Una sesión inicia inmediatamente con los participantes predeterminados o debe esperar a que se registren los usuarios?
- ¿Qué debe ocurrir si se intenta crear una sesión con listas nulas, vacías o parámetros incompletos?

### Victoria, goles y tiempo

- ¿Qué evento termina la muerte súbita cuando el partido está empatado al agotarse el tiempo?

### Movimiento y física

- ¿Qué velocidades, aceleraciones, límites y condiciones de salto/movimiento tienen los participantes?

### Marcador, estadísticas y poderes

- ¿Cómo se cargan/consumen las barras de poder y cómo se limita su valor?
- ¿Qué límites, costos y puntos disponibles tienen las estadísticas?
- ¿Qué efecto exacto aplica cada estadística, incluida `kick`?
- ¿Qué poderes existen, qué objetivo afectan, cuánto duran, qué daño producen y qué enfriamiento tienen?
- ¿Qué debe ocurrir cuando un participante queda aturdido o un efecto vence/se renueva?

### Bots, reglas y persistencia

- ¿Qué decisiones y prioridades debe seguir cada perfil de bot (`Clumsy`, `Offensive`, `Defensive`, `Expert`)?
- ¿Qué reglas genéricas se pueden agregar/quitar y en qué orden se aplican?
- ¿Qué datos guarda `save` y restaura `open`; cuál es el formato y qué errores deben reportarse?

## Registro de decisiones

| Fecha | Decisión | Confirmada por |
|---|---|---|
| 2026-11-10 | Las posiciones de `PlayerVsPlayer` son fijas; en `PlayerVsBot`, el humano siempre ocupa la derecha y el bot la izquierda, mientras `player` selecciona la identidad humana. El tiempo transcurrido inicia en cero; `ByTime.check` devuelve `true` para continuar y mantiene empate en muerte súbita. | Pablo |
| 2026-11-11 | `Ball.resetBall(width, height)` centra el balón geométricamente y pone ambas componentes de velocidad en cero. | Pablo |
| 2026-11-11 | Se retira el subsistema de configuración del jugador, incluidas preferencias de sonido, música y asignaciones de teclas. | Pablo |
| 2026-11-12 | `ByGoals` termina al igualar el límite; `ByTime.suddenDeath` se activa en empate al agotarse el tiempo; el campo reinicia cada balón a un centro `(x,y)` y deja su velocidad en cero. | Pablo |
| 2026-11-12 | `Hitbox.reposition(x,y)` mueve círculos por su centro y rectángulos por su esquina superior izquierda; las intersecciones y sus efectos quedan pendientes. | Pablo |
| 2026-11-12 | `Stats.action` usa un mapa con `speed`, `goal`, `kick` y `jump` inicializados en 1; `points` inicia en 0. | Pablo |
| 2026-11-15 | Las intersecciones geométricas incluyen el contacto entre bordes; `Hitbox.intersects` devuelve la entidad contactada y su hitbox asociada, o nombre vacío y hitbox nula si no hay contacto. Los nombres son `Ball`, `SessionParticipant` y `SoccerField`; la cancha considera sus límites, arcos y balones. | Pablo |
| 2026-11-15 | Se confirma el paso fijo de 1/60 s, gravedad 3.14, rebote con retención 70 % y umbral 0.5; impulso participante-balón ±4 horizontal y -1 vertical una vez por contacto; gol al entrar el centro en el arco, punto para el lado opuesto y reinicio simétrico. Se confirman las dos excepciones literales para objetos fuera/superpuestos y la actualización temporal ByTime en segundos con minutos enteros completados. | Pablo |
| 2026-11-15 | Se reemplaza la gravedad anterior por 40 unidades/s². El balón se separa horizontalmente del participante hasta salir de su hitbox; al tocarlo se redirige hacia fuera con impulso horizontal de magnitud 4 y suma impulso vertical -1 una vez por contacto. | Pablo |
| 2026-11-16 | El balón creado por `SoccerField` tiene radio 10 unidades. | Pablo |
