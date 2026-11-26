# Guía para editar y extender la presentación de DOPOSoccer

## Por dónde empezar

Cada pantalla oficial hereda de [BasePanel](../src/main/java/presentation/BasePanel.java). Busca `prepareView()` para crear o reorganizar componentes Swing y `prepareController()` para vincular botones y eventos. Para mostrar un dato que cambia durante la partida, crea la etiqueta una sola vez en la vista y actualiza su texto en el método de refresco del panel.

| Si quieres cambiar… | Empieza en… |
| --- | --- |
| Texto, posición, márgenes o imágenes | `prepareView()` y sus métodos auxiliares |
| Qué sucede al presionar un botón | `prepareController()` y el listener de ese botón |
| Qué valor muestra una etiqueta o barra | El método de refresco y el getter que lee |
| Cómo se cambia de pantalla | `NavigationController` y `DOPOSoccerGUI.showScreen()` |
| Un dato del juego disponible en varios paneles | `MatchSnapshot` y `DOPOSoccerGUI.publishMatch()` |

La ventana es el punto de contacto de la presentación oficial con `Game`. En cada paso del temporizador, [DOPOSoccerGUI](../src/main/java/presentation/DOPOSoccerGUI.java) llama a `game.updateGame()`, captura los datos de ese paso en [MatchSnapshot](../src/main/java/presentation/MatchSnapshot.java) y los publica en `PresentationState`. Los paneles leen la última captura cuando se refrescan. La ventana publica la primera captura al iniciar la sesión y otra después de completar una compra.

```text
Lectura: Game → publishMatch() → PresentationState.getMatch() → panel → etiqueta o dibujo
Acción: botón del panel → navegación o callback → ventana → Game, si corresponde
```

Lee `PresentationState.getMatch()` una sola vez por refresco. Devuelve un `Optional<MatchSnapshot>` vacío antes de iniciar una partida. Usa `getParticipant(false)` para el lado izquierdo y `getParticipant(true)` para el derecho. Para el saldo de un usuario, usa `getHumanPlayer(true)` para el jugador 1 y `getHumanPlayer(false)` para el jugador 2. En un partido contra un bot, el humano ocupa la izquierda y la identidad humana ausente devuelve un `Optional` vacío.

## 1. Menú principal

**Ejemplo: agregar un botón que abra la configuración.** [MainMenuPanel](../src/main/java/presentation/MainMenuPanel.java)

1. Declara un `JButton` como campo si vas a usarlo al construir la vista y al conectar sus eventos.
2. Créalo y agrégalo al contenedor en `prepareViewMainButtons()` o `prepareViewBottomButtons()`, según dónde quieras mostrarlo.
3. En `prepareController()`, registra una acción que llame a `navigationController.showScreen(NavigationController.CONFIGURATION)`.
4. Haz clic y comprueba que se muestra la pantalla de destino. Para modificar el logo o el texto alternativo, entra en `loadLogo()`.

El botón **Abrir partida** llama a `openMatch()`, que actualmente abre el selector de archivos. Conectar la carga exige decidir cómo la ventana reconstruirá y publicará la configuración de la partida cargada.

## 2. Configuración de teclas

**Ejemplo: cambiar la tecla inicial de salto de un jugador.** [ConfigurationPanel](../src/main/java/presentation/ConfigurationPanel.java)

1. Ubica `Salto` en `ACTION_NAMES` y cambia el `KeyEvent.VK_…` del mismo índice en `PLAYER1_DEFAULT_KEYS` o `PLAYER2_DEFAULT_KEYS`.
2. Si agregas una acción al formulario, agrega también su tecla inicial a ambos arreglos. `prepareViewKeyGrid()` genera los controles usando ese orden.
3. Para cambiar cómo se captura una tecla, parte de `prepareControllerKeyButton()`. `captureKey()` recibe el evento y `assignKey()` comprueba duplicados y actualiza el botón. `cancelKeyCapture()` restaura el texto anterior.
4. Comprueba la tecla inicial, una asignación nueva y el intento de usar una tecla que ya está ocupada.

Actualmente **Guardar** vuelve al menú y la edición cambia el texto de los botones. Conectar las asignaciones a la partida o conservarlas al cerrar la aplicación requiere definir cómo se comparten y guardan.

## 3. Personalización de la partida

**Ejemplo: ofrecer un tamaño adicional de cancha.** [PersonalizationPanel](../src/main/java/presentation/PersonalizationPanel.java)

