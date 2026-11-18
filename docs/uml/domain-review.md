# Contraste de UML y dominio

## Alcance

Se contrastaron el diagrama de clases y las secuencias entregadas en los PDF de `capturas/` con `src/main/java/domain/` y `docs/logica-proyecto.md`. El resultado describe el estado observado del código; no convierte las notas provisionales del PDF en reglas confirmadas.

## Resumen

- Implementado de forma coherente: construcción básica de `Game`; creación reflectiva de modalidades y sesiones; construcción de `PlayerVsBot` y `PlayerVsPlayer`; colocación fija de participantes; construcción y reinicio del balón; construcción y reposicionamiento de hitboxes; comprobaciones `ByGoals` y `ByTime`; valores iniciales y mejoras de `Stats`.
- Implementado: `Game.closeSession` elimina de `sessions` el objeto de sesión recibido.
- Implementado en esta revisión: actualización fija del balón, rebotes contra límites, otros balones y participantes, aterrizaje visual encima del balón, excepciones por salida/superposición, goles y marcador, reinicio de ronda y cierre por modalidad.
- Implementado en esta revisión: movimiento horizontal delegado por `Game`, escalado por `Stats.speed` y aplicado al participante y su hitbox.
- Implementado en esta revisión: 5 puntos por victoria humana, costo de 5 puntos por mejora, conservación por ID entre sesiones de una instancia de `Game`, daño de patada, aturdimiento con `StatusEffect` y errores de dominio emitidos como `GameException`.
- Sigue pendiente: salto/poder, persistencia, decisiones de bots y ejecución de disparos/efectos de los poderes.
- El UML tiene firmas, clases y mensajes que no coinciden con el código o con reglas ya confirmadas. Ver detalles abajo.

## Hallazgos por clase

| Clase / grupo | Código observado | Evaluación frente al UML y reglas |
|---|---|---|
| `Game` | `startSession` crea una sesión por reflexión con modalidad, reglas, poderes y perfil; la añade a `sessions`; `new Game()` inicializa la lista. | El flujo implementado pasa la lista de poderes al constructor de la sesión y conserva la traducción reflectiva de errores a `GameException`. |
| `Game.closeSession` | Ejecuta `sessions.remove(closeSession)`. | Retira de la lista la sesión recibida; si no está presente, no altera la lista. |
| `Game.movePlayer` | Delega en la sesión para participantes registrados en una partida activa; el participante se desplaza 150 × `speed` / 60 unidades, en la dirección confirmada. | Implementado en esta revisión conforme a dirección y fórmula confirmadas. |
| `Game.jumpPlayer`, `powerPlayer`, `resetSession`, `save`, `open` | Cuerpos vacíos. | Pendiente. Las secuencias de salto, poder y reinicio describen llamadas objetivo que todavía no ocurren. Las páginas de `open` y `save` están vacías. `ImproveStatistics` ya delega en la sesión. |
| `GameSession` | Constructor crea listas, modalidad reflectiva y `SoccerField`; fija tiempo transcurrido a cero y usa gravedad 40. Actualiza en pasos de 1/60 s, empuja balones quietos al caminar y delega a la cancha el contacto con balones en movimiento. | La secuencia de actualización incluye rebotes entre balones y participantes; al aterrizar sobre un balón, el banco mantiene al jugador encima y mueve el balón hacia abajo. Siguen pendientes `minParticipants`, `assignRules` y poder. |
| `PlayerVsBot` | Crea el jugador humano a la derecha y el perfil bot solicitado a la izquierda; asocia los dos poderes elegidos. | Coincide con el orden confirmado de participantes, poderes y dificultad. Los tipos de bot se crean reflectivamente y sus decisiones siguen pendientes. |
| `PlayerVsPlayer` | Crea jugador 1 a la derecha y jugador 2 a la izquierda; asocia el poder de cada lado según la lista recibida. | Coincide con posiciones fijas y orden confirmado de parámetros. |
| `SoccerField` | Crea cancha provisional de 450×150, cuatro límites, dos arcos y balones; proporciona consultas para gol, salida, suelo y ubicación simétrica de participantes; resuelve colisiones entre balones y contra participantes, y mantiene los balones dentro de los límites. | La intersección devuelve la entidad contactada y su hitbox. `reposition` traslada también los balones; `resetBall(x,y)` centra y detiene. Al contacto desde arriba el balón adquiere movimiento descendente mínimo. |
| `Ball` | Construye hitbox circular, reinicia al centro, expone posición/velocidad, suma impulsos, avanza por paso temporal y rebota con retención configurada por la sesión. | Física usada por `GameSession`; `physical` todavía no se utiliza. |
| `Hitbox`, `CircularHitbox`, `RectangularHitbox` | Interfaz y formas implementan `intersects` y `reposition`; las formas reposicionan sus referencias. | `intersects` devuelve `HitboxContact` con entidad/hitbox, o nombre vacío y hitbox nula sin contacto. `reposition` funciona según el punto de referencia documentado. El PDF de Hitbox no muestra `reposition`, aunque está en la interfaz y ambas formas. |
| `SessionParticipant` | Tiene posición, hitbox rectangular, estadísticas, estados, vida y poder; `move` modifica la coordenada horizontal según `speed`, y `resetPosition`/`reposition` actualizan posición e hitbox. | Movimiento y mejoras implementados; el daño reduce vida hasta cero, aplica `StatusEffect` por 3 segundos y restaura vida máxima al expirar. `jump` y `power` siguen pendientes. |
| `Player`, `BotPlayer`, perfiles | `Player` conserva un id. `BotPlayer.decideAction` devuelve `null`; `Clumsy`, `Offensive`, `Defensive`, `Expert` no agregan conducta. | Identidad/jerarquía disponibles; altas de usuarios y decisiones de bot pendientes. |
| `Stats` | Mapa `speed`, `goal`, `kick`, `jump`, cada uno en 1; `points` inicia en 0. Entrega 5 puntos por victoria y cobra 5 por nivel de mejora. | Mejora una habilidad en 1 solo si es válida y hay saldo; estos errores y los parámetros inválidos de reglas se lanzan como `GameException`. El UML PDF dibuja `action` como `HashSet`, lo cual contradice el código y la decisión documentada (`Map<String,Integer>`). |
| `Power`, `AuraAzul`, `Rayo`, `Tornado`, `Ilusion`, `PowerFactory` | `Power` es abstracta; cada tipo concreto se puede crear y asociar al participante mediante nombre de clase completo. | La selección/asociación está implementada. Lanzamiento, daño, carga y efectos de estado siguen pendientes. |
| `ByGoals` | Parsea el límite y termina cuando un marcador es exactamente igual al límite. | Coincide con la regla confirmada. `check` recibe una lista de marcadores en Java; el diagrama de clases lo presenta sin parámetros. |
| `ByTime` | Parsea minutos enteros; continúa antes del límite, activa muerte súbita en empate al límite y termina si hay diferencia. | Coincide con las reglas confirmadas de continuidad/muerte súbita. `check(List<String>)` y sus helpers están en `ByTime`; el UML ubica `isTime` e `isSuddenDeath` bajo `ByGoals`, contradiciendo la decisión documentada. |

