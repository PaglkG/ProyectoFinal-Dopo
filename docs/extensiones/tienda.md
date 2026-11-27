# Guía de estudio para manejar y extender la tienda

La tienda tiene dos recorridos. Al abrirse, lee una captura y actualiza etiquetas. Al pulsar Mejorar, envía una solicitud a la ventana. La ventana deja que el dominio valide la compra y publica los valores resultantes.

```text
Lectura  → refreshFromState → refreshPlayer → etiquetas y botones
Compra   → botón Mejorar → improveStatistic → Game.improveStatistics
Resultado → publishMatch → refreshFromState → saldo actualizado
```

## 1. Sigue una fila de principio a fin

Abre [StoreImprovementPanel](../../src/main/java/presentation/StoreImprovementPanel.java). `STAT_NAMES` contiene los textos y `STAT_KEYS` las claves que entiende el dominio. El orden es velocidad, salto, patada y vida. En la primera fila, `Velocidad` se ve en pantalla y `speed` se envía al comprar.

1. `prepareViewPlayers()` crea las etiquetas de puntos y los arreglos de niveles, precios y botones para las dos columnas.
2. `createPlayerColumn()` recorre los índices. En cada posición coloca una etiqueta de nivel, una de precio y un botón.
3. `prepareControllerUpgrades()` toma la clave del mismo índice y registra el clic. El callback recibe la **identidad humana de la columna** y la **clave de la fila**. El callback de la ventana se pasa al construir la tienda en `DOPOSoccerGUI.prepareViewPanels()`.

## 2. Lee el estado y elige la etiqueta que debes actualizar

1. En `DOPOSoccerGUI.showScreen()`, abrir la tienda llama a `refreshFromState()`. Ese método obtiene una captura y la usa para las dos columnas.
2. En `refreshPlayer(playerOne, match)`, sigue el primer `if`. Selecciona las etiquetas del jugador 1 o del jugador 2.
3. Antes de una partida se muestran los valores iniciales. Si hay captura, `match.getHumanPlayer(playerOne)` resuelve la identidad. En una partida contra bot puede faltar una identidad humana. En ese caso la columna usa el indicador de dato no disponible.
4. El saldo disponible sale de `humanPlayer.getPoints()` y llega a `pointsLabel.setText(pointsText)`. El bucle siguiente decide cómo presentar el nivel y el precio y cuándo habilita cada botón.

| Dato | Consulta actual | Lugar de actualización |
| --- | --- | --- |
| Puntos | `humanPlayer.getPoints()` | `pointsLabel.setText(pointsText)` |
| Nivel de velocidad | `humanPlayer.getSpeedLevel()` | `displayedLevel()` y luego `buildLevelBar()` |
| Nivel de vida | Se obtiene a partir de `getMaxLife()` | `displayedLevel()` |
| Niveles de salto y patada | Todavía faltan en la captura | La fila muestra el indicador de dato ausente durante una partida |
| Precio real de compra | Todavía falta en la captura | Antes de jugar se muestra el inicial. Después se muestra el indicador de dato ausente o el nivel máximo |

## 3. Practica un cambio pequeño: agregar la recarga bajo los puntos

1. Declara `player1PowerLabel` y `player2PowerLabel` junto a los campos de puntos. Inicialízalas en `prepareViewPlayers()`.
2. Recibe una etiqueta adicional en `createPlayerColumn()`. Actualiza sus dos llamadas para pasar a cada columna la etiqueta que le corresponde.
3. Agrega la etiqueta justo debajo de `column.add(pointsLabel)`.
4. En `refreshPlayer()`, después de actualizar el saldo, lee el dato del humano que ya resolvió el método:

```java
JLabel powerLabel = playerOne ? player1PowerLabel : player2PowerLabel;
powerLabel.setText(humanPlayer == null
        ? "Recarga: " + UNAVAILABLE
        : String.format("Recarga: %.1f s", humanPlayer.getPowerRemainingSeconds()));
```

El tiempo sale de la última captura. La tienda se refresca al entrar o después de una compra, mientras que el reloj de la cancha se detiene al dejar la pantalla del partido.

## 4. Sigue la compra hasta el dominio

1. En [DOPOSoccerGUI](../../src/main/java/presentation/DOPOSoccerGUI.java), entra a `improveStatistic()`. Revisa la guarda que comprueba captura, partida terminada e identidad humana.
2. La ventana convierte la identidad humana en el lado físico para `Game.improveStatistics()`. En manual el jugador 1 es el lado derecho. Contra bot, el humano compra para el lado izquierdo. Esta operación recibe lado aunque el botón use identidad.
3. Sigue `GameSession.improveStatistics()` → `SessionParticipant.improveStatistics()` → `Stats.improveStatistics()`. Ahí se comprueban saldo y nivel, se descuentan puntos y se aplica el nuevo valor. El precio comienza en 500 y se duplica tras cada compra del jugador. La mejora de vida también ajusta su vida actual y máxima.
4. Al completar la compra, la ventana llama primero a `publishMatch()` y después a `storeImprovementPanel.refreshFromState()`. Si la compra se rechaza, se conserva el saldo y la ventana muestra el mensaje de error. El botón de presentación solo solicita la compra.

## 5. Publica otro valor si la tienda lo necesita

Para mostrar el precio real o los niveles de salto y patada, sigue el recorrido de `getPlayerPoints()` como referencia. Primero confirma el dato y su contrato. Si la fachada aún carece de él, su ampliación requiere autorización según las reglas del repositorio. Una vez disponible, incorpora el valor a `MatchSnapshot.Participant`, captúralo en `DOPOSoccerGUI.readParticipant()` y úsalo en `displayedLevel()` o en la actualización de precios. Conserva en el dominio el cálculo del saldo, los costos y las reglas de compra.

## 6. Comprueba los casos del ejercicio

1. Antes de iniciar una partida, abre la tienda. Debe verse el estado inicial y los botones de compra deben estar deshabilitados.
2. Con una partida activa, comprueba que la tienda no permita comprar.
3. Con una partida terminada, verifica que se vea el saldo de cada humano y que el bot no ocupe una columna de compra.
4. Con una sesión de dominio preparada con saldo suficiente, comprueba que una compra actualiza el saldo y el nivel. Prueba también saldo insuficiente y nivel máximo.
5. Si agregaste la recarga, comprueba que el texto sale de la misma captura que el saldo y que una identidad ausente muestra el indicador previsto.
