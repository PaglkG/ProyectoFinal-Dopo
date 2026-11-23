# Referencia de clases, atributos y métodos

Este documento describe los tipos declarados en `src/main/java` según su implementación actual. Incluye clases, interfaces, enumeraciones, records y la clase interna `FieldView`; no repite los métodos heredados de Java, Swing o de una superclase. Cuando un método está vacío, se indica como punto de extensión y no como una función ya disponible.

## Dominio (`domain`)

### `Game`

**Responsabilidad:** administrar la única sesión compartida y servir como punto de entrada sin exigir que las operaciones reciban una sesión.

**Atributos:**

| Atributo | Representa |
|---|---|
| `session: GameSession` | Referencia a la sesión singleton administrada; queda nula tras `closeSession()`. |

**Métodos:**

| Método | Qué hace |
|---|---|
| `startSession(parametersRules, parametersPowers, typeGame, typeMatche, player)` | Obtiene o crea la sesión singleton con sus reglas, poderes y, en modo contra bot, perfil; guarda su referencia única y la devuelve. |
| `closeSession()` | Limpia la referencia administrada; no destruye el singleton. |
| `resetSession()` | Reinicia la ronda de la sesión administrada sin cambiar el marcador, tiempo, modalidad ni estado final. |
| `inspectSession()` | Devuelve una instantánea inmutable de cancha, hitboxes, participantes, balón, marcador, tiempo y estado de término. |
| `movePlayer(playerOne, direction)` / `kickPlayer(playerOne)` | Ejecutan movimiento o patada; `true` selecciona al jugador 1 y `false` al jugador 2. |
| `updateGame()` | Avanza la única sesión administrada un paso fijo. |
| `setPlayerPosition(playerOne, ...)`, `setBallPosition(...)`, `applyBallImpulse(...)` | Preparan coordenadas e impulsos para escenarios visuales mediante la fachada. |
| `powerPlayer(player)` | Está vacío; no activa poderes. |
| `improveStatistics(player, stats)` | Delega en la sesión única para mejorar la habilidad seleccionada; `true` selecciona derecha/jugador 1. |
| `save()`, `save(file)` | Serializan la sesión administrada en `dopo-saved-game.ser` o en el archivo indicado. |
| `open(file)` | Deserializan una sesión y la convierten en la única instancia administrada por `Game`. |

**Records anidados de la fachada:** `SessionView` contiene copias inmutables del rectángulo de cancha, límites, arcos, participantes, balones, marcador, tiempo y estado final. `ParticipantView`, `BallView` y `RectangleView` son datos de solo lectura para la presentación.

### `GameSession` (abstracta)

**Responsabilidad:** mantener el estado de una partida concreta y coordinar participantes, cancha, marcador, movimiento, colisiones, goles y condición de término.

**Atributos:**

| Atributo | Representa |
|---|---|
| `STEP_SECONDS` | Duración constante del paso de actualización: `1/60` s. |
| `BOUNCE_RETENTION` | Fracción de velocidad conservada en rebotes: `0.70`. |
| `MIN_BOUNCE_SPEED` | Umbral mínimo de velocidad vertical tras un rebote: `0.5`. |
| `GRAVITY` | Aceleración vertical aplicada al balón: `40`. |
| `KICK_REACH` | Distancia adicional de alcance para patear: `8`. |
| `KICK_HORIZONTAL_IMPULSE` | Impulso horizontal de la patada: `150`. |
| `KICK_VERTICAL_IMPULSE` | Impulso vertical de la patada: `-50`. |
| `timeLeft: double` | En la implementación, tiempo transcurrido en segundos, aunque el nombre diga `timeLeft`. |
| `TypeOfMatch: TypeOfMatch` | Modalidad que evalúa el fin del partido. |
| `field: SoccerField` | Cancha de la sesión y acceso a sus balones y geometría. |
| `goalsLeft`, `goalsRight: int` | Marcadores de ambos lados. |
| `finished: boolean` | Indica si la sesión terminó según su modalidad. |
| `participantsRight`, `participantsLeft: List<SessionParticipant>` | Participantes registrados por lado; son `protected` para las subclases de sesión. |

**Métodos:**

