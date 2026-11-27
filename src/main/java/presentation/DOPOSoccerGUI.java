package presentation;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import domain.Game;
import presentation.MatchSnapshot.Ball;
import presentation.MatchSnapshot.Bounds;
import presentation.MatchSnapshot.Participant;

/**
 * Ventana principal de DOPOSoccer. Contiene todas las pantallas del juego.
 * Lee los catálogos de {@code Game} al construir la ventana y publica la
 * primera captura en {@link #startMatch(MatchConfiguration)}.
 * {@code updateGame()} avanza la partida y {@code publishMatch()} reúne la
 * geometría, los participantes, el tiempo y el marcador en {@link MatchSnapshot}.
 * La captura queda disponible en {@link PresentationState#getMatch()} antes
 * de refrescar la vista. {@link #showScreen(String)} refresca la pantalla
 * de destino y controla el temporizador. {@code improveStatistic()} procesa
 * la compra con la fachada, publica el resultado y refresca la tienda.
 */

public class DOPOSoccerGUI extends JFrame 
{
    private static final String TITLE = "DOPO SOCCER";
    private static final int WINDOW_WIDTH = 800;
    private static final int WINDOW_HEIGHT = 600;
    private static final int COURT_WINDOW_WIDTH_MARGIN = 40;
    private static final int COURT_WINDOW_HEIGHT_MARGIN = 140;
    private static final int MATCH_UPDATE_DELAY_MS = 1000 / 60;

    /** Controlador que coordina los cambios de pantalla. */
    private final NavigationController navigationController;
    /** Fachada que suministra el estado y las operaciones de la partida. */
    private final Game game;
    /** Avanza la sesión mientras se muestra la cancha. */
    private final Timer matchTimer;
    /** Teclas sostenidas para aplicar movimiento continuamente durante el juego. */
    private final Set<Integer> pressedKeys = new HashSet<>();
    /** Configuración con la que se inició la sesión administrada. */
    private MatchConfiguration startedConfiguration;
    /** Pantalla inicial con los accesos a las demás opciones. */
    private MainMenuPanel mainMenuPanel;
    /** Pantalla de asignación de teclas. */
    private ConfigurationPanel configurationPanel;
    /** Pantalla de selección de modalidad, cancha y personajes. */
    private PersonalizationPanel personalizationPanel;
    /** Pantalla de consulta y compra de mejoras. */
    private StoreImprovementPanel storeImprovementPanel;
    /** Pantalla previa al inicio de la partida. */
    private PreparationPanel preparationPanel;
    /** Pantalla donde se presenta la cancha de la partida. */
    private MatchPanel matchPanel;
    /** Pantalla de pausa con las acciones disponibles durante una partida. */
    private PausePanel pausePanel;
    /** Pantalla que resume el resultado de la partida. */
    private ResultsPanel resultsPanel;
 
    /**
     * Crea la ventana principal.
     */
    public DOPOSoccerGUI() 
    {
        super(TITLE);
        game = new Game();
        // Personalización recibe los catálogos antes de construir sus selectores.
        PresentationState.setOptions(game.getSessionTypes(), game.getMatchTypes(),
                game.getBotTypes(), game.getPowerTypes());
        PresentationState.setMatch(null);
        matchTimer = new Timer(MATCH_UPDATE_DELAY_MS, event -> updateGame());
        navigationController = new NavigationController(this);
        prepareView();
        prepareController();
    }

    /**
     * Crea y organiza los componentes visuales de la ventana.
     */
    private void prepareView() 
    {
        prepareViewLayout();
        prepareViewPanels();
        prepareViewScreens();
        prepareViewWindow();
    }

    /**
     * Define la distribución que permite mostrar una pantalla a la vez.
     */
    private void prepareViewLayout() 
    {
        getContentPane().setLayout(new CardLayout());
    }

