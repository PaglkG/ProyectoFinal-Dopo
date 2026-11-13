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

### Modalidad por goles

- `ByGoals` termina el partido cuando uno de los marcadores iguala exactamente el límite configurado. Superarlo no cuenta como igualdad con el límite.
- La comparación de goles permanece en `ByGoals`; las comprobaciones de tiempo y muerte súbita permanecen en `ByTime`, aunque el diagrama ubica esos helpers bajo `ByGoals`.

### Reinicio del balón

- `Ball.resetBall(width, height)` sitúa el balón en el centro geométrico de la cancha (`x = width / 2`, `y = height / 2`) y deja `velocityX` y `velocityY` en cero.
- `SoccerField.resetBall(x, y)` ubica el centro de cada balón en esas coordenadas y deja sus velocidades en cero.

### Hitboxes y medidas provisionales

- Las intersecciones se representan mediante `Hitbox`; sus efectos por pareja de objetos siguen pendientes.
- `Hitbox.reposition(x, y)` reposiciona la referencia de cada forma: centro para círculos y esquina superior izquierda para rectángulos. `Ball` y `SessionParticipant` delegan en su hitbox; `SoccerField` reposiciona las formas que contiene.
- Como propuesta provisional, la cancha mide 450 × 150 unidades, los límites tienen grosor `alto / 100`, cada arco mide `ancho / 10` por `alto / 3` y queda centrado verticalmente, y el balón tiene radio 2.
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
- `GameSession.checkTypeOfMatch` está pendiente de implementación.

## Decisiones pendientes

### Inicio y participantes

- Confirmar el mapeo exacto de `player`: ¿`true` identifica al jugador 1 y `false` al jugador 2?
- ¿Una sesión inicia inmediatamente con los participantes predeterminados o debe esperar a que se registren los usuarios?
- ¿Qué debe ocurrir si se intenta crear una sesión con listas nulas, vacías o parámetros incompletos?

### Victoria, goles y tiempo

- ¿Qué evento termina la muerte súbita cuando el partido está empatado al agotarse el tiempo?
- ¿Los minutos se manejan como minutos enteros, segundos o tiempo fraccionario? ¿Qué formato usa `check` para el tiempo transcurrido?
- ¿Qué estructura exacta debe tener la lista que `GameSession.checkTypeOfMatch` enviará a cada tipo de partido?

### Movimiento y física

- ¿Qué coordenadas, unidades y origen usa la cancha?
- ¿Qué velocidades iniciales tiene el balón?
- ¿Cuál es la ecuación de actualización, convención de ejes y valor/unidad de gravedad?
- ¿Qué rebotes y colisiones deben ocurrir con el suelo, bordes, arcos, jugadores y balón?
- ¿Qué define un gol y desde qué estado/posición se reinicia la ronda?
- ¿Qué velocidades, aceleraciones, límites y condiciones de salto/movimiento tienen los participantes?

### Marcador, estadísticas y poderes

- ¿Cómo se incrementan los marcadores y qué pasa con el empate?
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
