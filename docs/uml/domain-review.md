# Contraste de UML y dominio

## Alcance

Se contrastaron el diagrama de clases y las secuencias entregadas en los PDF de `capturas/` con `src/main/java/domain/` y `docs/logica-proyecto.md`. El resultado describe el estado observado del código; no convierte las notas provisionales del PDF en reglas confirmadas.

## Resumen

- Implementado de forma coherente: construcción básica de `Game`; creación reflectiva de modalidades y sesiones; construcción de `PlayerVsBot` y `PlayerVsPlayer`; colocación fija de participantes; construcción y reinicio del balón; construcción y reposicionamiento de hitboxes; comprobaciones `ByGoals` y `ByTime`; valores iniciales confirmados de `Stats`.
- Implementado con defecto: `Game.closeSession` ignora el objeto recibido y ejecuta `sessions.remove(0)`.
- Implementado en esta revisión: actualización fija del balón, resolución de contactos confirmados, excepciones por salida/superposición, goles y marcador, reinicio de ronda y cierre por modalidad.
- Sigue pendiente: entrada de movimiento/salto/poder, mejoras de estadísticas, persistencia, decisiones de bots, efectos de estado y poderes.
- El UML tiene firmas, clases y mensajes que no coinciden con el código o con reglas ya confirmadas. Ver detalles abajo.

## Hallazgos por clase

| Clase / grupo | Código observado | Evaluación frente al UML y reglas |
|---|---|---|
| `Game` | `startSession` crea una sesión por reflexión y la añade a `sessions`; `new Game()` inicializa la lista. | Flujo base implementado. La secuencia PDF de creación escribe la firma con parámetros en otro orden que la firma Java (`parametersRules, typeGame, typeMatche, player`). |
| `Game.closeSession` | Ejecuta `sessions.remove(0)`. | Defecto: no retira necesariamente `closeSession`; puede quitar una sesión distinta y falla si la lista está vacía. El PDF representa la eliminación de la sesión recibida. |
| `Game.movePlayer`, `jumpPlayer`, `powerPlayer`, `resetSession`, `ImproveStatistics`, `updateGame`, `save`, `open` | Cuerpos vacíos. | Pendiente. Las secuencias de movimiento, salto, poder, reinicio, mejora y actualización describen llamadas objetivo que todavía no ocurren. Las páginas de `open` y `save` están vacías. |
| `GameSession` | Constructor crea listas, modalidad reflectiva y `SoccerField`; fija tiempo transcurrido a cero y usa gravedad 40. Actualiza en pasos de 1/60 s, mantiene goles, resuelve rebotes, separa el balón del jugador y lo impulsa hacia fuera, además de las excepciones y el cierre de modalidad. | La secuencia de actualización está implementada conforme a reglas confirmadas. Siguen pendientes `minParticipants`, `assignRules`, movimiento, salto, poder y mejoras. |
| `PlayerVsBot` | Crea `Player(1)` o `Player(2)` a la derecha según el booleano y siempre `BotPlayer` a la izquierda. | Coincide con regla confirmada. `addParticipant` no hace nada. El diagrama de secuencia completo contiene llamadas/responsabilidades que no están en el constructor actual. |
| `PlayerVsPlayer` | Crea jugador 1 a la derecha y jugador 2 a la izquierda; ignora el booleano. | Coincide con regla confirmada. `addParticipants` no hace nada. La secuencia PDF de esta modalidad rotula la sesión como `PlayerVsBot` y muestra la adición de identidades que no corresponden claramente al flujo implementado. |
| `SoccerField` | Crea cancha provisional de 450×150, cuatro límites, dos arcos y balones; proporciona consultas para gol, salida, suelo y ubicación simétrica de participantes. `hitboxs()` expone los elementos asociados. | La intersección devuelve la entidad contactada y su hitbox. `reposition` traslada también los balones; `resetBall(x,y)` centra y detiene. |
| `Ball` | Construye hitbox circular, reinicia al centro, expone posición/velocidad, suma impulsos, avanza por paso temporal y rebota con retención configurada por la sesión. | Física usada por `GameSession`; `physical` todavía no se utiliza. |
| `Hitbox`, `CircularHitbox`, `RectangularHitbox` | Interfaz y formas implementan `intersects` y `reposition`; las formas reposicionan sus referencias. | `intersects` devuelve `HitboxContact` con entidad/hitbox, o nombre vacío y hitbox nula sin contacto. `reposition` funciona según el punto de referencia documentado. El PDF de Hitbox no muestra `reposition`, aunque está en la interfaz y ambas formas. |
| `SessionParticipant` | Tiene posición, hitbox rectangular, estadísticas, estados y poder; `resetPosition` y `reposition` actualizan posición/hitbox. | Reinicio básico de posición e identificación del objeto/hitbox contactados implementados. `move`, `jump`, `power` y mejora siguen pendientes. |
| `Player`, `BotPlayer`, perfiles | `Player` conserva un id. `BotPlayer.decideAction` devuelve `null`; `Clumsy`, `Offensive`, `Defensive`, `Expert` no agregan conducta. | Identidad/jerarquía disponibles; altas de usuarios y decisiones de bot pendientes. |
| `Stats` | Mapa `speed`, `goal`, `kick`, `jump`, cada uno en 1; `points` inicia en 0. | Valores iniciales coinciden con regla confirmada. Los métodos de mejora están vacíos. El UML PDF dibuja `action` como `HashSet`, lo cual contradice el código y la decisión documentada (`Map<String,Integer>`). |
| `Power`, `StatusEffect` | Métodos de uso/actualización vacíos; consultas de efecto devuelven `false`/`0.0`. | Esqueleto sin comportamiento funcional. El diagrama de secuencia condiciona el poder a `isUsable`, pero el método no implementa esa condición. |
| `ByGoals` | Parsea el límite y termina cuando un marcador es exactamente igual al límite. | Coincide con la regla confirmada. `check` recibe una lista de marcadores en Java; el diagrama de clases lo presenta sin parámetros. |
| `ByTime` | Parsea minutos enteros; continúa antes del límite, activa muerte súbita en empate al límite y termina si hay diferencia. | Coincide con las reglas confirmadas de continuidad/muerte súbita. `check(List<String>)` y sus helpers están en `ByTime`; el UML ubica `isTime` e `isSuddenDeath` bajo `ByGoals`, contradiciendo la decisión documentada. |