| Método | Qué hace |
|---|---|
| `GameSession(player, typeMatche, parametersRules)` | Crea las listas, instancia reflectivamente la modalidad, crea la cancha y fija el tiempo inicial en cero. |
| `movePlayer(user, direction)` | Valida participante y sesión, guarda la coordenada anterior, delega el paso de movimiento en el participante y delega a `SoccerField` la resolución del contacto con balones. |
| `restartRound()` | Centra y detiene todos los balones y posiciona los participantes. |
| `goal(side)` | Si `side` es `left` o `right`, incrementa ese marcador, reinicia la ronda y consulta la modalidad. Otros textos no producen cambios. |
| `goal()` | Privado: busca un balón cuyo centro esté en un arco y registra el punto para el lado opuesto al arco. |
| `powerPlayer(player)` | Está vacío; no activa poderes. |
| `kickPlayer(player)` | Rechaza sesiones terminadas, nulos, no registrados o aturdidos; si el rival está a 8 unidades o menos, le aplica daño según `kick` y prioriza ese golpe. Si no, patea el balón al alcance. |
| `createMatchRule(typeGame, parametersRules)` | Privado: carga una modalidad por nombre, valida su tipo y constructor, y la instancia. |
| `checkTypeOfMatch()` | Pasa marcadores y, para `ByTime`, minutos completos transcurridos a la modalidad; actualiza `finished`. |
| `improveStatistics(player, stats)` | Mejora una habilidad del lado elegido con el costo vigente de su jugador; al terminar el partido se otorgan 100 puntos por victoria y 25 por derrota o empate. |
| `updateGame()` | Si la sesión sigue activa, avanza los efectos temporales un paso, suma tiempo, delega el avance de balones a la cancha, procesa colisiones y comprueba el término. |
| `collision()` | Privado: valida participantes, procesa goles, delega en la cancha las colisiones entre balones y entre balones y participantes, y procesa rebotes contra límites/arcos/suelo; puede lanzar `GameException`. |
| `positionParticipants()` | Público: delega a `SoccerField` la colocación simétrica del primer participante de cada lado, si ambos lados tienen uno. |
| `getGoalsLeft()`, `getGoalsRight()` | Devuelven el marcador correspondiente. |
| `isFinished()` | Devuelve si la sesión terminó. |
| `getElapsedSeconds()` | Devuelve el tiempo transcurrido en segundos. |
| `getField()` | Devuelve la cancha de la sesión. |
| `getParticipantsLeft()`, `getParticipantsRight()` | Devuelven copias inmutables de las listas de cada lado. |

### `PlayerVsPlayer`

**Responsabilidad:** construir una sesión con dos jugadores humanos y asignarles sus lados fijos.

**Atributos:** ninguno declarado; hereda el estado de `GameSession`.

**Métodos:**

- `PlayerVsPlayer(player, typeMatche, parametersRules, parametersPowers)`: inicializa la sesión base, registra al jugador 1 a la derecha y al jugador 2 a la izquierda, asocia los poderes indicados por nombre completo de clase y los posiciona.

### `PlayerVsBot`

**Responsabilidad:** construir una sesión con un jugador humano y un bot.

**Atributos:** ninguno declarado; hereda el estado de `GameSession`.

**Métodos:**

- `PlayerVsBot(player, typeMatche, parametersRules, parametersPowers)`: crea al jugador 1 o 2 según `player`, lo agrega a la derecha, crea el perfil bot indicado en el tercer elemento y lo agrega a la izquierda. Asocia los poderes del primer y segundo elemento, respectivamente, y posiciona ambos.

### `SoccerField`

**Responsabilidad:** contener y avanzar los balones y la geometría de cancha, contestar consultas espaciales y delegar las respuestas de colisión a `CollisionFacade`.

**Atributos:**

| Atributo | Representa |
|---|---|
| `LeftGoalHitbox`, `RightGoalHitbox: RectangularHitbox` | Geometría de los dos arcos. |
| `fieldHitbox: RectangularHitbox[]` | Límites superior, inferior, izquierdo y derecho, en ese orden. |
| `balls: Ball[]` | Balones contenidos; el constructor actual crea uno de radio 10. |
| `collisions: CollisionFacade` | Coordinador de colisiones asociado a esta cancha. |

**Métodos:**

| Método | Qué hace |
|---|---|
| `SoccerField()` | Crea la cancha de `450 × 150`, un balón de radio 10 y posiciona la geometría en el origen. |
| `advanceBalls(deltaSeconds, gravity)` | Avanza todos los balones delegando a `Ball.advance`. |
| `addBall(ball)` | Agrega un balón conservando su estado inicial; rechaza nulos y la misma instancia duplicada con `GameException`. Permite a una sesión extendida componer varios balones. |
| `resolveBallCollisions(restitution)`, `resolveBallParticipantCollisions(...)`, `resolveWalkingContact(...)` | Delegan a `CollisionFacade` la resolución de colisiones entre balones, participantes y empujes al caminar. |
| `keepBallInsideCourt(ball, retention)`, `bounceBallAtWallsAndGoalFrames(...)`, `bounceBallAtGround(...)` | Delegan en la fachada la respuesta geométrica frente a límites, arcos y suelo. |
| `getHitboxes()` | Devuelve un arreglo nuevo con límites, arcos y balones como objetos `Hitbox`. |
| `resetBall(x, y)` | Reinicia cada balón con centro en `(x, y)` y velocidad cero. |
| `getHitboxField()` | Devuelve el arreglo interno de límites. |
| `getHitboxBalls()` | Devuelve un arreglo nuevo con las hitboxes circulares de los balones. |
| `getBalls()` | Devuelve una copia del arreglo, con las mismas instancias de balón. |
| `getGoalHitboxes()` | Devuelve un arreglo nuevo con los arcos izquierdo y derecho. |
| `balls()` | Acceso de paquete: devuelve copia del arreglo de balones para el dominio. |
| `courtX()`, `courtY()`, `courtWidth()`, `courtHeight()` | Consultas de paquete de posición y dimensiones de la cancha. |
| `boundaryThickness()`, `groundY()` | Consultas de paquete del grosor de límites y altura del suelo. |
| `bounceBallAtWallsAndGoalFrames(ball, retention)` | Ajusta y rebota el balón contra laterales, techo y bordes de los arcos. |
| `bounceBallAtGround(ball, retention, minimumSpeed)` | Si el balón toca el suelo, lo coloca a la altura de contacto y delega el rebote a `Ball`. |
| `goalSideFor(ball)` | Devuelve `left`, `right` o cadena vacía según dónde esté el centro del balón. |
| `containsCenter(goal, ball)` | Privado: comprueba si el centro del balón cae dentro o en el borde del arco. |
| `isOutside(ball)` | Comprueba si alguna parte circular del balón sale del rectángulo exterior. |
| `isOutside(participant)` | Comprueba si la hitbox del participante excede el rectángulo exterior. |
| `touchesForbiddenBoundary(participant)` | Comprueba contacto del participante con techo o laterales. |
| `touchesGround(ball)` | Comprueba contacto entre balón y límite inferior. |
| `groundCenterY(ball)` | Calcula el centro vertical del balón cuando su borde toca el suelo. |
| `positionParticipants(left, right)` | Ajusta hitboxes y coloca a los participantes a cuartos y tres cuartos del ancho, apoyados en el suelo. |
| `intersects(hitbox)` | Busca si el objeto recibido toca límites, arcos o alguno de los balones. |
| `contactingPart(other)` | Devuelve qué parte de la cancha o qué balón contenido toca al objeto recibido. |
| `reposition(x, y)` | Reposiciona límites y arcos y traslada los balones para conservar su posición relativa. |