## Diferencias concretas del UML

1. `TypeOfMatch.check` aparece sin parámetros en el PDF; Java declara `check(List<String>)`. `ByGoals.check` y `ByTime.check` también necesitan la lista según el contrato actual.
2. El PDF coloca `isTime` e `isSuddenDeath` en `ByGoals`; el código y la regla documentada los ubican en `ByTime`.
3. `Stats.action` aparece como `HashSet` en el PDF; el código/regla acordada definen `Map<String,Integer>`.
4. La secuencia `closeSession` y la implementación eliminan la sesión solicitada.
5. La secuencia `resetSession` usa cuatro coordenadas y bases distintas por lado; Java expone `SoccerField.resetBall(x,y)` y `GameSession` solo declara `xBase`/`yBase` comunes.
6. Las secuencias `startSessionPlayerVsBot`/`VsPlayer` tienen inconsistencias de nombres/tipos: llamadas a modalidad con nombres de parámetros intercambiados, creación o retorno rotulado como la modalidad equivocada, y creación de `Scoreboard` aunque esa clase fue retirada del dominio actual. En la secuencia PVP, la sesión aparece rotulada `PlayerVsBot`.
7. La interfaz y formas del diagrama de Hitbox omiten `reposition(x,y)`, que sí existe en Java.
8. La secuencia `movePlayer` fija `true = derecha`, `false = izquierda`; la dirección y fórmula de desplazamiento se confirmaron e implementaron.
9. Las secuencias `movePlayer` y `updateGame` describen la implementación actual: al caminar el participante empuja el balón en pasos pequeños; la cancha resuelve los choques entre balones y participantes, y se conservan el avance fijo, los rebotes, la prioridad del gol frente a salida y el cierre por modalidad.

## Pendientes de codificación derivados de los diagramas

1. Mantener `Game.closeSession` alineado con el flujo que elimina el objeto de sesión recibido.
2. Implementar `Game.resetSession`; aclarar y uniformar las coordenadas de reinicio por lado antes de codificar el contrato divergente del PDF.
3. Extender el movimiento si se confirman límites de cancha, colisiones u otras restricciones adicionales.
4. Implementar salto y poder a través de `Game`, `GameSession`, participante y `Power` una vez definidos sus efectos y límites.
5. Completar estadísticas, estados temporales y ejecución de disparos/efectos de poderes, además de las decisiones de perfiles de bot.
6. Implementar `save`/`open`; sus páginas de secuencia no muestran interacciones que permitan derivar el formato o el contrato.
7. Implementar `minParticipants`, `assignRules`, `addParticipant(s)` y otras operaciones declaradas si se mantienen dentro del alcance del producto.

## Páginas sin flujo utilizable

En `todo.pdf`, las páginas tituladas `Cicle1`, `Cicle2`, `open` y `save` no muestran interacciones suficientes para transcribir un flujo. `session1.pdf` solo muestra un marco vacío `Cicle1`. `game.pdf` contiene la secuencia de construcción de `Game`; está incluida en `domain-sequences.puml`.