    /**
     * Crea el panel de cada pantalla del juego.
     */
    private void prepareViewPanels() 
    {
        mainMenuPanel = new MainMenuPanel(navigationController, this::openMatch);
        configurationPanel = new ConfigurationPanel(navigationController);
        personalizationPanel = new PersonalizationPanel(navigationController);
        storeImprovementPanel = new StoreImprovementPanel(navigationController, this::improveStatistic);
        preparationPanel = new PreparationPanel(navigationController);
        resultsPanel = new ResultsPanel(navigationController);
        matchPanel = new MatchPanel(navigationController);
        pausePanel = new PausePanel(navigationController,
                () -> showScreen(NavigationController.RESULTS), this::restartMatch,
                this::saveMatch, Game.DEFAULT_SAVE_FILE);
    }

    /**
     * Registra cada panel con el nombre que usa el controlador de navegación.
     */
    private void prepareViewScreens() 
    {
        addScreen(mainMenuPanel, NavigationController.MAIN_MENU);
        addScreen(configurationPanel, NavigationController.CONFIGURATION);
        addScreen(personalizationPanel, NavigationController.PERSONALIZATION);
        addScreen(storeImprovementPanel, NavigationController.STORE_IMPROVEMENT);
        addScreen(preparationPanel, NavigationController.PREPARATION);
        addScreen(matchPanel, NavigationController.MATCH);
        addScreen(pausePanel, NavigationController.PAUSE);
        addScreen(resultsPanel, NavigationController.RESULTS);
    }

