# Referencia de clases, atributos y métodos

Este documento describe los tipos declarados en `src/main/java` según su implementación actual. Incluye clases e interfaces, además de las clases internas `FieldView` y `GameCanvas`; no repite los métodos heredados de Java, Swing o de una superclase. Cuando un método está vacío, se indica como punto de extensión y no como una función ya disponible.

## Dominio (`domain`)

### `Game`

**Responsabilidad:** administrar la sesión única y servir de fachada escalar a presentación.

**Atributos:** `session: GameSession` conserva la sesión administrada; `DEFAULT_SAVE_FILE` es el nombre inicial propuesto para guardar.

**Métodos:** `startSession(...)` inicia o reutiliza la sesión; `resetSession()` reinicia la ronda; `movePlayer(...)` y `kickPlayer(...)` delegan movimiento y patada por identidad; los getters `getPlayerX/Y/Width/Height/Speed`, `getBallX/Y/Radius/VelocityX/VelocityY/Count`, `getGoalX/Y/Width/Height`, `getCourtX/Y/Width/Height`, `getGoalsLeft/Right`, `getElapsedSeconds`, `isFinished` y `getMatchState` exponen datos escalares a la presentación. `getSessionType`, `getSessionTypes`, `getMatchTypes` y `getBotTypes` describen sesiones/reglas disponibles; `powerPlayer` es un punto de extensión vacío; `improveStatistics`, `save(File)`, `open(File)` y `updateGame()` modifican la sesión administrada.
`jumpPlayer(player)` solicita el salto por identidad y `getPlayerY(player)` devuelve la altura de la hitbox física.

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
| `movePlayer(user, direction)` | Valida participante y sesión, proyecta el paso para comprobar que evitará penetrar en un límite lateral, un arco u otro participante y, si procede, mueve y delega a `SoccerField` el contacto con balones. |
| `restartRound()` | Centra y detiene todos los balones y posiciona los participantes. |
| `goal(side)` | Si `side` es `left` o `right`, incrementa ese marcador, reinicia la ronda y consulta la modalidad. Otros textos no producen cambios. |
| `goal()` | Privado: busca un balón cuyo centro esté en un arco y registra el punto para el lado opuesto al arco. |
| `powerPlayer(player)` | Está vacío; no activa poderes. |
| `kickPlayer(player)` | Rechaza sesiones terminadas, nulos, no registrados o aturdidos; si el rival está a 8 unidades o menos, le aplica daño según `kick` y prioriza ese golpe. Si no, patea el balón al alcance. |
| `createMatchRule(typeGame, parametersRules)` | Privado: carga una modalidad por nombre, valida su tipo y constructor, y la instancia. |
| `checkTypeOfMatch()` | Pasa marcadores y, para `ByTime`, minutos completos transcurridos a la modalidad; actualiza `finished`. |
| `improveStatistics(player, stats)` | Mejora una habilidad del lado elegido con el costo vigente de su jugador; al terminar el partido se otorgan 100 puntos por victoria y 25 por derrota o empate. |
| `jumpPlayer(user)` | Inicia el salto de un participante registrado, habilitado para actuar y apoyado en el suelo o en la cabeza de otro participante. |
| `updateGame()` | Si la sesión sigue activa, avanza efectos y saltos, suma tiempo, avanza los balones, procesa colisiones y comprueba el término. |
| `collision(allParticipants)` | Privado: valida posiciones, actualiza los balones apoyados, recupera contactos atravesados, procesa goles y delega los rebotes a la cancha. |
| `positionParticipants()` | Público: delega a `SoccerField` la colocación simétrica y el ajuste de la hitbox a la coronilla de cada participante, si ambos lados tienen uno. |
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

- `PlayerVsBot(player, typeMatche, parametersRules, parametersPowers)`: crea al jugador 1 o 2 según `player`, lo agrega a la izquierda, crea el perfil bot indicado en el tercer elemento y lo agrega a la derecha. Asocia los poderes del primer y segundo elemento, respectivamente, y posiciona ambos.

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
| `getHitboxes()` | Devuelve un arreglo nuevo con las formas hitbox de límites, arcos y balones. |
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
| `intersects(target)` | Busca si la entidad o forma recibida toca límites, arcos o alguno de los balones. |
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
| `rewindMissedContacts(balls, participants)` | Recupera el primer contacto atravesado en el recorrido relativo de cada balón antes de comprobar un gol. |
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
| `intersects(target)` | Delega la detección geométrica a `HitboxIntersections`. |
| `reposition(x, y)` | Mueve el centro de la hitbox circular. |

