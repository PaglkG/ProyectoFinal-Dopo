# Contraste de UML y dominio

## Alcance

Se contrastaron el diagrama de clases y las secuencias entregadas en los PDF de `capturas/` con `src/main/java/domain/` y `docs/logica-proyecto.md`. El resultado describe el estado observado del código; no convierte las notas provisionales del PDF en reglas confirmadas.

## Resumen

- Implementado de forma coherente: instancia compartida de `GameSession` para `PlayerVsBot` y `PlayerVsPlayer`; creación reflectiva de la modalidad; colocación fija de participantes; construcción y reinicio del balón; construcción y reposicionamiento de hitboxes; comprobaciones `ByGoals` y `ByTime`; valores iniciales y mejoras de `Stats`.
- Implementado: `Game.closeSession` elimina de `sessions` el objeto de sesión recibido.
- Implementado en esta revisión: actualización fija del balón, rebotes contra límites, otros balones y participantes, aterrizaje visual encima del balón, excepciones por salida/superposición, goles y marcador, reinicio de ronda y cierre por modalidad.
- Implementado en esta revisión: movimiento horizontal delegado por `Game`, escalado por `Stats.speed` y aplicado al participante y su hitbox.
- Implementado en esta revisión: 5 puntos por victoria humana, costo de 5 puntos por mejora, conservación por ID entre sesiones de una instancia de `Game`, daño de patada, aturdimiento con `StatusEffect` y errores de dominio emitidos como `GameException`.
- Sigue pendiente: salto/poder, persistencia, decisiones de bots y ejecución de disparos/efectos de los poderes.
- El UML tiene firmas, clases y mensajes que no coinciden con el código o con reglas ya confirmadas. Ver detalles abajo.

## Hallazgos por clase

| Clase / grupo | Código observado | Evaluación frente al UML y reglas |
|---|---|---|
| `Game` | `startSession` solicita `GameSession.getInstance`; registra la referencia solo una vez. | Las llamadas posteriores reutilizan la primera sesión, aun si solicitan otra subclase; la primera modalidad concreta permanece activa. |
| `Game.closeSession` | Ejecuta `sessions.remove(closeSession)`. | Retira la referencia de esta lista; no destruye la instancia singleton, que puede volver a registrarse. |
| `Game.movePlayer` | Delega en la sesión para participantes registrados en una partida activa; el participante se desplaza 150 × `speed` / 60 unidades, en la dirección confirmada. | Implementado en esta revisión conforme a dirección y fórmula confirmadas. |
| `Game.resetSession` | Delega `restartRound` en la sesión única administrada; también permite resolverla por índice. | Reinicia balones y posiciones sin reemplazar la instancia ni cambiar el marcador, tiempo o estado final. `powerPlayer`, `save` y `open` siguen vacíos. |
powerPlayer sigue vacío; minParticipants y assignRules ya no existen en el código actual.
| `PlayerVsBot` | Crea el jugador humano a la derecha y el perfil bot solicitado a la izquierda; asocia los dos poderes elegidos. | Coincide con el orden confirmado de participantes, poderes y dificultad. Los tipos de bot se crean reflectivamente y sus decisiones siguen pendientes. |
| `PlayerVsPlayer` | Crea jugador 1 a la derecha y jugador 2 a la izquierda; asocia el poder de cada lado según la lista recibida. | Coincide con posiciones fijas y orden confirmado de parámetros. |
| `SoccerField` | Crea cancha provisional de 450×150, cuatro límites, dos arcos y balones; proporciona consultas de geometría y ubicación simétrica de participantes; delega las colisiones a `CollisionFacade`. | La intersección devuelve la entidad contactada y su hitbox. `reposition` traslada también los balones; `resetBall(x,y)` centra y detiene. Al contacto desde arriba el balón adquiere movimiento descendente mínimo. |
| `CollisionFacade`, `CourtGeometry`, `BallCollisionResolver`, `BallParticipantCollisionResolver` | La fachada coordina respuestas físicas; la geometría resuelve contactos contra cancha y arcos; los resolvers se ocupan de balón-balón, empuje al caminar y balón-participante. | La cancha es propietaria de la fachada. `GameSession` conserva el orden de la ronda y no depende directamente de los colaboradores de colisión. |
| `Ball` | Construye hitbox circular, reinicia al centro, expone posición/velocidad, suma impulsos, avanza por paso temporal y rebota con retención configurada por la sesión. | Física usada por `GameSession`; `physical` todavía no se utiliza. |
| `Hitbox`, `CircularHitbox`, `RectangularHitbox` | Interfaz y formas implementan `intersects` y `reposition`; las formas reposicionan sus referencias. | `intersects` devuelve `HitboxContact` con entidad/hitbox, o nombre vacío y hitbox nula sin contacto. `reposition` funciona según el punto de referencia documentado. El PDF de Hitbox no muestra `reposition`, aunque está en la interfaz y ambas formas. |
| `SessionParticipant` | Tiene posición, hitbox rectangular, estadísticas, estados, vida y poder; `move` modifica la coordenada horizontal según `speed`, y `resetPosition`/`reposition` actualizan posición e hitbox. | Movimiento y mejoras implementados; el daño reduce vida hasta cero, aplica `StatusEffect` por 3 segundos y restaura vida máxima al expirar. `jump` y `power` siguen pendientes. |
| `Player`, `BotPlayer`, perfiles | `Player` conserva un id. `BotPlayer.decideAction` devuelve `null`; `Clumsy`, `Offensive`, `Defensive`, `Expert` no agregan conducta. | Identidad/jerarquía disponibles; altas de usuarios y decisiones de bot pendientes. |
| `Stats` | Mapa `speed`, `goal`, `kick`, `jump`, cada uno en 1; `points` inicia en 0. Entrega 5 puntos por victoria y cobra 5 por nivel de mejora. | Mejora una habilidad en 1 solo si es válida y hay saldo; estos errores y los parámetros inválidos de reglas se lanzan como `GameException`. El UML PDF dibuja `action` como `HashSet`, lo cual contradice el código y la decisión documentada (`Map<String,Integer>`). |
| `Power`, `BlueAura`, `Lightning`, `Tornado`, `Illusion`, `PowerFactory` | `Power` es abstracta; cada tipo concreto se puede crear y asociar al participante mediante nombre de clase completo. | La selección/asociación está implementada. Lanzamiento, daño, carga y efectos de estado siguen pendientes. |
| `ByGoals` | Parsea el límite y termina cuando un marcador es exactamente igual al límite. | Coincide con la regla confirmada. `check` recibe una lista de marcadores en Java; el diagrama de clases lo presenta sin parámetros. |
| `ByTime` | Parsea minutos enteros; continúa antes del límite, activa muerte súbita en empate al límite y termina si hay diferencia. | Coincide con las reglas confirmadas de continuidad/muerte súbita. `check(List<String>)` y sus helpers están en `ByTime`; el UML ubica `isTime` e `isSuddenDeath` bajo `ByGoals`, contradiciendo la decisión documentada. |