## Diferencias concretas del UML

1. `TypeOfMatch.check` aparece sin parámetros en el PDF; Java declara `check(List<String>)`. `ByGoals.check` y `ByTime.check` también necesitan la lista según el contrato actual.
2. El PDF coloca `isTime` e `isSuddenDeath` en `ByGoals`; el código y la regla documentada los ubican en `ByTime`.
3. `Stats.action` aparece como `HashSet` en el PDF; el código/regla acordada definen `Map<String,Integer>`.
4. La secuencia `closeSession` supone que se borra la sesión solicitada; Java elimina el índice cero.
5. La secuencia `resetSession` usa cuatro coordenadas y bases distintas por lado; Java expone `SoccerField.resetBall(x,y)` y `GameSession` solo declara `xBase`/`yBase` comunes.
6. Las secuencias `startSessionPlayerVsBot`/`VsPlayer` tienen inconsistencias de nombres/tipos: llamadas a modalidad con nombres de parámetros intercambiados, creación o retorno rotulado como la modalidad equivocada, y creación de `Scoreboard` aunque esa clase fue retirada del dominio actual. En la secuencia PVP, la sesión aparece rotulada `PlayerVsBot`.
7. La interfaz y formas del diagrama de Hitbox omiten `reposition(x,y)`, que sí existe en Java.
8. La secuencia `movePlayer` fija `true = derecha`, `false = izquierda`. Esa correspondencia aparece como nota de diagrama, pero no está confirmada en `logica-proyecto.md` y los métodos Java están vacíos.
9. La secuencia de `updateGame` se actualizó junto con su implementación: documenta avance fijo, validaciones de participantes, prioridad del gol sobre la salida del balón, rebote, impulso y cierre por modalidad.

## Pendientes de codificación derivados de los diagramas

1. Corregir `Game.closeSession` para retirar el objeto recibido, una vez que ese contrato se mantenga confirmado.
2. Implementar `Game.resetSession`; aclarar y uniformar las coordenadas de reinicio por lado antes de codificar el contrato divergente del PDF.
3. Implementar `Game.movePlayer` → `GameSession.movePlayer` → `SessionParticipant.move`; confirmar antes el significado de `direction`.
4. Implementar salto y poder a través de `Game`, `GameSession`, participante y `Power` una vez definidos sus efectos y límites.
5. Completar estadísticas, estados temporales, poderes y decisiones de perfiles de bot según reglas aún pendientes.
6. Implementar `save`/`open`; sus páginas de secuencia no muestran interacciones que permitan derivar el formato o el contrato.
7. Implementar `minParticipants`, `assignRules`, `addParticipant(s)` y otras operaciones declaradas si se mantienen dentro del alcance del producto.

## Páginas sin flujo utilizable

En `todo.pdf`, las páginas tituladas `Cicle1`, `Cicle2`, `open` y `save` no muestran interacciones suficientes para transcribir un flujo. `session1.pdf` solo muestra un marco vacío `Cicle1`. `game.pdf` contiene la secuencia de construcción de `Game`; está incluida en `domain-sequences.puml`.