### Colisiones en `domain`

#### `CollisionFacade`

**Responsabilidad:** intermediar entre `SoccerField` y los colaboradores que consultan geometría y resuelven colisiones.

| Método | Qué hace |
|---|---|
| `CollisionFacade(field)` | Asocia la fachada con la cancha y crea sus colaboradores de geometría y resolución. |
| `resolveBallCollisions(restitution)` | Delega la resolución de contactos entre todos los pares de balones. |
| `resolveBallParticipantCollisions(participants, restitution, minimumLandingSpeed)` | Delega la resolución de los contactos entre balones y participantes. |
| `resolveWalkingContact(moved, previousX, participants, restitution)` | Delega la respuesta al contacto producido por el movimiento de un participante. |
| `keepBallInsideCourt(ball, retention)` | Corrige la posición del balón dentro de los límites jugables y aplica rebote si corresponde. |
| `bounceBallAtWallsAndGoalFrames(ball, retention)` | Resuelve rebotes del balón contra paredes y marcos de arco. |
| `bounceBallAtGround(ball, retention, minimumSpeed)` | Resuelve el contacto del balón con el suelo. |
| `isOutside(ball)`, `isOutside(participant)` | Indican si el balón o participante rebasa el rectángulo exterior de la cancha. |
| `touchesForbiddenBoundary(participant)` | Indica si el participante toca un límite superior o lateral restringido. |
| `touchesGround(ball)` | Indica si el balón toca el suelo. |
| `groundCenterY(ball)` | Devuelve la coordenada vertical del centro del balón cuando está apoyado en el suelo. |
| `goalSideFor(ball)` | Devuelve el lado del arco que contiene el centro del balón, o cadena vacía si no está en un arco. |

#### `CourtGeometry`

**Responsabilidad:** evaluar la geometría de cancha y resolver respuestas geométricas de balones y participantes.

| Método | Qué hace |
|---|---|
| `CourtGeometry(field)` | Asocia el evaluador con la cancha cuyas dimensiones y hitboxes consulta. |
| `isValidBallPosition(candidate, moved, participants)` | Valida que la posición candidata respete límites, otros balones y participantes. |
| `keepBallInsideCourt(ball, retention)` | Corrige salidas de los límites interiores y rebota el balón hacia dentro. |
| `bounceBallAtWallsAndGoalFrames(ball, retention)` | Aplica los rebotes de paredes y marcos de arco. |
| `bounceBallAtGround(ball, retention, minimumSpeed)` | Rebota el balón cuando toca el suelo. |
| `isOutside(ball)`, `isOutside(participant)` | Comprueban si la entidad sale del rectángulo exterior de cancha. |
| `touchesForbiddenBoundary(participant)` | Comprueba el contacto con el techo o los límites laterales restringidos. |
| `touchesGround(ball)` | Comprueba el contacto con la hitbox del suelo. |
| `groundCenterY(ball)` | Calcula la coordenada del centro del balón apoyado en el suelo. |
| `goalSideFor(ball)` | Detecta el arco que contiene el centro del balón; devuelve cadena vacía si no hay gol. |
| `containsCenter(goal, ball)` (privado) | Comprueba si el centro del balón cae dentro del rectángulo del arco. |

#### `BallCollisionResolver`

**Responsabilidad:** resolver contactos entre balones y el empuje de balones causado al caminar.

| Método | Qué hace |
|---|---|
| `resolveAll(balls, restitution)` | Recorre cada par de balones y resuelve su contacto. |
| `resolveWalkingContact(balls, moved, previousX, participants, restitution, geometry, participantResolver)` | Resuelve el contacto del participante que camina: rebota contra un balón en movimiento o intenta empujar uno quieto sin producir superposiciones inválidas. |
| `resolvePair(first, second, restitution)` (privado) | Corrige penetración entre dos balones y aplica el impulso normal cuando se aproximan. |