1. Añade una opción a `FIELD_SIZES` con el formato de las actuales, `"ancho x alto"`.
2. Revisa `prepareViewMatchOptions()`, que crea el selector. Al iniciar, `createMatchConfiguration()` lee el tamaño y entrega a la fachada los parámetros en el orden **alto, ancho, límite**.
3. Entra a preparación y después a la cancha para verificar que se aplicaron las dimensiones. Reinicia la aplicación entre configuraciones distintas porque la sesión es única.
4. Para cambiar las vistas previas, edita `CHARACTER_PROFILES`, revisa los recursos que carga `loadPreview()` y comprueba las imágenes y banderas en preparación, partido y resultados. Si agregas un personaje con un poder nuevo, primero verifica que el dominio acepte ese poder.

Las opciones de modalidad, regla y perfil de bot vienen de `PresentationState`. Para cambiar sus textos visibles, revisa `GameOptionRenderer`. Los identificadores que recibe `createMatchConfiguration()` deben coincidir con el catálogo. `updateMode()`, `updateMatchType()` y `updateCountry()` actualizan los controles dependientes de cada selección.

## 4. Preparación

**Ejemplo: mostrar la modalidad junto al aviso de partida lista.** [PreparationPanel](../src/main/java/presentation/PreparationPanel.java)

1. Declara una etiqueta para la modalidad y créala en `prepareViewLoadingBody()` o en uno de sus métodos auxiliares.
2. En `refreshFromState()`, dentro de `ifPresent`, toma `match.getConfiguration()` y usa `configuration.typeMatch()` para elegir el nombre visible.
3. Conserva `setCountryCodes()` para las banderas y `setReady()` para marcar el progreso completo. Si personalizas el texto de `statusLabel`, escríbelo en `setReady()` o después de su llamada, porque ese método también lo actualiza.
4. Verifica la pantalla con una partida por goles y otra por tiempo. Para cambiar los destinos de los botones, usa `prepareControllerCancel()` y `prepareControllerContinue()`.

## 5. Partido

**Ejemplo: mostrar los puntos de cada lado junto con la vida y la recarga.** [MatchPanel](../src/main/java/presentation/MatchPanel.java)

1. Crea una etiqueta por lado en el HUD. Usa `prepareViewHud()` y los componentes `PlayerStatus` como referencia para colocarla.
2. Actualiza la etiqueta en `showParticipantStatus(boolean right, Participant participant)` con `participant.getPoints()`. Usa `right` para elegir la etiqueta del lado correspondiente. El método ya recibe el mismo participante que alimenta las barras de vida y recarga.
3. Para cambiar marcador o reloj, edita `setScore()` o `setElapsedSeconds()`. Para cambiar cómo se dibujan balones, arcos o jugadores, entra a `CourtView.paintComponent()` y a los métodos de dibujo que llama. Trabaja con la geometría de la captura y conserva su conversión a píxeles.
4. Abre la cancha, pausa y reanuda. Comprueba que los datos pertenecen al lado correcto en partida manual y contra bot. El temporizador y la publicación se gestionan en la ventana.

## 6. Pausa

**Ejemplo: cambiar el texto de un botón o agregar un acceso al menú.** [PausePanel](../src/main/java/presentation/PausePanel.java)

1. Agrega o cambia el componente donde se construye el menú en `prepareView()`.
2. En `prepareController()`, conecta el evento. Los botones actuales piden continuar o volver al menú mediante el navegador, o ejecutan `resultsPreviewAction` para mostrar resultados.
3. Deja la transición en `DOPOSoccerGUI.showScreen()`, que detiene el temporizador al cambiar de pantalla y lo activa al volver a una partida en curso.
4. Pausa y comprueba que el tiempo deja de avanzar. Usa Continuar y verifica que se reanuda.

## 7. Resultados

**Ejemplo: agregar el indicador de muerte súbita al resumen.** [ResultsPanel](../src/main/java/presentation/ResultsPanel.java)

1. Crea la etiqueta en `prepareView()` junto a las etiquetas de modalidad y duración.
2. En `showResult(MatchSnapshot match)`, usa `match.isSuddenDeath()` y llama a `setText()` en la etiqueta. Si quieres que aparezca solo cuando el partido está en esa fase, actualiza también `setVisible()` en ese método.
3. Para agregar información de los personajes o de la cancha, empieza en `setMatchConfiguration()`. Para cambiar el texto de victoria o empate, empieza en `showResult()`.
4. Comprueba el resultado al terminar un partido y al abrir la vista previa desde Pausa. Si el rótulo debe distinguir una vista previa de un resultado definitivo, usa `match.isFinished()`.

## 8. Tienda de mejoras

**Ejemplo: mostrar los segundos de recarga debajo del saldo de cada jugador.** [StoreImprovementPanel](../src/main/java/presentation/StoreImprovementPanel.java)

1. Declara dos campos `JLabel` junto a `player1PointsLabel` y `player2PointsLabel`:

   ```java
   private JLabel player1PowerLabel;
   private JLabel player2PowerLabel;
   ```