### `SessionParticipant` (abstracta)

**Responsabilidad:** definir el estado y las operaciones compartidas por un jugador humano y un bot; contiene una hitbox rectangular y no implementa `Hitbox`.

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
| `jump()` | Inicia el impulso vertical del participante si puede actuar y aún no está saltando. |
| `resetPosition(x, y)` | Delega el cambio de posición a `reposition`. |
| `power()` | Está vacío; no aplica poder. |
| `getSpecialPower()` | Devuelve el poder asociado. |
| `setSpecialPower(power)` | Asocia el poder al participante. |
| `improveStatistics(stats)` | Delega en `Stats` la mejora de un nivel hasta el nivel 10, con el precio vigente del jugador. |
| `getHitbox()` | Devuelve la hitbox rectangular. |
| `resizeHitbox(width, height)` | Público: cambia sus dimensiones sin moverla. |
| `intersects(target)` | Delega la detección geométrica a `HitboxIntersections`. |
| `reposition(x, y)` | Actualiza `x`, `y` y la esquina superior izquierda de la hitbox. |

### `Player`

**Responsabilidad:** representar un participante controlado por una persona e identificarlo por número.

**Atributos:** `user: int`, identificador guardado al construirlo.

**Métodos:** `Player(user)` guarda el identificador; `getUserId()` lo devuelve para asociar estadísticas entre sesiones. Los demás comportamientos se heredan de `SessionParticipant`.

### `BotPlayer` y perfiles

**`BotPlayer` — responsabilidad:** base común para participantes controlados por la máquina.

**Relaciones:** `BotPlayer` hereda de `SessionParticipant`, conserva una `BotDecisionStrategy` y crea un `BotDecisionContext` por decisión. `PlayerVsBot` hereda de `GameSession` y ejecuta las acciones del bot derecho mediante los métodos de la sesión.

**Métodos de `BotPlayer`:**

- `BotPlayer()` crea el participante. Su política se construye al decidir por primera vez.
- `decideAction(field)` aplica el flujo común con una diferencia de goles neutra. Conserva el contrato `Set<String>`.
- `decideAction(field, goalDifference, supportY)` descarta el paso si el bot está aturdido o falta la cancha, observa el alcance desde el apoyo actual y devuelve como máximo una acción.
- `createDecisionStrategy()` habilita que cada perfil proporcione su política al flujo común. La política base espera.

**Tipos nuevos:**

- `BotDecisionStrategy` define `chooseAction(context)`. Las estrategias devuelven un nombre de acción o texto vacío y reciben una observación sin referencias modificables a entidades del partido.
- `BotDecisionContext` captura el centro del bot, el balón más cercano, el alcance de la patada, la entrada del arco derecho y la diferencia de goles. `hasBall()`, `ballX()`, `ownGoalFrontX()`, `ballInReach()` y `goalDifference()` consultan esos datos. `moveToward(targetX)` devuelve una dirección cuando un paso acerca al objetivo.

**Integración:** `GameSession.updateGame()` llama a `updateAutonomousParticipants()` antes de avanzar el balón. `PlayerVsBot` implementa este paso y solicita el movimiento al mismo `movePlayer` que usa el humano. `GameSession.fitsHorizontalStep(user, right, participants)` usa `SessionParticipant.horizontalStep()` para validar la posición proyectada. `GameSession.isBallInKickReach(box, ball)` proporciona la misma comprobación de alcance a `kickPlayer` y a la observación.

**Perfiles derivados:** `Clumsy`, `Offensive`, `Defensive` y `Expert` redefinen `createDecisionStrategy()` para crear su política de `BotDecisionStrategies`. Las cuatro políticas implementan `BotDecisionStrategy`:

- `RandomMovement.chooseAction(context)` mantiene izquierda, derecha o espera durante 18 pasos y luego vuelve a sortear.
- `Attack.chooseAction(context)` se acerca al balón más cercano o pide patear si un balón está al alcance.
- `DefendGoal.chooseAction(context)` pide despejar si hay un balón al alcance. Si no, se posiciona entre el balón y la entrada del arco propio.
- `AdaptToScore.chooseAction(context)` delega en `DefendGoal` con ventaja y en `Attack` en empate o desventaja.

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

**Atributos:** constantes públicas de mensaje. `NO_FOUND_TYPE_GAME` indica que no se encontró la modalidad; `NO_FOUND_SESSION`, que no se encontró la sesión; `NO_CONSTRUCTOR_FOUND`, que no existe el constructor requerido; `NO_INSTANTIATE_CLASS`, que no se pudo instanciar; `NO_ACCESSIBLE_CONSTRUCTOR`, que el constructor no es accesible; `EXCEPTION_IN_METHOD`, que un constructor o método interno lanzó una excepción; `MINUTES_NOT_NEGATIVE` y `GOALS_NOT_NEGATIVE` identifican parámetros negativos; `OBJECT_OUT_OF_MATCH` indica objeto fuera de partida; `OBJECTS_OVERLAPPED` conserva el mensaje histórico de superposición; `INVALID_BALL` identifica un balón nulo o una instancia ya agregada.

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

**Responsabilidad:** unificar detección de intersecciones y reposicionamiento de formas geométricas. Las entidades que colisionan contienen estas formas; solo las formas implementan `Hitbox`.

**Atributos:** ninguno.

**Métodos:** `intersects(target)` devuelve el contacto detectado; `reposition(x, y)` reposiciona la forma preservando tamaño. Para círculos `(x,y)` es el centro; para rectángulos, la esquina superior izquierda.

### `HitboxContact` (clase final)

**Responsabilidad:** transportar el nombre de la entidad contactada y la forma concreta implicada.

**Componentes:** `name: String`, nombre simple o cadena vacía; `hitbox: Hitbox`, forma implicada o `null` si no hubo contacto.

**Métodos:** constructor, accesores `name()` y `hitbox()`, `none()` devuelve la instancia compartida sin contacto; `isContact()` devuelve `true` si hay nombre y hitbox no nulos. `equals`, `hashCode` y `toString` comparan y representan los dos campos.

### `HitboxIntersections`

**Responsabilidad:** concentrar cálculos geométricos; detecta contactos, pero no aplica consecuencias físicas.

**Atributos:** ninguno; clase utilitaria no instanciable.

**Métodos:** `HitboxIntersections()` es privado; `intersects(first, second)` resuelve entidades y formas; `shapeOf(subject)` extrae la forma; `shapesIntersect(first, second)` selecciona el cálculo por par de formas; `contactFor(target)` construye la respuesta para la entidad objetivo; `circlesIntersect(first, second)` compara distancia entre centros con suma de radios; `rectanglesIntersect(first, second)` compara intervalos normalizados; `circleIntersectsRectangle(circle, rectangle)` compara el círculo con el punto más cercano del rectángulo.

### `CircularHitbox`

**Responsabilidad:** almacenar y representar una forma circular.

**Atributos:** `x`, `y: double` centro; `radius: double` radio.

**Métodos:** `CircularHitbox(x, y, radius)` inicializa la forma; `intersects(target)` delega el cálculo; `reposition(x, y)` cambia el centro sin cambiar el radio; `getX()`, `getY()` y `getRadius()` consultan sus valores.

### `RectangularHitbox`

**Responsabilidad:** almacenar y representar una forma rectangular.

**Atributos:** `x`, `y: double` esquina superior izquierda; `width`, `height: double` dimensiones.

**Métodos:** `RectangularHitbox(x, y, width, height)` inicializa la forma; `intersects(target)` delega el cálculo; `reposition(x, y)` cambia la esquina sin cambiar dimensiones; `getX()`, `getY()`, `getWidth()`, `getHeight()` consultan valores; `resize(width, height)` cambia dimensiones sin mover la esquina.

## Presentación (presentation)

La aplicación principal usa Swing y todavía no está conectada a una sesión del dominio.