#### `BallParticipantCollisionResolver`

**Responsabilidad:** separar y resolver el rebote entre balones y hitboxes de participantes.

| Método | Qué hace |
|---|---|
| `resolveAll(balls, participants, restitution, minimumLandingSpeed)` | Resuelve los contactos de cada balón con los participantes recibidos. |
| `resolve(ball, participant, restitution, minimumLandingSpeed)` | Corrige la penetración, refleja la componente normal de velocidad si se aproximan y aplica la velocidad mínima de aterrizaje al contacto desde arriba. |

### `Ball`

**Responsabilidad:** almacenar el estado físico y la hitbox circular de un balón, y ejecutar operaciones de movimiento, impulso y rebote solicitadas por la sesión o cancha.

**Atributos:**

| Atributo | Representa |
|---|---|
| `physical: boolean` | Indicador declarado, actualmente sin lectura ni escritura en la clase. |
| `velocityX`, `velocityY: double` | Componentes horizontal y vertical de velocidad. |

**Métodos:**

| Método | Qué hace |
|---|---|
| `Ball(width, height, radius)` | Crea la hitbox centrada en las dimensiones indicadas, con el radio recibido. |
| `resetBall(width, height)` | Centra el balón y pone ambas velocidades en cero. |
| `getHitbox()` | Devuelve la hitbox circular asociada. |
| `getX()`, `getY()` | Devuelven las coordenadas del centro. |
| `getRadius()` | Devuelve el radio. |
| `getVelocityX()`, `getVelocityY()` | Devuelven las componentes actuales de velocidad. |
| `applyImpulse(deltaX, deltaY)` | Suma los cambios recibidos a las velocidades actuales. |
| `ensureHorizontalDirection(direction, minimumSpeed)` | Acceso de paquete: redirige la velocidad horizontal si apunta hacia el lado incorrecto o es cero. |
| `advance(deltaSeconds, gravity)` | Suma gravedad a la velocidad vertical y luego desplaza la hitbox con las velocidades resultantes. |
| `bounceAt(centerY, retention, minimumSpeed)` | Acceso de paquete: fija la altura de contacto y refleja/reduce la velocidad vertical si el balón baja; la detiene si queda bajo el umbral. |
| `bounceHorizontallyAt(centerX, retention)` | Acceso de paquete: fija la coordenada horizontal de contacto e invierte la velocidad horizontal con retención. |
| `bounceVerticallyAt(centerY, retention)` | Acceso de paquete: fija la coordenada vertical de contacto e invierte la velocidad vertical con retención. |
| `intersects(hitbox)` | Delega la detección geométrica a `HitboxIntersections`. |
| `reposition(x, y)` | Mueve el centro de la hitbox circular. |

### `SessionParticipant` (abstracta)

**Responsabilidad:** definir el estado y las operaciones compartidas por un jugador humano y un bot, y representar su hitbox rectangular.

**Atributos:**

| Atributo | Representa |
|---|---|
| `x`, `y: double` | Coordenadas almacenadas de la esquina superior izquierda; se sincronizan al reposicionar. |
| `UserHitbox: RectangularHitbox` | Geometría de colisión del participante. |
| `stats: Stats` | Estadísticas inicializadas al crear al participante; incluyen niveles de acción y puntos de victoria. |
| `status: List<StatusEffect>` | Efectos temporales activos, incluido el aturdimiento. |
| `life: int` | Vida actual; inicia en 100 y aumenta junto con la vida máxima al mejorar `survival`. La vida máxima se calcula desde `Stats`. |
| `specialPower: Power` | Poder asociado al participante durante la creación de la sesión; su activación aún no está implementada. |

**Métodos:**

| Método | Qué hace |
|---|---|
| `SessionParticipant()` | Crea una hitbox provisional en el origen, con dimensiones iniciales basadas en `450 × 150`. |
| `move(dir)` | Desplaza el participante 150 × `speed` / 60 unidades en la dirección indicada. |
| `getStats()` / `setStats(stats)` | Consultan o asocian las estadísticas; `Game` comparte por ID humano la instancia guardada entre sesiones. `setStats(null)` produce `GameException`. |
| `takeDamage(damage)` | Reduce vida sin bajar de cero; al llegar a cero aplica `StatusEffect.STUNNED` por 3 segundos. |
| `blocksMovement()` / `updateStatusEffects()` | Bloquea movimiento durante el aturdimiento, avanza su temporizador y restaura vida máxima al expirar. |
| `getHitboxAsInterface()` | Devuelve la hitbox asociada usando el tipo general `Hitbox`. |
| `jump()` | Está vacío; no modifica el estado. |
| `resetPosition(x, y)` | Delega el cambio de posición a `reposition`. |
| `power()` | Está vacío; no aplica poder. |
| `getSpecialPower()` | Devuelve el poder asociado. |
| `setSpecialPower(power)` | Asocia el poder al participante. |
| `improveStatistics(stats)` | Delega en `Stats` la mejora de un nivel hasta el nivel 10, con el precio vigente del jugador. |
| `getHitbox()` | Devuelve la hitbox rectangular. |
| `resizeHitbox(width, height)` | Público: cambia sus dimensiones sin moverla. |
| `intersects(hitbox)` | Delega la detección geométrica a `HitboxIntersections`. |
| `reposition(x, y)` | Actualiza `x`, `y` y la esquina superior izquierda de la hitbox. |