    /**
     * Configura el tamaño, la posición y las opciones de la ventana.
     */
    private void prepareViewWindow() 
    {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    /**
     * Agrega una pantalla a la ventana con el nombre indicado.
     *
     * @param panel panel de la pantalla
     * @param name nombre con el que se mostrará (constante de NavigationController)
     */
    private void addScreen(BasePanel panel, String name) 
    {
        getContentPane().add(panel, name);
    }

    /**
     * Asocia los eventos de la ventana con sus acciones.
     */
    private void prepareController() 
    {
        prepareControllerClose();
        prepareControllerKeyboard();
    }

    /**
     * Define qué ocurre al cerrar la ventana: termina la aplicación.
     */
    private void prepareControllerClose() 
    {
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    /** Conecta los controles predeterminados visibles en configuración con el partido activo. */
    private void prepareControllerKeyboard() {
        bindMatchKey(KeyEvent.VK_A, "player-one-left", null);
        bindMatchKey(KeyEvent.VK_D, "player-one-right", null);
        bindMatchKey(KeyEvent.VK_W, "player-one-jump",
                () -> requestPlayerAction(true, () -> game.jumpPlayer(true)));
        bindMatchKey(KeyEvent.VK_SPACE, "player-one-kick",
                () -> requestPlayerAction(true, () -> game.kickPlayer(true)));

        bindMatchKey(KeyEvent.VK_LEFT, "player-two-left", null);
        bindMatchKey(KeyEvent.VK_RIGHT, "player-two-right", null);
        bindMatchKey(KeyEvent.VK_UP, "player-two-jump",
                () -> requestPlayerAction(false, () -> game.jumpPlayer(false)));
        bindMatchKey(KeyEvent.VK_0, "player-two-kick",
                () -> requestPlayerAction(false, () -> game.kickPlayer(false)));

        addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent event) {
                clearPressedKeys();
            }
        });
    }

    /** Registra una pareja de acciones Swing para la presión y liberación de una tecla. */
    private void bindMatchKey(int keyCode, String name, Runnable pressAction) {
        InputMap inputMap = matchPanel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = matchPanel.getActionMap();
        String pressedName = name + "-pressed";
        String releasedName = name + "-released";
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedName);
        inputMap.put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedName);
        actionMap.put(pressedName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                if (!matchTimer.isRunning() || !pressedKeys.add(keyCode)) return;
                if (pressAction != null) pressAction.run();
            }
        });
        actionMap.put(releasedName, new AbstractAction() {
            @Override
            public void actionPerformed(java.awt.event.ActionEvent event) {
                pressedKeys.remove(keyCode);
            }
        });
    }

    /** Ejecuta una acción discreta solo para el participante humano de la partida. */
    private void requestPlayerAction(boolean playerOne, Runnable action) {
        if (matchTimer.isRunning() && isHumanController(playerOne)) action.run();
    }

    /** Comprueba si la identidad corresponde a una persona y no al bot de la sesión. */
    private boolean isHumanController(boolean playerOne) {
        return startedConfiguration != null && (!startedConfiguration.againstBot()
                || startedConfiguration.playerOne() == playerOne);
    }

    /** Aplica un paso horizontal a cada participante humano cuya tecla siga presionada. */
    private void updateHumanMovement() {
        updatePlayerMovement(true, KeyEvent.VK_A, KeyEvent.VK_D);
        updatePlayerMovement(false, KeyEvent.VK_LEFT, KeyEvent.VK_RIGHT);
    }

    /** Interpreta las dos teclas de dirección para una identidad de jugador. */
    private void updatePlayerMovement(boolean playerOne, int leftKey, int rightKey) {
        if (!isHumanController(playerOne)) return;
        boolean left = pressedKeys.contains(leftKey);
        boolean right = pressedKeys.contains(rightKey);
        if (left != right) game.movePlayer(playerOne, right);
    }

    /** Olvida las teclas sostenidas al salir de la cancha o perder el foco de la ventana. */
    private void clearPressedKeys() {
        pressedKeys.clear();
    }

    /**
     * Recibe una solicitud de navegación de un panel y detiene el temporizador.
     * Al entrar en la cancha refresca la última captura y reanuda el temporizador
     * si la partida sigue activa. Al entrar en preparación, resultados o tienda,
     * llama al refresco del panel antes de mostrarlo en el {@code CardLayout}.
     *
     * @param screen nombre de la pantalla que se mostrará
     */
    public void showScreen(String screen) 
    {
        // Solo la cancha de una partida activa vuelve a iniciar el temporizador.
        matchTimer.stop();
        if (!NavigationController.MATCH.equals(screen)) clearPressedKeys();
        if (NavigationController.MATCH.equals(screen)) {
            MatchSnapshot match = PresentationState.getMatch().orElse(null);
            if (match == null) return;
            if (match.isFinished()) {
                screen = NavigationController.RESULTS;
            } else {
                matchPanel.refreshMatchView();
                matchTimer.start();
            }
        }
        // Refresca el panel de destino antes de que CardLayout lo muestre.
        if (NavigationController.RESULTS.equals(screen)) {
            resultsPanel.refreshFromState();
        } else if (NavigationController.STORE_IMPROVEMENT.equals(screen)) {
            storeImprovementPanel.refreshFromState();
        } else if (NavigationController.PREPARATION.equals(screen)) {
            preparationPanel.refreshFromState();
        }
        resizeWindowForScreen(screen);
        CardLayout layout = (CardLayout) getContentPane().getLayout();
        layout.show(getContentPane(), screen);
        if (NavigationController.MATCH.equals(screen) && matchTimer.isRunning()) {
            SwingUtilities.invokeLater(matchPanel::focusCourtForControls);
        }
    }

    /** Ajusta la ventana al tamaño configurado mientras se muestran pantallas del partido.
     * @param screen identificador de la pantalla que se va a mostrar
     */
    private void resizeWindowForScreen(String screen) {
        boolean showCourt = isCourtRelatedScreen(screen);
        Dimension targetSize = showCourt && startedConfiguration != null
                ? courtWindowSize(startedConfiguration)
                : new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT);
        if (!getSize().equals(targetSize)) {
            setSize(targetSize);
            setLocationRelativeTo(null);
        }
    }

    /** Indica si la pantalla forma parte del flujo de una partida ya configurada.
     * @param screen identificador de pantalla
     * @return {@code true} si debe conservar el tamaño de ventana de la cancha
     */
    private boolean isCourtRelatedScreen(String screen) {
        return NavigationController.PREPARATION.equals(screen)
                || NavigationController.MATCH.equals(screen)
                || NavigationController.PAUSE.equals(screen)
                || NavigationController.RESULTS.equals(screen);
    }

    /** Calcula el tamaño de ventana según las dimensiones elegidas y el área visible.
     * @param configuration configuración que contiene alto y ancho de la cancha
     * @return tamaño de ventana limitado al área útil de pantalla
     */
    private Dimension courtWindowSize(MatchConfiguration configuration) {
        int courtHeight = Integer.parseInt(configuration.parametersRules().get(0));
        int courtWidth = Integer.parseInt(configuration.parametersRules().get(1));
        Dimension usableScreen = GraphicsEnvironment.getLocalGraphicsEnvironment()
                .getMaximumWindowBounds().getSize();
        int windowWidth = Math.min(courtWidth + COURT_WINDOW_WIDTH_MARGIN, usableScreen.width);
        int windowHeight = Math.min(courtHeight + COURT_WINDOW_HEIGHT_MARGIN, usableScreen.height);
        return new Dimension(windowWidth, windowHeight);
    }

    /** Crea la sesión desde la configuración de presentación y alista el partido.
     * @param configuration opciones de sesión seleccionadas en la presentación
     */
    public void startMatch(MatchConfiguration configuration) {
        if (startedConfiguration != null && !startedConfiguration.equals(configuration)
                && !game.isFinished()) {
            throw new IllegalStateException("La sesión única ya conserva otra configuración. "
                    + "Termina la partida actual antes de iniciar otra configuración.");
        }
        game.startSession(configuration.parametersRules(), configuration.parametersPowers(),
                configuration.typeGame(), configuration.typeMatch(), configuration.playerOne());
        startedConfiguration = configuration;
        // Preparación necesita la captura inicial aunque el reloj aún no haya avanzado.
        publishMatch();
        showScreen(NavigationController.PREPARATION);
    }

    /** Reinicia el partido completo y vuelve a su pantalla previa al inicio. */
    private void restartMatch() {
        game.resetSession();
        publishMatch();
        showScreen(NavigationController.PREPARATION);
    }

    /** Guarda el estado del dominio y la configuración visual necesaria para restaurarlo. */
    private void saveMatch(File file) {
        File visualFile = configurationFile(file);
        if ((file.exists() || visualFile.exists()) && JOptionPane.showConfirmDialog(this,
                "¿Quieres reemplazar el guardado seleccionado?", "Guardar partida",
                JOptionPane.YES_NO_OPTION) != JOptionPane.YES_OPTION) return;

        try {
            if (startedConfiguration == null) {
                throw new IllegalStateException("No hay una configuración de partida para guardar.");
            }
            writeMatchFiles(file.toPath().toAbsolutePath(), visualFile.toPath());
            JOptionPane.showMessageDialog(this, "Partida guardada correctamente.",
                    "Partida guardada", JOptionPane.INFORMATION_MESSAGE);
        } catch (RuntimeException | IOException exception) {
            JOptionPane.showMessageDialog(this, "No se pudo guardar la partida: "
                    + exception.getMessage(), "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Prepara ambos archivos antes de reemplazar el guardado anterior. */
    private void writeMatchFiles(Path gamePath, Path visualPath) throws IOException {
        Path tempGame = Files.createTempFile(gamePath.getParent(), ".dopo-", ".tmp");
        try {
            Path tempVisual = Files.createTempFile(gamePath.getParent(), ".dopo-", ".tmp");
            try {
                game.save(tempGame.toFile());
                try (ObjectOutputStream output = new ObjectOutputStream(
                        new FileOutputStream(tempVisual.toFile()))) {
                    output.writeObject(startedConfiguration);
                }
                Files.move(tempGame, gamePath, StandardCopyOption.REPLACE_EXISTING);
                Files.move(tempVisual, visualPath, StandardCopyOption.REPLACE_EXISTING);
            } finally {
                Files.deleteIfExists(tempVisual);
            }
        } finally {
            Files.deleteIfExists(tempGame);
        }
    }

    /** Restaura el dominio y su configuración de presentación desde el guardado. */
    private void openMatch(File file) {
        try {
            MatchConfiguration configuration = readMatchConfiguration(file);
            game.open(file);
            startedConfiguration = configuration;
            publishMatch();
            showScreen(NavigationController.PAUSE);
        } catch (RuntimeException | IOException | ClassNotFoundException exception) {
            JOptionPane.showMessageDialog(this, "No se pudo cargar la partida: "
                    + exception.getMessage(), "Error al cargar", JOptionPane.ERROR_MESSAGE);
        }
    }

    /** Lee el auxiliar visual o la configuración integrada de los guardados anteriores. */
    private MatchConfiguration readMatchConfiguration(File file)
            throws IOException, ClassNotFoundException {
        File visualFile = configurationFile(file);
        if (!visualFile.exists()) return MatchFile.readConfiguration(file);
        try (ObjectInputStream input = new ObjectInputStream(new FileInputStream(visualFile))) {
            if (input.readObject() instanceof MatchConfiguration configuration) return configuration;
            throw new IOException("El archivo no contiene la configuración visual de la partida.");
        }
    }

    /** Archivo auxiliar con configuración de personajes y cancha de la interfaz. */
    private File configurationFile(File gameFile) {
        return new File(gameFile.getAbsolutePath() + ".ui");
    }

    /** Avanza un paso, publica su estado y refresca la vista que muestra el partido. */
    private void updateGame() {
        updateHumanMovement();
        game.updateGame();
        MatchSnapshot match = publishMatch();
        matchPanel.refreshMatchView();
        if (match.isFinished()) {
            showScreen(NavigationController.RESULTS);
        }
    }

    /**
     * Captura un paso en este orden: configuración, cancha, arcos, balones,
     * participante izquierdo, participante derecho, goles izquierdos, goles
     * derechos, segundos transcurridos, muerte súbita y partida terminada.
     * @return captura que también queda publicada en el estado de presentación
     */
    private MatchSnapshot publishMatch() {
        // Cada rectángulo se construye con los getters X, Y, ancho y alto de la fachada.
        Bounds court = new Bounds(game.getCourtX(), game.getCourtY(),
                game.getCourtWidth(), game.getCourtHeight());
        List<Bounds> goals = new ArrayList<>(2);
        for (int index = 0; index < 2; index++) {
            goals.add(new Bounds(game.getGoalX(index), game.getGoalY(index),
                    game.getGoalWidth(index), game.getGoalHeight(index)));
        }
        int ballCount = game.getBallCount();
        // Conserva el orden de índices de Game al copiar centros, radios y velocidades.
        List<Ball> balls = new ArrayList<>(ballCount);
        for (int index = 0; index < ballCount; index++) {
            balls.add(new Ball(game.getBallX(index), game.getBallY(index), game.getBallRadius(index),
                    game.getBallVelocityX(index), game.getBallVelocityY(index)));
        }
        // Game consulta por identidad y MatchSnapshot guarda los jugadores por lado de la cancha.
        boolean leftIsPlayerOne = startedConfiguration.againstBot() && startedConfiguration.playerOne();
        List<String> score = game.getMatchState();
        // Solo la regla temporal agrega la muerte súbita al cuarto elemento.
        boolean suddenDeath = score.size() > 3 && Boolean.parseBoolean(score.get(3));
        MatchSnapshot match = new MatchSnapshot(startedConfiguration, court, goals, balls,
                readParticipant(leftIsPlayerOne), readParticipant(!leftIsPlayerOne),
                game.getGoalsLeft(), game.getGoalsRight(), game.getElapsedSeconds(), suddenDeath,
                game.isFinished());
        // Publica en una sola asignación la captura que leerán los paneles.
        PresentationState.setMatch(match);
        return match;
    }

    /**
     * Es el punto donde los getters de {@code Game} alimentan la hitbox,
     * la vida, la recarga, los puntos, los niveles de mejora y su próximo costo.
     * Los paneles acceden a esos valores a través de {@link MatchSnapshot.Participant}.
     * @param playerOne indica si se consulta la identidad del jugador 1
     * @return datos del participante para la captura que se va a publicar
     */
    private Participant readParticipant(boolean playerOne) {
        // La geometría se guarda en Bounds y el resto de los valores alimenta el HUD y la tienda.
        Bounds bounds = new Bounds(game.getPlayerX(playerOne), game.getPlayerY(playerOne),
                game.getPlayerWidth(playerOne), game.getPlayerHeight(playerOne));
        return new Participant(bounds, game.getPlayerLife(playerOne), game.getPlayerMaxLife(playerOne),
                game.getPlayerPowerRemainingSeconds(playerOne), game.getPlayerPoints(playerOne),
                game.getPlayerSpeed(playerOne), game.getPlayerJump(playerOne),
                game.getPlayerKick(playerOne), game.getPlayerNextUpgradeCost(playerOne));
    }

    /**
     * Recibe el clic de compra desde la tienda. Usa la captura para comprobar
     * que la partida terminó y que existe esa identidad humana. Traduce la
     * identidad a lado para llamar a la fachada y, si la compra se completa,
     * publica el nuevo saldo antes de refrescar la tienda.
     * @param playerOne {@code true} para el jugador humano 1
     * @param statistic clave de la habilidad elegida en la tienda
     */
    private void improveStatistic(boolean playerOne, String statistic) {
        // Confirma que la identidad corresponde a un humano antes de solicitar la compra.
        MatchSnapshot match = PresentationState.getMatch().orElse(null);
        if (match == null || !match.isFinished() || match.getHumanPlayer(playerOne).isEmpty()) return;
        // La compra de la fachada usa lado físico. Contra bot, el humano está a la izquierda.
        boolean rightSide = !match.getConfiguration().againstBot() && playerOne;
        try {
            game.improveStatistics(rightSide, statistic);
            // Refresca la tienda después de publicar el saldo actualizado.
            publishMatch();
            storeImprovementPanel.refreshFromState();
        } catch (IllegalArgumentException exception) {
            JOptionPane.showMessageDialog(storeImprovementPanel, exception.getMessage(),
                    "No se pudo comprar la mejora", JOptionPane.WARNING_MESSAGE);
        }
    }

    /** Detiene el avance de la partida al cerrar la ventana. */
    @Override
    public void dispose() {
        matchTimer.stop();
        super.dispose();
    }

    /** Punto de lectura de catálogos y de la última captura de la presentación
     * oficial. La ventana escribe con {@code setOptions()} y {@code setMatch()};
     * cada panel consulta los getters cuando necesita refrescarse. */
    public static final class PresentationState {
        private static volatile MatchSnapshot match;
        private static volatile List<String> sessionTypes = List.of();
        private static volatile List<String> matchTypes = List.of();
        private static volatile List<String> botTypes = List.of();
        private static volatile List<String> powerTypes = List.of();

        /** Impide crear instancias del estado compartido. */
        private PresentationState() { }

        /** Devuelve el último estado publicado.
         * @return datos inmutables de la partida, o vacío antes de iniciarla
         */
        public static Optional<MatchSnapshot> getMatch() { return Optional.ofNullable(match); }

        /** Devuelve las modalidades de sesión disponibles.
         * @return lista inmutable de nombres simples
         */
        public static List<String> getSessionTypes() { return sessionTypes; }

        /** Devuelve las reglas de partido disponibles.
         * @return lista inmutable de nombres simples
         */
        public static List<String> getMatchTypes() { return matchTypes; }

        /** Devuelve los perfiles de bot disponibles.
         * @return lista inmutable de nombres simples
         */
        public static List<String> getBotTypes() { return botTypes; }

        /** Devuelve los poderes admitidos por el catálogo del dominio.
         * @return lista inmutable de nombres completos de clase
         */
        public static List<String> getPowerTypes() { return powerTypes; }

        /** Publica una captura completa de la partida con una sola asignación. */
        private static void setMatch(MatchSnapshot value) {
            match = value;
        }

        /** Publica copias inmutables de las opciones que ofrece Game. */
        private static void setOptions(List<String> sessions, List<String> rules, List<String> bots,
                List<String> powers) {
            sessionTypes = List.copyOf(sessions);
            matchTypes = List.copyOf(rules);
            botTypes = List.copyOf(bots);
            powerTypes = List.copyOf(powers);
        }
    }

    /**
     * Inicia la aplicación.
     * @param args argumentos de línea de comandos; no se utilizan
     */
    public static void main(String[] args) 
    {
        SwingUtilities.invokeLater(() -> {
            DOPOSoccerGUI gui = new DOPOSoccerGUI();
            gui.setVisible(true);
        });
    }
}