## Diferencias concretas del UML

1. `TypeOfMatch.check` aparece sin parámetros en el PDF; Java declara `check(List<String>)`. `ByGoals.check` y `ByTime.check` también necesitan la lista según el contrato actual.
2. El PDF coloca `isTime` e `isSuddenDeath` en `ByGoals`; el código y la regla documentada los ubican en `ByTime`.
3. `Stats.action` aparece como `HashSet` en el PDF; el código/regla acordada definen `Map<String,Integer>`.
4. La secuencia `closeSession` y la implementación eliminan la sesión solicitada.
5. La secuencia resetSession del PDF supone cuatro coordenadas y bases por lado; esos campos no existen en GameSession actual. El reinicio confirmado centra el balón y reposiciona participantes mediante `restartRound`.
6. Las secuencias `startSessionPlayerVsBot`/`VsPlayer` tienen inconsistencias de nombres/tipos: llamadas a modalidad con nombres de parámetros intercambiados, creación o retorno rotulado como la modalidad equivocada, y creación de `Scoreboard` aunque esa clase fue retirada del dominio actual. En la secuencia PVP, la sesión aparece rotulada `PlayerVsBot`.
7. La interfaz y formas del diagrama de Hitbox omiten `reposition(x,y)`, que sí existe en Java.
8. La secuencia `movePlayer` fija `true = derecha`, `false = izquierda`; la dirección y fórmula de desplazamiento se confirmaron e implementaron.
9. Las secuencias `movePlayer` y `updateGame` describen la implementación actual: al caminar el participante empuja el balón en pasos pequeños; la cancha resuelve los choques entre balones y participantes, y se conservan el avance fijo, los rebotes, la prioridad del gol frente a salida y el cierre por modalidad.

## Pendientes de codificación derivados de los diagramas

1. Mantener `Game.closeSession` alineado con el flujo que elimina el objeto de sesión recibido.
2. La secuencia resetSession del PDF continúa pendiente de actualización para coincidir con el reinicio confirmado de la ronda.
3. Extender el movimiento si se confirman límites de cancha, colisiones u otras restricciones adicionales.
4. Implementar salto y poder a través de `Game`, `GameSession`, participante y `Power` una vez definidos sus efectos y límites.
5. Completar estadísticas, estados temporales y ejecución de disparos/efectos de poderes, además de las decisiones de perfiles de bot.
6. Implementar `save`/`open`; sus páginas de secuencia no muestran interacciones que permitan derivar el formato o el contrato.
7. Definir si se agregará un flujo de registro de participantes; actualmente las modalidades construyen automáticamente sus participantes.

## Páginas sin flujo utilizable

En `todo.pdf`, las páginas tituladas `Cicle1`, `Cicle2`, `open` y `save` no muestran interacciones suficientes para transcribir un flujo. `session1.pdf` solo muestra un marco vacío `Cicle1`. `game.pdf` contiene la secuencia de construcción de `Game`; está incluida en `domain-sequences.puml`.