- DOPOSoccerGUI crea la ventana, registra las pantallas en un CardLayout y configura el cierre.
- NavigationController cambia la pantalla visible y cierra la ventana.
- BasePanel es la clase abstracta con los contratos prepareView() y prepareController().
- MainMenuPanel, ConfigurationPanel, PersonalizationPanel y StoreImprovementPanel implementan interfaces locales. La configuración de teclado no se persiste; la tienda mantiene niveles y precios provisionales en memoria.
- PreparationPanel, MatchPanel y PausePanel son pantallas sin lógica de partida conectada.
- RoundedButton, RoundedPanel y Theme aportan componentes y estilos compartidos.

No hay clases App, GameGUI, GameController, Screen ni ScreenManager en el código actual. La presentación principal no importa tipos de domain; el banco visual independiente consume la fachada Game.

## Banco de pruebas visual (`presentation.testing`)

### `MechanicsTestApp`

**Responsabilidad:** iniciar la ventana independiente del banco de pruebas visual.

**Métodos:** el constructor privado evita instancias; `main(args)` crea y muestra `MechanicsTestPanel` en el hilo de eventos de Swing, configura el tamaño de ventana e inicia la actualización periódica.

### `MechanicsTestController`

**Responsabilidad:** adaptar las acciones de la pantalla a la fachada `Game`, sin acceder directamente a otras clases del dominio; coordina también el movimiento visual.

**Atributos:** `game: Game` es la única puerta al dominio.

**Métodos:** `MechanicsTestController()` crea o reutiliza una sesión estándar jugador contra jugador por goles; `game()` devuelve la fachada; `update()` avanza el movimiento visual y la sesión; `reset()` reinicia la ronda y los controles; `setMovementControl(control, pressed)` registra las teclas mantenidas; `kick(rightSide)` intenta una patada; `setBallState(x, y, velocityX, velocityY)` prepara posición y velocidad del balón; `prepareKick()` acerca a los participantes y al balón para observar la patada.

### `PresentationMovement`

**Responsabilidad:** repetir el movimiento horizontal mientras se mantiene una tecla.

**Controles:** `A/D` desplazan al participante izquierdo y las flechas horizontales al derecho en modo manual. El banco solicita saltos a `Game.jumpPlayer` con `W` a la izquierda y flecha arriba a la derecha en modo manual.

### `MechanicsTestPanel`

**Responsabilidad:** presentar la cancha y un menú configurable de pruebas y capas visuales.

**Elementos visibles:** usa recursos gráficos incluidos en el proyecto para cancha, balón y pares de cabeza/pata del lobo y el tigre; compone los personajes y los orienta según su lado. El menú de capas activa hitbox exterior de cancha, hitboxes de arcos, participantes y balones, vector del balón, animación de patada, marcador/tiempo y secciones de vida y poder. Estas dos últimas indican que sus datos no están disponibles: `Game` no expone vida y el poder todavía no está implementado.

**Métodos:**

| Método | Qué hace |
|---|---|
| `MechanicsTestPanel()` | Registra opciones iniciales, crea controles, enlaza teclas y prepara la sesión. |
| `addScenario(label, action)` | Agrega una acción al selector de escenarios, punto de extensión para nuevos tipos de prueba. |
| `start()` / `stop()` | Inicia o detiene el temporizador de actualización. |
| `registerDefaultScenarios()` | Registra partida libre, vector, patada y goles. |
| `registerDefaultLayers()` | Registra las capas iniciales que se muestran como casillas activables. |
| `runSelectedScenario()` | Ejecuta el escenario seleccionado y refresca el estado visual. |
| `bindKeys()` | Enlaza A/D, W, flechas, F y Enter con movimiento, saltos y patadas. |
| `tick()` | Avanza la sesión, actualiza la animación de la pata y solicita redibujo. |

**Clases internas:** `GameCanvas` escala la cancha según las dimensiones de `Game`, pinta recursos, hitboxes, vectores y HUD; `Layer` identifica las capas; `LayerOption` conserva su estado visible; `TestScenario` conserva la etiqueta y acción de cada opción del menú.

## Estado de las implementaciones


La persistencia guarda la sesión serializable. El banco muestra las hitboxes y el vector del balón, compone los sprites y refleja su posición física al caminar y saltar. La activación de poderes y parte de sus efectos están pendientes.