### `Player`

**Responsabilidad:** representar un participante controlado por una persona e identificarlo por número.

**Atributos:** `user: int`, identificador guardado al construirlo.

**Métodos:** `Player(user)` guarda el identificador; `getUserId()` lo devuelve para asociar estadísticas entre sesiones. Los demás comportamientos se heredan de `SessionParticipant`.

### `BotPlayer` y perfiles

**`BotPlayer` — responsabilidad:** base común para participantes controlados por la máquina.

**Atributos:** ninguno declarado.

**Métodos:**

- `BotPlayer()`: constructor sin estado de decisión adicional.
- `decideAction(field)`: punto de extensión; actualmente devuelve `null` y no decide acciones.

**Perfiles derivados:** `Clumsy`, `Offensive`, `Defensive` y `Expert` representan perfiles diferenciados por nombre. No declaran atributos, constructores ni métodos propios; heredan el constructor implícito y el comportamiento de `BotPlayer`. Los perfiles aún no implementan estrategias distintas.

### `Stats`

**Responsabilidad:** guardar puntos disponibles y niveles de acciones del participante.

**Atributos:**

| Atributo | Representa |
|---|---|
| `points: int` | Puntos disponibles; inicia en cero. |
| `action: Map<String, Integer>` | Niveles `speed`, `jump`, `kick` y `survival`; cada uno inicia en uno y llega hasta diez. |

**Métodos:**

| Método | Qué hace |
|---|---|
| `getPoints()` | Devuelve los puntos disponibles. |
| `getKickMultiplier()` / `getKickDamage()` | Devuelve el multiplicador de 1,0 a 1,9 y el daño normal de 10 a 19 según `kick`. |
| `getMaxLife()` | Devuelve la vida máxima entre 100 y 190 según `survival`. |
| `getNextUpgradeCost()` | Devuelve el precio de la próxima compra, que inicia en 500 y se duplica tras cada mejora. |
| `getStatistic(statistic)` | Devuelve el nivel de una habilidad válida o lanza `GameException`. |
| `awardWin()` / `awardLoss()` / `awardDraw()` | Agregan 100 puntos por victoria o 25 por derrota o empate. |
| `improveStatistics(stats)` | Incrementa una habilidad válida en 1 hasta nivel 10 y descuenta el precio vigente; lanza `GameException` si faltan puntos o la habilidad es inválida. |

### `StatusEffect`

**Responsabilidad:** representar un efecto temporal que modifica el movimiento del participante.

**Atributos:** `type: String` identifica el tipo; `duration: double` la duración configurada; `remaining: double` el tiempo restante. `STUNNED` y `STUN_DURATION_SECONDS` identifican el aturdimiento de 3 segundos.

**Métodos:** `update()` descuenta un paso de 1/60 s; `isExpired()` identifica el final; `refresh()` reinicia el temporizador; `blocksMovement()` devuelve `true` durante el aturdimiento y los factores de velocidad/salto se vuelven cero.

### `Power`

**Responsabilidad:** clase abstracta base para poderes activables.

**Atributos:** `chargePower: int`, carga del poder; queda en cero por inicialización predeterminada de Java.

**Métodos:** `Power()` no altera el valor inicial; `usePower()` está vacío; `isUsable()` es privado, está vacío y no consulta ni devuelve disponibilidad.

**Subclases:** `BlueAura`, `Lightning`, `Tornado` e `Illusion` son tipos concretos sin lógica de disparo implementada todavía.

**`PowerFactory`:** `create(className)` instancia reflectivamente un poder por nombre de clase completo. Los errores de clase, tipo, constructor e invocación se traducen a `GameException` como en la fábrica reflectiva de sesiones. `PlayerVsBot` resuelve el perfil por nombre y usa el mismo mapeo de excepciones al instanciarlo.

### `GameException`

**Responsabilidad:** comunicar errores del dominio como `IllegalArgumentException`, con mensajes compartidos para usos conocidos.

**Atributos:** constantes públicas de mensaje. `NO_FOUND_TYPE_GAME` indica que no se encontró la modalidad; `NO_FOUND_SESSION`, que no se encontró la sesión; `NO_CONSTRUCTOR_FOUND`, que no existe el constructor requerido; `NO_INSTANTIATE_CLASS`, que no se pudo instanciar; `NO_ACCESSIBLE_CONSTRUCTOR`, que el constructor no es accesible; `EXCEPTION_IN_METHOD`, que un constructor o método interno lanzó una excepción; `MINUTES_NOT_NEGATIVE` y `GOALS_NOT_NEGATIVE` identifican parámetros negativos; `OBJECT_OUT_OF_MATCH` indica objeto fuera de partida; `OBJECTS_OVERLAPPED` indica participantes superpuestos; `INVALID_BALL` identifica un balón nulo o una instancia ya agregada.

**Métodos:** `GameException(message)` construye la excepción con mensaje; `GameException(message, cause)` conserva además la causa original.

