# Guía de estudio para conectar el movimiento en presentación

La cancha oficial ya muestra las posiciones que publica el juego. El banco de pruebas ya recibe las teclas y mueve a los participantes. El ejercicio consiste en llevar ese flujo de entrada a la pantalla oficial, que hoy conecta el botón de Pausa. La presentación captura las teclas y solicita acciones. El dominio calcula el desplazamiento, el salto y los contactos.

## 1. Sigue un ejemplo que ya funciona

Abre [MechanicsTestPanel](../../src/main/java/presentation/testing/MechanicsTestPanel.java) y sigue `bindKeys()` → `bindControl()` → `bindInput()`. En `bindInput()`, el `InputMap` relaciona cada tecla con un nombre de acción y el `ActionMap` indica qué código se ejecuta. Swing registra dos eventos asociados a la misma tecla, presión y liberación.

Después abre [MechanicsTestController](../../src/main/java/presentation/testing/MechanicsTestController.java). `setMovementControl()` recibe lado, dirección y estado de la tecla. [PresentationMovement](../../src/main/java/presentation/testing/PresentationMovement.java) conserva el estado y `update()` lo consulta antes de llamar a `Game.movePlayer()`. Esa separación explica por qué mantener una tecla genera pasos regulares.

## 2. Decide dónde va cada parte en la ventana oficial

| Clase | Qué editar | Qué conserva |
| --- | --- | --- |
| [MatchPanel](../../src/main/java/presentation/MatchPanel.java) | Registra las teclas en `prepareController()`, junto al registro de Pausa. | Componentes visuales y asociaciones de teclas. |
| [DOPOSoccerGUI](../../src/main/java/presentation/DOPOSoccerGUI.java) | Conecta el panel a una acción de entrada al construirlo en `prepareViewPanels()`. Aplica entradas en `updateGame()`. | Referencia a `Game`, temporizador y publicación de capturas. |
| Control de movimiento en presentación | Guarda cuatro booleanos, izquierda y derecha para los dos lados. Decide el paso a partir de esos estados. Puedes usar el banco como modelo. | Estado temporal de las teclas, que se limpia al salir o perder el foco. |
| `Game` | Llama a sus operaciones existentes. | Movimiento, salto, patada y validaciones del dominio. |

