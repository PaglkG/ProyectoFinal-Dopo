/**
 * Presentación Swing de DOPO Soccer.
 *
 * <h2>Agregar un botón a un menú</h2>
 * <ol>
 *   <li>Declara el botón en el panel si su acción se conecta en otro método.
 *       Créalo en la sección de vista y agrégalo al contenedor con
 *       {@code add(...)}. El orden y el diseño del contenedor determinan su posición.</li>
 *   <li>En {@code prepareController()} o en el método que conecta ese grupo,
 *       registra la acción con {@code addActionListener(...)}.</li>
 *   <li>Para ir a otra pantalla, llama a
 *       {@code navigationController.showScreen(NavigationController.CONFIGURATION)}
 *       usando la constante del destino. Si la pantalla es nueva, declara su
 *       identificador en {@link presentation.NavigationController} y crea y
 *       registra su panel en {@link presentation.DOPOSoccerGUI} bajo la misma clave.</li>
 * </ol>
 *
 * <h2>Dónde está cada menú de botones</h2>
 * <ul>
 *   <li>{@link presentation.MainMenuPanel}: {@code prepareViewMainButtons()}
 *       agrega Jugar y Abrir Partida a {@code content}. {@code prepareViewBottomButtons()}
 *       agrega Configuración y Salir a {@code bottomRow}.</li>
 *   <li>{@link presentation.ConfigurationPanel}: {@code prepareViewButtons()}
 *       agrega Guardar y Volver a {@code row}. Los botones de teclas están en la cuadrícula.</li>
 *   <li>{@link presentation.PersonalizationPanel}: {@code prepareViewButtons()}
 *       agrega Tienda a {@code content} y los botones de inicio y regreso a {@code bottomRow}.</li>
 *   <li>{@link presentation.PreparationPanel}: {@code prepareViewPreparationButtons()}
 *       agrega Cancelar y Continuar a {@code buttons}.</li>
 *   <li>{@link presentation.MatchPanel}: {@code prepareViewScore()} coloca el botón
 *       Pausa en la parte inferior del marcador.</li>
 *   <li>{@link presentation.PausePanel}: {@code prepareViewPauseCard()} agrega los
 *       botones a la tarjeta vertical {@code card}.</li>
 *   <li>{@link presentation.ResultsPanel}: {@code prepareViewResult()} agrega
 *       Tienda y Menú principal a {@code card}.</li>
 *   <li>{@link presentation.StoreImprovementPanel}: {@code prepareViewBackButton()}
 *       agrega el botón de regreso. Los botones de compra se agregan en cada fila de mejora.</li>
 * </ul>
 */
package presentation;