## Reglas

### `TypeOfMatch` (abstracta)

**Responsabilidad:** declarar el contrato de comprobación de continuidad o término de una modalidad.

**Atributos:** ninguno.

**Métodos:** `TypeOfMatch()` constructor base; `check(parameters)` método abstracto que cada modalidad implementa. El significado del booleano depende de la modalidad: `ByTime` devuelve si continúa y `ByGoals` indica si se alcanzó el límite.

### `ByGoals`

**Responsabilidad:** terminar por el límite de goles configurado.

**Atributos:** `goals: int`, límite leído del primer parámetro.

**Métodos:** `ByGoals(parametersRules)` interpreta el primer texto como entero y rechaza límites negativos con `GameException`; `check(parameters)` lee los marcadores izquierdo y derecho; `isGoals(playerLeft, playerRight)` devuelve `true` si cualquiera iguala exactamente el límite.

### `ByTime`

**Responsabilidad:** terminar al agotarse el tiempo si hay ganador y continuar en empate durante muerte súbita.

**Atributos:** `minutes: int`, límite en minutos; `suddenDeath: boolean`, indicador de muerte súbita.

**Métodos:** `ByTime(parametersRules)` lee el primer elemento como minutos y rechaza negativos; `check(parameters)` evalúa ambos marcadores y minutos transcurridos, actualizando muerte súbita cuando el límite ya se alcanzó; `isSuddenDeath(goalLeft, goalRight)` comprueba empate; `isTime(minutesInGame)` comprueba si se igualó o superó el límite.

## Geometría (`domain`)

### `Hitbox` (interfaz)

**Responsabilidad:** unificar detección de intersecciones y reposicionamiento de formas o entidades geométricas.

**Atributos:** ninguno.

**Métodos:** `intersects(hitbox)` devuelve el contacto detectado; `reposition(x, y)` reposiciona la forma preservando tamaño. Para círculos `(x,y)` es el centro; para rectángulos, la esquina superior izquierda.

### `HitboxContact` (clase final)

**Responsabilidad:** transportar el nombre de la entidad contactada y la forma concreta implicada.

**Componentes:** `name: String`, nombre simple o cadena vacía; `hitbox: Hitbox`, forma implicada o `null` si no hubo contacto.

**Métodos:** constructor, accesores `name()` y `hitbox()`, `none()` devuelve la instancia compartida sin contacto; `isContact()` devuelve `true` si hay nombre y hitbox no nulos. `equals`, `hashCode` y `toString` comparan y representan los dos campos.

### `HitboxIntersections`

**Responsabilidad:** concentrar cálculos geométricos; detecta contactos, pero no aplica consecuencias físicas.

**Atributos:** ninguno; clase utilitaria no instanciable.

**Métodos:** `HitboxIntersections()` es privado; `intersects(first, second)` resuelve entidades y formas; `shapeOf(hitbox)` extrae la forma; `shapesIntersect(first, second)` selecciona el cálculo por par de formas; `contactFor(target)` construye la respuesta para la entidad objetivo; `circlesIntersect(first, second)` compara distancia entre centros con suma de radios; `rectanglesIntersect(first, second)` compara intervalos normalizados; `circleIntersectsRectangle(circle, rectangle)` compara el círculo con el punto más cercano del rectángulo.

### `CircularHitbox`

**Responsabilidad:** almacenar y representar una forma circular.

**Atributos:** `x`, `y: double` centro; `radius: double` radio.

**Métodos:** `CircularHitbox(x, y, radius)` inicializa la forma; `intersects(hitbox)` delega el cálculo; `reposition(x, y)` cambia el centro sin cambiar el radio; `getX()`, `getY()` y `getRadius()` consultan sus valores.

### `RectangularHitbox`

**Responsabilidad:** almacenar y representar una forma rectangular.

**Atributos:** `x`, `y: double` esquina superior izquierda; `width`, `height: double` dimensiones.

**Métodos:** `RectangularHitbox(x, y, width, height)` inicializa la forma; `intersects(hitbox)` delega el cálculo; `reposition(x, y)` cambia la esquina sin cambiar dimensiones; `getX()`, `getY()`, `getWidth()`, `getHeight()` consultan valores; `resize(width, height)` cambia dimensiones sin mover la esquina.

### Action (enumeración de domain)

**Responsabilidad:** enumerar las acciones previstas para un participante controlado por el programa.

**Valores:** `LEFT`, `RIGHT`, `JUMP`, `POWER`. No declara atributos ni métodos propios.

## Presentación (presentation)

La aplicación principal usa Swing y todavía no está conectada a una sesión del dominio.

- DOPOSoccerGUI crea la ventana, registra las pantallas en un CardLayout y configura el cierre.
- NavigationController cambia la pantalla visible y cierra la ventana.
- BasePanel es la clase abstracta con los contratos prepareView() y prepareController().
- MainMenuPanel, ConfigurationPanel, PersonalizationPanel y StoreImprovementPanel implementan interfaces locales. La configuración de teclado y sonido no se persiste; la tienda mantiene niveles y precios provisionales en memoria.
- PreparationPanel, MatchPanel y PausePanel son pantallas sin lógica de partida conectada.
- RoundedButton, RoundedPanel y Theme aportan componentes y estilos compartidos.