2. Inicialízalos en `prepareViewPlayers()` con un texto inicial. Agrega `JLabel powerLabel` a la firma de `createPlayerColumn()` y pasa la etiqueta correspondiente en sus dos llamadas.
3. Dentro de `createPlayerColumn()`, agrega `column.add(powerLabel)` justo después de `column.add(pointsLabel)`.
4. Dentro de `refreshPlayer()`, coloca este bloque justo después de `pointsLabel.setText(points)`. Queda antes del bucle de mejoras:

   ```java
   JLabel powerLabel = playerOne ? player1PowerLabel : player2PowerLabel;
   powerLabel.setText(player == null
           ? "Recarga: " + UNAVAILABLE
           : String.format("Recarga: %.1f s", player.getPowerRemainingSeconds()));
   ```

5. Abre la tienda antes de jugar y después de una partida. Comprueba que la columna de una identidad ausente muestra `—` y que los valores vienen de la última captura. La tienda refresca al entrar y después de una compra completada.

Para cambiar **cómo se ve el saldo**, edita `refreshPlayer()`. Para cambiar **cómo se solicita una compra**, sigue `prepareControllerUpgrades()` → `DOPOSoccerGUI.improveStatistic()` → `Game.improveStatistics()`. `STAT_NAMES` y `STAT_KEYS` comparten el orden de las filas. El precio real de la siguiente compra y los niveles de salto y patada todavía requieren datos que la fachada y la captura puedan entregar. `displayedLevel()` ya usa el nivel de velocidad y deriva el nivel de vida de `getMaxLife()`.

## 9. Banco de pruebas

**Ejemplo: agregar una capa con las coordenadas del primer balón.** [MechanicsTestPanel](../src/main/java/presentation/testing/MechanicsTestPanel.java)

1. En `registerDefaultLayers()`, añade `layers.add(new LayerOption("Posición del balón", false));`. El menú crea su casilla a partir de esa lista.
2. En `GameCanvas.drawLayer()`, agrega un `case "Posición del balón"` con el mismo texto. Si `game.getBallCount() > 0`, lee `game.getBallX(0)` y `game.getBallY(0)` y dibuja el texto con `g.drawString(...)`.
3. Para un escenario seleccionable, usa `addScenario("Nombre visible", acción)` dentro de `registerDefaultScenarios()`. La acción prepara el escenario mediante las operaciones disponibles en el controlador. Por ejemplo, `addScenario("Reiniciar ronda", this::resetRound)` reutiliza el reinicio existente.
4. Ejecuta `presentation.testing.MechanicsTestApp` y activa o desactiva la nueva casilla. Comprueba que el dibujo aparece durante el avance y que desaparece al desactivarla.

El banco tiene su propio `MechanicsTestController`. `tick()` avanza la simulación y repinta. El lienzo lee del `Game` del banco mediante `controller.game()`.

## Agregar una pantalla oficial nueva

1. Crea una clase en `presentation` que extienda `BasePanel`. Usa un constructor que reciba las dependencias de presentación, las asigne y llame a `prepareView()` y `prepareController()`. Puedes seguir la estructura de `PausePanel` si tu pantalla solo navega.
2. Agrega un identificador en [NavigationController](../src/main/java/presentation/NavigationController.java).
3. Declara el panel como campo en `DOPOSoccerGUI`, créalo en `prepareViewPanels()` y regístralo en `prepareViewScreens()` con `addScreen(panel, identificador)`.
4. Si necesita datos de partida, añade un método de refresco que consulte la última captura y llámalo al entrar desde `DOPOSoccerGUI.showScreen()`.
5. Conecta el botón de entrada y el de regreso. Verifica el recorrido completo y la disponibilidad del contenido antes y después de iniciar una partida.

## Llevar un dato nuevo desde el juego a un panel

1. Busca el dato en `MatchSnapshot`. Si ya existe, úsalo en el refresco del panel.
2. Si el dato ya lo expone `Game` pero falta en la captura, agrega el campo y su getter a `MatchSnapshot` o a `MatchSnapshot.Participant`. Actualiza el constructor correspondiente y la lectura en `DOPOSoccerGUI.publishMatch()` o `readParticipant()`.
3. Si la fachada carece de ese dato, revisa primero el contrato con el dominio. Las reglas del repositorio reservan a `Game` como conexión con el dominio y prohíben agregarle código en el alcance actual.
4. Prueba el cambio con una captura vacía y otra publicada. Para abrir las pantallas, ejecuta `presentation.DOPOSoccerGUI` desde el IDE. Para comprobar la compilación y las pruebas del proyecto, usa `mvn verify`.

Consulta las [reglas del proyecto](logica-proyecto.md) al extender un comportamiento que afecte la partida.