`MatchPanel` recibe acciones o callbacks desde la ventana. Al modificar su constructor, actualiza la llamada en `prepareViewPanels(). Un callback encapsula lo que el panel debe pedir cuando Swing recibe un evento.

## 3. Sigue la W desde la configuración hasta el salto

### Lo que ocurre hoy al pulsar W en Configuración

1. Abre `ConfigurationPanel`. Las filas vienen de `ACTION_NAMES`. «Salto» es la tercera fila, con índice `2`. En esa misma posición, `PLAYER1_DEFAULT_KEYS[2]` contiene `KeyEvent.VK_W` y `PLAYER2_DEFAULT_KEYS[2]` contiene `KeyEvent.VK_UP`.
2. `prepareViewKeyGrid()` llama a `createKeyButtons()`. Este método usa `KeyEvent.getKeyText(código)` para mostrar «W» en el botón. El texto es la etiqueta visible. El dato para registrar la tecla es el código entero.
3. Haz clic en el botón de Salto del Jugador 1. El listener de clic registrado en `prepareControllerKeyButton()` llama a `startKeyCapture()`. El botón guarda su texto anterior, entra en modo de captura y pide el foco.
4. Presiona físicamente **W**. El `KeyAdapter.keyPressed(KeyEvent e)` de ese botón llama a `captureKey(button, e)`. `e.getKeyCode()` devuelve `KeyEvent.VK_W`. Este evento llega al botón porque está esperando una tecla. Si presionas Escape, la captura se cancela.
5. `captureKey()` convierte el código a texto y se lo pasa a `assignKey()`. Este busca duplicados en las dos columnas. Si la tecla está libre, `button.setText(keyText)` muestra «W». Si ya pertenece a otro control, se restaura el texto anterior.
6. Hoy **Guardar** ejecuta `prepareControllerSave()` y vuelve al menú. La etiqueta editada no alimenta `MatchPanel`. En el banco, W sí salta porque `MechanicsTestPanel.bindKeys()` registra directamente `bindJump(KeyEvent.VK_W, "left-jump", false)`.

### Cómo usar la configuración existente en la cancha oficial

1. En `ConfigurationPanel`, crea dos arreglos editables que empiecen como copias de `PLAYER1_DEFAULT_KEYS` y `PLAYER2_DEFAULT_KEYS`. Conserva la correspondencia entre cada botón, su columna y la fila de `ACTION_NAMES`. La misma interfaz sirve para cambiar los controles.
2. En `captureKey()`, conserva `event.getKeyCode()` y pásalo a `assignKey()` junto con el botón. Usa `KeyEvent.getKeyText(keyCode)` para mostrar la etiqueta. Una vez que `assignKey()` valida el cambio, guarda el entero en la fila y columna del botón. Si el usuario cancela o la validación rechaza la tecla, conserva el entero anterior.
3. En `DOPOSoccerGUI.prepareViewPanels()`, pasa a `ConfigurationPanel` una acción que reciba las dos copias al pulsar Guardar. La ventana conserva los controles confirmados durante la ejecución, y antes de abrir la cancha los entrega a `MatchPanel`. Así los cambios de Configuración permanecen en la capa de presentación. El almacenamiento en disco queda fuera del ejercicio.
4. En `MatchPanel`, registra los controles recibidos mediante un método como `bindInput()` del banco. Usa aquí el código que llega de la fila Salto. Retira las asociaciones anteriores al volver a configurar para que deje de funcionar la tecla reemplazada.

```java
// Ejemplo: configurar el salto con el código confirmado del Jugador 1.
int jumpKey = player1KeyCodes[2];
bindInput(jumpKey, "player-1-jump", pressed -> {
    if (pressed && heldJumpKeys.add(jumpKey)) {
        onPlayerOneJump.run();
    }
    if (!pressed) {
        heldJumpKeys.remove(jumpKey);
    }
});
```

- `player1KeyCodes[2]` vale `KeyEvent.VK_W` inicialmente. Si el usuario seleccionó E y guardó, vale `KeyEvent.VK_E`. `heldJumpKeys` es un conjunto de enteros para descartar repeticiones mientras se mantiene la tecla.
- `bindInput()` asocia dos eventos en el `InputMap` con alcance `WHEN_IN_FOCUSED_WINDOW`. `KeyStroke.getKeyStroke(jumpKey, 0, false)` representa la presión y con `true` representa la liberación. El `0` indica que no requiere modificadores. En el `ActionMap`, la presión llama al callback con `true` y la liberación con `false`.
- Cuando presionas **W en la cancha**, Swing busca la asociación de presión por el código de la tecla. El callback agrega ese código al conjunto y ejecuta `onPlayerOneJump` una vez. Al soltar W, el código sale del conjunto y la próxima pulsación puede pedir otro salto.
- `DOPOSoccerGUI` aporta `onPlayerOneJump`. Con la cancha activa y el participante humano asignado, llama a `game.jumpPlayer(true)` para la identidad del Jugador 1. Para el Jugador 2 usa `false`. Si el callback se define por lado de cancha, aplica primero la correspondencia de la sección siguiente. En modo contra bot habilita únicamente los controles de la identidad humana.
- En el siguiente ciclo, `DOPOSoccerGUI.updateGame()` llama a `game.updateGame()`, publica las nuevas coordenadas mediante `publishMatch()` y llama a `matchPanel.refreshMatchView()`. La nueva posición Y de la captura llega al dibujo del personaje.

**Comprobación de la conexión.** En modo de dos humanos, inicia una partida con los valores iniciales. W debe hacer saltar al Jugador 1 (lado derecho) y ↑ al Jugador 2 (lado izquierdo). Vuelve a Configuración, cambia Salto del Jugador 1 a E y guarda. Al entrar otra vez, E debe hacer saltar a ese jugador y W debe quedar libre. Mantén E presionada y luego suéltala para comprobar que se solicita un salto por pulsación. En el código actual solo puedes comprobar por separado la edición del botón y el salto con W en el banco.



## 4. Convierte la tecla sostenida en un paso

En `DOPOSoccerGUI.updateGame()`, antes de `game.updateGame()`, consulta los booleanos. Si ambas direcciones del participante tienen el mismo estado, conserva la posición. Si solo una está pulsada, llama una vez a `game.movePlayer(identidad, dirección)`. La dirección es `true` para derecha y `false` para izquierda.

| Configuración | Identidad para el lado izquierdo | Identidad para el lado derecho |
| --- | --- | --- |
| Dos humanos | `false`, jugador 2 | `true`, jugador 1 |
| Humano contra bot | `configuration.playerOne()` | El bot actúa por el dominio |

La identidad y la dirección son dos booleanos distintos. Determina el lado con la configuración, calcula la identidad y pasa la dirección por separado. Después conserva el orden actual de la ventana: avanzar el juego, publicar una captura y llamar a `matchPanel.refreshMatchView()`. `CourtView.drawPlayer()` recibe la posición nueva de esa captura. Cambiar el sprite en el listener solo cambiaría el dibujo.

## 5. Separa salto y patada del movimiento sostenido

En el banco, `bindJump()` y `bindKick()` marcan una tecla al presionarla y borran esa marca al liberarla. Copia ese patrón para que un evento repetido no se convierta en varios intentos. Para el salto llama a `Game.jumpPlayer(identidad)`; para la patada usa `Game.kickPlayer(identidad)`. Conecta los callbacks a la ventana y comprueba allí que la cancha esté activa antes de ejecutar una acción. El dominio conserva las reglas de contacto.

## 6. Limpia las entradas y verifica

Adapta `DOPOSoccerGUI.showScreen()` para limpiar las direcciones y marcas de teclas al salir de la cancha, y el evento de pérdida de foco de la ventana para hacer lo mismo. Puedes seguir `MechanicsTestApp` → `MechanicsTestPanel.releaseControls()`. Al volver de Pausa, el movimiento se inicia de nuevo cuando recibe una pulsación.

1. Ejecuta `presentation.DOPOSoccerGUI` y abre una partida manual. Mantén D y suéltala. Con las teclas de Configuración por identidad, el Jugador 1 debe avanzar a la derecha y luego detenerse.
2. Mantén A y D a la vez. Comprueba que esas órdenes se cancelen. Prueba también ambos lados en el mismo ciclo.
3. Pausa mientras una tecla está sostenida, suéltala fuera de la cancha y reanuda. Verifica que no se quede caminando. Repite al perder foco.
4. En una aplicación recién iniciada, prueba contra bot. Las teclas humanas deben actuar sobre el lado izquierdo sin controlar al bot.
5. Prueba la colisión contra un rival y los bordes. El movimiento visible sigue lo que decidió el dominio.