No hay clases App, GameGUI, GameController, Screen ni ScreenManager en el código actual. La presentación principal no importa tipos de domain; el banco visual independiente consume la fachada Game.

## Banco de pruebas visual (`presentation.testing`)

### `MechanicsTestApp`

**Responsabilidad:** iniciar la ventana independiente del banco manual de mecánicas.

**Atributos:** ninguno.

**Métodos:** constructor privado impide crear instancias; `main(args)` presenta la configuración inicial; `showMechanicsWindow(configuration)` abre el banco con las opciones escogidas; el manejador `windowClosed(event)` detiene el temporizador del panel al cerrar.

### `MechanicsTestConfiguration`

**Responsabilidad:** conservar los nombres completos de clases y parámetros elegidos para crear o reiniciar una sesión del banco.

**Componentes:** `sessionMode` y `matchMode` indican los modos seleccionados; `limit` contiene el límite visible; `sessionClass` y `matchClass` guardan los nombres completos de clase; `ruleParameters` conserva sus argumentos; `rightPowerClass` y `leftPowerClass` identifican los poderes; `botProfile` especifica el perfil del bot; `playerOne` selecciona la identidad humana.

**Métodos:** constructor compacto copia la lista de parámetros para hacerla inmutable; `defaults()` produce la configuración estándar PVP por goles.

### `MechanicsTestSetupPanel`

**Responsabilidad:** recoger la configuración extensible antes de abrir la ventana de simulación.

**Atributos:** `onStart: Consumer<MechanicsTestConfiguration>` recibe la configuración; selectores para modo de sesión/modalidad; campos de texto para nombres de clase, límite, parámetros, poderes y perfil de bot; `playerOne` marca la identidad seleccionada.

**Métodos:** `MechanicsTestSetupPanel(onStart)` arma el formulario; `addField(panel, label, field)` privado agrega una fila; `start()` privado valida el límite, convierte los parámetros separados por coma en lista y entrega la configuración al consumidor, mostrando errores en un diálogo.

### `MechanicsTestController`

**Responsabilidad:** preparar sesiones y escenarios para inspección manual comunicándose solo con `Game`.

**Enumeraciones anidadas:**

- `MatchMode`: `GOALS` y `TIME`, con atributo `label`; constructor asigna etiqueta y `toString()` la devuelve para la interfaz.
- `SessionMode`: `PLAYER_VS_PLAYER` y `PLAYER_VS_BOT`, con atributo `label`; constructor asigna etiqueta y `toString()` devuelve texto legible.
- `Scenario`: enumera gol izquierdo/derecho, contacto, rebotes, colisión entre dos balones, participante fuera/límite, superposición, patada y victoria en muerte súbita; no declara campos ni métodos.

**Atributos:** `game: Game` única puerta al dominio; la sesión se obtiene por la fachada única `Game`; `selectedSessionMode`, `selectedMatchMode` y `selectedLimit` opciones seleccionadas; `configuration: MechanicsTestConfiguration` clases y parámetros elegidos; `movement: PresentationMovement` entrada visual.

**Métodos:**

| Método | Qué hace |
|---|---|
| `MechanicsTestController()` | Inicia el banco con la configuración predeterminada. |
| `MechanicsTestController(configuration)` | Crea la sesión usando la configuración recibida y únicamente la fachada `Game`. |
| `startNewSession(sessionMode, matchMode, limit)` | Guarda opciones y crea una sesión estándar nueva. |
| `restartConfiguredSession()` | Vuelve a crear la sesión con la configuración elegida. |
| `createConfiguredSession()` | Privado: ordena parámetros de regla y poderes, crea `Game` e inicia o reutiliza la sesión mediante `startSession`, ignorando el objeto que devuelve. |
| `step()` | Consulta si la sesión sigue activa, actualiza controles visuales y avanza el dominio mediante `Game`; limpia movimiento tras un gol. |
| `setMovementControl(control, pressed)` | Paquete: reenvía el estado de tecla a `PresentationMovement`. |
| `kickLeft()` | Paquete: intenta patear con el primer jugador izquierdo; no aplica en modo contra bot. |
| `kickRight()` | Paquete: intenta patear con el primer participante derecho. |
| `runScenario(scenario)` | Crea sesión limpia, prepara el escenario mediante `Game` y ejecuta el dominio; la colisión entre balones necesita al menos dos. |
| `getSessionView()` | Devuelve una instantánea `SessionView` para que la interfaz la dibuje. |

### `MechanicsTestPanel`

**Responsabilidad:** exponer la configuración seleccionada, simular, patear y activar escenarios, además de dibujar la instantánea proporcionada por el controlador.

**Atributos:** `controller: MechanicsTestController` coordina el dominio; `sessionMode`, `matchMode` y `limit` muestran opciones de solo lectura elegidas al inicio; `status`, `score`, `controlHelp: JLabel` muestran mensajes, marcador e instrucciones; `fieldView: FieldView` dibuja la cancha; `timer: Timer` avanza pasos visuales; `runButton`, `kickLeftButton`, `kickRightButton: JButton` son botones de control; `kickKeysDown: Set<Integer>` evita repetir patadas por mantener una tecla pulsada.

