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

## Estado actual del código (no equivale a una regla confirmada)

- `Game.startSession` crea reflectivamente una subclase de `GameSession`, le pasa `player`, el nombre de la modalidad y sus parámetros, y agrega la instancia a la lista de sesiones.
- `PlayerVsBot` construye un jugador humano y un bot; `PlayerVsPlayer` construye dos jugadores humanos.
- En la implementación actual, `PlayerVsPlayer` coloca al jugador 1 a la derecha y al jugador 2 a la izquierda. `PlayerVsBot` coloca al jugador humano a la derecha y al bot a la izquierda, tanto cuando el humano es el jugador 1 como cuando es el jugador 2; esto coincide con la regla confirmada.
- `GameSession` instancia una subclase de `TypeOfMatch` mediante reflexión.
- `ByGoals` recibe el límite de goles como primer parámetro de su lista. Su método `check` recibe dos marcadores.
- `ByTime` recibe los minutos del partido como primer parámetro de su lista. Su método `check` lee los marcadores desde las posiciones 0 y 1 y el tiempo transcurrido desde la posición 2.
- `GameSession.checkTypeOfMatch` está pendiente de implementación.

## Decisiones pendientes

### Inicio y participantes

- Confirmar el mapeo exacto de `player`: ¿`true` identifica al jugador 1 y `false` al jugador 2?
- ¿Una sesión inicia inmediatamente con los participantes predeterminados o debe esperar a que se registren usuarios/outfits?
- ¿Qué debe ocurrir si se intenta crear una sesión con listas nulas, vacías o parámetros incompletos?

### Victoria, goles y tiempo

- ¿`ByGoals.check` debe terminar el partido al igualar el límite (`==`) o al alcanzarlo/superarlo (`>=`)?
- Al llegar al límite de tiempo con marcador desigual, ¿`ByTime.check` debe devolver `false` para indicar que terminó?
- ¿Qué evento termina la muerte súbita cuando el partido está empatado al agotarse el tiempo?
- ¿Los minutos se manejan como minutos enteros, segundos o tiempo fraccionario? ¿Qué formato usa `check` para el tiempo transcurrido?
- ¿Qué estructura exacta debe tener la lista que `GameSession.checkTypeOfMatch` enviará a cada tipo de partido?

### Movimiento y física

- ¿Qué coordenadas, unidades y origen usa la cancha?
- ¿Qué valores iniciales tienen posición, radio y velocidades del balón?
- ¿Cuál es la ecuación de actualización, convención de ejes y valor/unidad de gravedad?
- ¿Qué rebotes y colisiones deben ocurrir con el suelo, bordes, arcos, jugadores y balón?
- ¿Qué define un gol y desde qué estado/posición se reinicia la ronda?
- ¿Qué velocidades, aceleraciones, límites y condiciones de salto/movimiento tienen los participantes?

### Marcador, estadísticas y poderes

- ¿Cómo se incrementan los marcadores y qué pasa con el empate?
- ¿Cómo se cargan/consumen las barras de poder y cómo se limita su valor?
- ¿Qué valores iniciales, límites, costos y puntos disponibles tienen las estadísticas?
- ¿Qué efecto exacto aplica cada estadística, incluida `kick`?
- ¿Qué poderes existen, qué objetivo afectan, cuánto duran, qué daño producen y qué enfriamiento tienen?
- ¿Qué debe ocurrir cuando un participante queda aturdido o un efecto vence/se renueva?

### Bots, controles, reglas y persistencia

- ¿Qué decisiones y prioridades debe seguir cada perfil de bot (`Clumsy`, `Offensive`, `Defensive`, `Expert`)?
- ¿Cuáles son las teclas predeterminadas y cómo se resuelven conflictos o entradas inválidas?
- ¿Qué reglas genéricas se pueden agregar/quitar y en qué orden se aplican?
- ¿Qué datos guarda `save` y restaura `open`; cuál es el formato y qué errores deben reportarse?

## Registro de decisiones

| Fecha | Decisión | Confirmada por |
|---|---|---|
| 2026-11-10 | Las posiciones de `PlayerVsPlayer` son fijas; en `PlayerVsBot`, el humano siempre ocupa la derecha y el bot la izquierda, mientras `player` selecciona la identidad humana. El tiempo transcurrido inicia en cero; `ByTime.check` devuelve `true` para continuar y mantiene empate en muerte súbita. | Pablo |