**Métodos:**

| Método | Qué hace |
|---|---|
| `MechanicsTestPanel()` | Construye y organiza controles, escenarios, campo, teclado y sesión inicial. |
| `MechanicsTestPanel(configuration)` | Construye la ventana de simulación con las opciones elegidas al inicio. |
| `stopSimulation()` | Detiene el temporizador Swing. |
| `buildSettings()` | Privado: crea selectores y controles de nueva sesión, paso y simulación. |
| `buildScenarios()` | Privado: crea acciones de patada y lista desplazable de escenarios. |
| `wrapScenarioScroll(scroll)` | Privado: coloca la lista desplazable en un contenedor. |
| `addScenario(panel, title, scenario)` | Privado: añade un botón que ejecuta el escenario, muestra errores y refresca. |
| `buildFooter()` | Privado: compone marcador, estado e instrucciones. |
| `configureMovementKeys()` | Privado: conecta teclas de dirección, salto y patada. |
| `bindKey(keyCode, name, control)` | Privado: conecta pulsación/liberación con un control lógico. |
| `bindKickKey(keyCode, name, leftPlayer)` | Privado: conecta patada y limita la acción a una por pulsación. |
| `performKick(leftPlayer)` | Privado: intenta patear, escribe el resultado y refresca el dibujo. |
| `label(text)` | Privado: crea una etiqueta de texto con el estilo común. |
| `startSession()` | Privado: detiene la sesión anterior, reinicia la configuración elegida e inicia la simulación. |
| `toggleSimulation()` | Privado: pausa o reanuda el temporizador. |
| `advanceOneStep(showStepStatus)` | Privado: avanza el controlador; detiene por término/error y refresca el panel. |
| `refreshView()` | Privado: actualiza el texto de marcador/tiempo/velocidad y solicita repintado. |
| `focusGameField()` | Privado: solicita foco al área gráfica en el hilo Swing. |

**Clase interna `FieldView`:** panel gráfico interno sin atributos propios. `FieldView()` configura tamaño/fondo/foco; `paintComponent(graphics)` dibuja cancha, arcos, participantes y balones; `drawStadium(g)` pinta el fondo decorativo; `drawGoal(g, x, y, width, height)` pinta marco y red; `drawVelocityIndicator(g, x, y, velocityY)` representa la velocidad vertical; `drawParticipants(...)` escala y dibuja los participantes según sus hitboxes.

### `PresentationMovement`

**Responsabilidad:** pasar movimientos horizontales a `Game` y conservar una simulación visual del salto.

**Constantes:** `STEP_SECONDS = 1/60` duración de paso; `JUMP_SPEED = 110` y `GRAVITY = 180` parámetros del salto visual; `EDGE_GAP = 0.02` separación de seguridad respecto a límites.

**Enumeración `Control`:** `LEFT_LEFT`, `LEFT_RIGHT`, `LEFT_JUMP`, `RIGHT_LEFT`, `RIGHT_RIGHT`, `RIGHT_JUMP`; identifican controles lógicos de cada lado.

**Atributos:** `held` teclas mantenidas; `jumpRequested` solicitudes de salto del flanco inicial; `verticalSpeeds` velocidades verticales por id de participante; `activeGame` identifica la fachada `Game` que consulta.

**Métodos:**

| Método | Qué hace |
|---|---|
| `setControl(control, pressed)` | Registra/libera tecla y registra una solicitud de salto solo al primer key-down. |
| `clearInputs()` | Limpia teclas mantenidas y saltos pendientes. |
| `reset(game)` | Limpia controles y velocidades y establece la sesión activa. |
| `resetAfterRound(game)` | Limpia solicitudes de salto/velocidades tras gol y conserva la sesión activa. |
| `update(game, mode)` | Consulta una instantánea, envía movimiento horizontal por `Game` y actualiza el salto visual. |
| `updateActor(game, actor, opponent, controls...)` | Privado: delega el movimiento horizontal a `Game`, calcula el salto visual y limita el cruce vertical del balón; puede saltar desde la cara superior. |
| `supportedByBall(balls, x, y, width, height)` | Privado: identifica si un balón sostiene al participante para permitir un nuevo salto. |
| `resolveBallVerticalContact(balls, x, oldY, nextY, width, height, verticalSpeed)` | Privado: detiene el desplazamiento vertical en la cara superior al caer o en la inferior al ascender. |
| `overlapsHorizontally(x, width, ball)` | Privado: comprueba que el participante esté horizontalmente sobre el círculo del balón. |
| `touches(x, y, width, height, other)` | Privado: comprueba el solapamiento geométrico de dos vistas de participante. |
| `isJump(control)` | Privado: indica si el control representa salto. |
| `clamp(value, minimum, maximum)` | Privado: restringe el valor al intervalo indicado. |

## Estado de las implementaciones

Los métodos vacíos y los campos no inicializados se describen como tales porque esta referencia documenta el código actual, no las funciones previstas. En especial, las pantallas de presentación, los poderes, efectos y salto del dominio tienen partes aún no implementadas; la persistencia guarda la sesión serializable y el banco simula visualmente el salto y permite aterrizar sobre los balones.
