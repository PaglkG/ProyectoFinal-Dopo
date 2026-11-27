package presentation.testing;

import domain.Game;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import java.util.function.Consumer;
import javax.imageio.ImageIO;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.ImageIcon;
import java.awt.Image;

/**
 * Banco visual configurable para inspeccionar los datos de juego y las hitboxes.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> el constructor recibe el perfil opcional del
 *       bot. {@code refreshLabels()} y {@code GameCanvas.paintComponent()}
 *       leen el estado de la fachada obtenida con {@code controller.game()}.</li>
 *   <li><strong>Modificación:</strong> {@code bindKeys()} conecta movimiento,
 *       salto y patada con el controlador. {@code resetRound()} solicita el
 *       reinicio y {@code buildSideMenu()} alterna las capas visibles del banco.</li>
 *   <li><strong>Actualización:</strong> el temporizador ejecuta {@code tick()},
 *       que llama a {@code controller.update()}, refresca las etiquetas
 *       y repinta la cancha. Las capas de vida y poder muestran textos
 *       fijos de diagnóstico en {@code drawLife()} y {@code drawPower()}.</li>
 * </ul>
 */
public final class MechanicsTestPanel extends JPanel {
    private static final Color BACKGROUND = new Color(25, 31, 40);
    private static final Color TEXT = new Color(238, 242, 246);
    private static final Color LEFT_COLOR = new Color(255, 100, 90);
    private static final Color RIGHT_COLOR = new Color(87, 200, 255);
    private static final double CHARACTER_HEAD_HEIGHT_RATIO = 0.16;
    private static final double CHARACTER_BODY_HEIGHT_RATIO = 0.09;
    private static final double CHARACTER_BODY_WIDTH_RATIO = 0.62;
    private static final CharacterStyle LEFT_CHARACTER = new CharacterStyle("Lobo",
            "/images/outfits/heads/Heads Scaled/wolf_referencia.png",
            "/images/outfits/shoes/PATAWOLF.png", true);
    private static final CharacterStyle RIGHT_CHARACTER = new CharacterStyle("Tigre",
            "/images/outfits/heads/Heads Scaled/tiger_escalado.png",
            "/images/outfits/shoes/tiger@4x-8.png", false);

    /** Intermediario entre los controles del banco y la fachada de juego. */
    private final MechanicsTestController controller;
    /** Escenarios disponibles para preparar el banco. */
    private final List<TestScenario> scenarios = new ArrayList<>();
    /** Elementos que el usuario puede mostrar u ocultar en la cancha. */
    private final List<LayerOption> layers = new ArrayList<>();
    /** Selector de escenarios registrados en el banco. */
    private final JComboBox<TestScenario> scenarioMenu = new JComboBox<>();
    /** Contenedor de las opciones para mostrar las capas. */
    private final JPanel layerMenu = new JPanel();
    /** Mensaje sobre la ejecución del escenario o un fallo del banco. */
    private final JLabel status = new JLabel("Simulación lista");
    /** Rótulo que muestra marcador y tiempo actualizados. */
    private final JLabel score = new JLabel();
    /** Componente que dibuja la cancha y los datos de depuración. */
    private final GameCanvas canvas = new GameCanvas();
    /** Temporizador Swing que solicita cada actualización del banco. */
    private final Timer timer = new Timer(16, event -> tick());
    /** Teclas de acción presionadas que esperan su liberación. */
    private final Set<Integer> actionKeysDown = new HashSet<>();
    /** Indica si la simulación espera un reinicio tras una excepción. */
    private boolean updateFailed;
    /** Progreso restante de la animación de patada izquierda. */
    private double leftKickProgress;
    /** Progreso restante de la animación de patada derecha. */
    private double rightKickProgress;

    /** Construye el banco con control manual para los dos jugadores. */
    public MechanicsTestPanel() {
        this(null);
    }

    /** Construye el banco con el perfil seleccionado antes de abrir la sesión.
     * @param botProfile nombre simple del perfil disponible en {@link Game#getBotTypes()},
     *        o {@code null} para dos jugadores con control manual
     */
    public MechanicsTestPanel(String botProfile) {
        super(new BorderLayout(10, 10));
        controller = new MechanicsTestController(botProfile);
        setBackground(BACKGROUND);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        registerDefaultScenarios();
        registerDefaultLayers();
        add(buildToolbar(), BorderLayout.NORTH);
        add(canvas, BorderLayout.CENTER);
        add(buildSideMenu(), BorderLayout.EAST);
        bindKeys();
        refreshLabels();
    }

    /** Inicia la actualización periódica si la ronda no espera un reinicio.
     */
    public void start() {
        if (!updateFailed) {
            timer.start();
            canvas.requestFocusInWindow();
        }
    }

    /** Pausa la actualización periódica y libera los controles sostenidos. */
    public void stop() {
        timer.stop();
        releaseControls();
    }

    /** Libera los controles cuando Swing puede dejar de recibir eventos de teclado. */
    void releaseControls() {
        controller.releaseMovement();
        actionKeysDown.clear();
    }

    /** Conecta la barra con el escenario seleccionado, el reinicio de la ronda
     * y la alternancia del temporizador. Reiniciar también refresca el marcador
     * y solicita el repintado. */
    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 4));
        toolbar.setOpaque(false);
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        JButton runScenario = new JButton("Preparar prueba");
        // El clic ejecuta la acción registrada en el selector de escenarios.
        runScenario.addActionListener(event -> runSelectedScenario());
        JButton reset = new JButton("Reiniciar");
        reset.addActionListener(event -> {
            // Después del reinicio, actualiza los rótulos y repinta la cancha.
            resetRound();
            status.setText("Ronda reiniciada");
            refreshLabels();
            canvas.repaint();
        });
        JButton pause = new JButton("Pausar / continuar");
        pause.addActionListener(event -> {
            // El clic alterna el avance del banco sin reiniciar la ronda.
            if (timer.isRunning()) stop(); else start();
        });
        controls.add(scenarioMenu);
        controls.add(runScenario);
        controls.add(reset);
        controls.add(pause);
        toolbar.add(controls, BorderLayout.CENTER);
        score.setForeground(TEXT);
        score.setFont(score.getFont().deriveFont(Font.BOLD, 18f));
        toolbar.add(score, BorderLayout.EAST);
        status.setForeground(TEXT);
        toolbar.add(status, BorderLayout.SOUTH);
        return toolbar;
    }

    /** Crea una casilla por capa. Cada clic copia la selección al estado de la
     * capa y solicita el repintado del lienzo. */
    private JScrollPane buildSideMenu() {
        layerMenu.setOpaque(false);
        layerMenu.setLayout(new javax.swing.BoxLayout(layerMenu, javax.swing.BoxLayout.Y_AXIS));
        JLabel title = new JLabel("Elementos visibles");
        title.setForeground(TEXT);
        title.setFont(title.getFont().deriveFont(Font.BOLD));
        layerMenu.add(title);
        for (LayerOption layer : layers) {
            JCheckBox toggle = new JCheckBox(layer.label(), layer.enabled());
            toggle.setOpaque(false);
            toggle.setForeground(TEXT);
            toggle.addActionListener(event -> {
                // La casilla controla el dibujo de la capa y solicita otro repintado.
                layer.setEnabled(toggle.isSelected());
                canvas.repaint();
            });
            layerMenu.add(toggle);
        }
        String instructions = controller.hasBot()
                ? "<html>Humano: lado izquierdo<br>Bot: lado derecho<br>A / D: mover · F: patear"
                        + "<br>W: salto del humano</html>"
                : "<html>A / D: mover jugador izquierdo<br>← / →: mover jugador derecho<br>F / Enter: patear"
                        + "<br>W / ↑: salto izquierdo / derecho</html>";
        JLabel help = new JLabel(instructions);
        help.setForeground(TEXT);
        layerMenu.add(help);
        JScrollPane scroll = new JScrollPane(layerMenu);
        scroll.setPreferredSize(new Dimension(245, 100));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return scroll;
    }

    /** Registra las pruebas disponibles al abrir el banco. */
    private void registerDefaultScenarios() {
        addScenario(controller.matchLabel(), this::resetRound);
    }

    /** Añade una opción al menú de escenarios. Para extender el banco, registrar aquí una nueva acción.
     * @param label texto que aparece en el menú
     * @param action preparación de la prueba seleccionada
     */
    public void addScenario(String label, Runnable action) {
        TestScenario scenario = new TestScenario(label, action);
        scenarios.add(scenario);
        scenarioMenu.addItem(scenario);
    }

    /** Registra las capas de dibujo y sus estados iniciales. */
    private void registerDefaultLayers() {
        layers.add(new LayerOption("Hitbox completa de cancha", true));
        layers.add(new LayerOption("Hitboxes de arcos", true));
        layers.add(new LayerOption("Hitboxes de participantes", true));
        layers.add(new LayerOption("Hitboxes de balón", true));
        layers.add(new LayerOption("Vector del balón", true));
        layers.add(new LayerOption("Animación de patada", true));
        layers.add(new LayerOption("Vida", true));
        layers.add(new LayerOption("Barra de poder", true));
        layers.add(new LayerOption("Marcador y tiempo", true));
    }

    /** Ejecuta la preparación seleccionada y devuelve el foco a la cancha. */
    private void runSelectedScenario() {
        // El selector guarda juntos el nombre visible y la acción a ejecutar.
        TestScenario selected = (TestScenario) scenarioMenu.getSelectedItem();
        if (selected == null) return;
        try {
            selected.action().run();
            status.setText("Prueba: " + selected.label());
            refreshLabels();
            canvas.requestFocusInWindow();
            canvas.repaint();
        } catch (RuntimeException exception) {
            status.setText("No se pudo preparar: " + exception.getMessage());
        }
    }

    /** Solicita al controlador recolocar la ronda y limpia las teclas y las
     * animaciones de patada. Si un fallo había detenido el ciclo, lo reanuda. */
    private void resetRound() {
        controller.reset();
        actionKeysDown.clear();
        leftKickProgress = rightKickProgress = 0;
        if (updateFailed) {
            updateFailed = false;
            start();
        }
    }

    /** Solicita al controlador avanzar un paso, actualiza la animación y las
     * etiquetas y repinta el lienzo. Ante un error pausa la simulación y deja
     * el diagnóstico visible hasta reiniciar. */
    private void tick() {
        // El controlador aplica las teclas sostenidas antes de avanzar la simulación.
        try {
            controller.update();
        } catch (RuntimeException exception) {
            // Mantiene visible el estado que produjo el fallo para poder inspeccionarlo.
            stop();
            updateFailed = true;
            String detail = exception.getMessage() == null
                    ? exception.getClass().getSimpleName() : exception.getMessage();
            status.setText("Simulación detenida: " + detail + ". Pulsa Reiniciar.");
            canvas.repaint();
            return;
        }
        // El paso de juego ya terminó. Avanza la animación y vuelve a leer el marcador.
        leftKickProgress = Math.max(0, leftKickProgress - 0.08);
        rightKickProgress = Math.max(0, rightKickProgress - 0.08);
        refreshLabels();
        canvas.repaint();
    }

    /** Actualiza el marcador y el tiempo con las consultas de Game. */
    private void refreshLabels() {
        // El banco toma los goles y los segundos de su propia fachada de juego.
        Game game = controller.game();
        score.setText(game.getGoalsLeft() + "  :  " + game.getGoalsRight()
                + "     " + String.format("%02d:%02d", (int) game.getElapsedSeconds() / 60,
                        (int) game.getElapsedSeconds() % 60));
    }

    /** Asigna teclas a los lados que controla la persona. */
    private void bindKeys() {
        // A, D, W y F controlan el lado izquierdo en ambos modos del banco.
        bindControl(KeyEvent.VK_A, "left-player-left", false, false);
        bindControl(KeyEvent.VK_D, "left-player-right", false, true);
        bindJump(KeyEvent.VK_W, "left-jump", false);
        bindKick(KeyEvent.VK_F, "left-kick", false);
        // En modo manual se habilitan las teclas del participante derecho.
        if (!controller.hasBot()) {
            bindControl(KeyEvent.VK_LEFT, "right-player-left", true, false);
            bindControl(KeyEvent.VK_RIGHT, "right-player-right", true, true);
            bindJump(KeyEvent.VK_UP, "right-jump", true);
            bindKick(KeyEvent.VK_ENTER, "right-kick", true);
        }
    }

    /** Registra los eventos de una tecla direccional como estado sostenido. */
    private void bindControl(int keyCode, String name, boolean rightPlayer, boolean rightDirection) {
        // Presionar o soltar cambia el estado que lee el controlador en el siguiente paso.
        bindInput(keyCode, name, pressed -> controller.setMovementControl(rightPlayer, rightDirection, pressed));
    }

    /** Envía una solicitud de salto por pulsación mientras la simulación está activa. */
    private void bindJump(int keyCode, String name, boolean rightSide) {
        bindInput(keyCode, name, pressed -> {
            // El conjunto evita que la repetición del teclado dispare varios saltos.
            if (!pressed) actionKeysDown.remove(keyCode);
            else if (actionKeysDown.add(keyCode) && timer.isRunning()) controller.jump(rightSide);
        });
    }

    /** Envía solo una patada por pulsación, aunque el teclado repita el evento. */
    private void bindKick(int keyCode, String name, boolean rightSide) {
        bindInput(keyCode, name, pressed -> {
            // Una patada se vuelve a habilitar cuando se libera la tecla.
            if (pressed) {
                if (actionKeysDown.add(keyCode)) kick(rightSide);
            } else {
                actionKeysDown.remove(keyCode);
            }
        });
    }

    /** Registra presión y liberación en los mapas de Swing para toda la ventana
     * enfocada. El consumidor recibe {@code true} al presionar y
     * {@code false} al liberar. */
    private void bindInput(int keyCode, String name, Consumer<Boolean> onInput) {
        String pressedName = name + "-pressed";
        String releasedName = name + "-released";
        // Ambos eventos llegan al mismo consumidor, incluso si el foco está en otro control del banco.
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedName);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedName);
        getActionMap().put(pressedName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                onInput.accept(true);
            }
        });
        getActionMap().put(releasedName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                onInput.accept(false);
            }
        });
    }

    /** Inicia la animación de la pata y muestra si la patada alcanzó un objetivo. */
    private void kick(boolean rightSide) {
        // El resultado de Game decide el mensaje. La animación representa el intento.
        if (rightSide) rightKickProgress = 1; else leftKickProgress = 1;
        boolean hit = controller.kick(rightSide);
        status.setText(hit ? "Patada: contacto" : "Patada: sin contacto");
        canvas.repaint();
    }

    /** Recursos de cabeza y pata que componen un personaje seleccionable del banco. */
    private static final class CharacterStyle {
        private final String name;
        private final String headPath;
        private final String bodyPath;
        private final boolean bodyFacesRightAtSource;

        /** Crea la definición visual y sus rutas de recursos.
         * @param name nombre del estilo
         * @param headPath ruta de la cabeza
         * @param bodyPath ruta del cuerpo
         * @param bodyFacesRightAtSource orientación original del cuerpo
         */
        /** Registra las rutas y la orientación original del personaje. */
        private CharacterStyle(String name, String headPath, String bodyPath,
                boolean bodyFacesRightAtSource) {
            this.name = name;
            this.headPath = headPath;
            this.bodyPath = bodyPath;
            this.bodyFacesRightAtSource = bodyFacesRightAtSource;
        }

        String name() { return name; }
        String headPath() { return headPath; }
        String bodyPath() { return bodyPath; }
        boolean bodyFacesRightAtSource() { return bodyFacesRightAtSource; }
    }

    /** Imágenes cargadas y recortadas de un personaje. */
    private static final class CharacterSprite {
        private final BufferedImage head;
        private final BufferedImage body;
        private final boolean bodyFacesRightAtSource;

        /** Crea el par de imágenes recortadas del personaje.
         * @param head imagen de cabeza
         * @param body imagen del cuerpo
         * @param bodyFacesRightAtSource orientación original del cuerpo
         */
        /** Conserva las dos partes recortadas y la orientación original de la pata. */
        private CharacterSprite(BufferedImage head, BufferedImage body,
                boolean bodyFacesRightAtSource) {
            this.head = head;
            this.body = body;
            this.bodyFacesRightAtSource = bodyFacesRightAtSource;
        }

        BufferedImage head() { return head; }
        BufferedImage body() { return body; }
        boolean bodyFacesRightAtSource() { return bodyFacesRightAtSource; }
    }

    /** Conserva la selección visible de una capa y su control de menú. */
    private static final class LayerOption {
        private final String label;
        private boolean enabled;
        /** Crea una capa del menú con su estado inicial.
         * @param label texto visible
         * @param enabled estado inicial de visibilidad
         */
        LayerOption(String label, boolean enabled) {
            this.label = label;
            this.enabled = enabled;
        }
        /** Devuelve la etiqueta de la capa.
         * @return etiqueta visible
         */
        String label() { return label; }
        /** Indica si la capa está activa.
         * @return {@code true} si se dibuja
         */
        boolean enabled() { return enabled; }
        /** Cambia el estado de visibilidad.
         * @param enabled nuevo estado
         */
        void setEnabled(boolean enabled) { this.enabled = enabled; }
    }

    /** Representa una acción registrable desde el menú de escenarios. */
    private static final class TestScenario {
        private final String label;
        private final Runnable action;

        /** Crea una opción ejecutable del menú de escenarios.
         * @param label texto visible
         * @param action preparación que se ejecuta al seleccionar el escenario
         */
        /** Asocia la etiqueta visible del escenario con su acción de preparación. */
        private TestScenario(String label, Runnable action) {
            this.label = label;
            this.action = action;
        }

        String label() { return label; }
        Runnable action() { return action; }

        /** Devuelve el nombre que debe mostrar el selector.
         * @return etiqueta del escenario
         */
        @Override public String toString() { return label; }
    }

    /** Lee geometría y velocidades desde {@code controller.game()} en
     * {@code paintComponent}. El panel exterior solicita el repintado al
     * avanzar la simulación, reiniciar la ronda o cambiar una capa.
     */
    private final class GameCanvas extends JPanel {
        private final Image courtImage = loadImage("/images/ui/court@4x-8.png");
        private final Image ballImage = loadImage("/images/ball/ball@4x-8.png");
        private final Map<String, BufferedImage> trimmedSprites = new HashMap<>();
        private final CharacterSprite leftPlayer = loadCharacter(LEFT_CHARACTER);
        private final CharacterSprite rightPlayer = loadCharacter(RIGHT_CHARACTER);

        /** Prepara el lienzo y carga recursos gráficos del proyecto. */
        GameCanvas() {
            setFocusable(true);
            setBackground(new Color(25, 135, 95));
            setMinimumSize(new Dimension(500, 360));
            setBorder(BorderFactory.createLineBorder(new Color(226, 208, 156), 2));
        }

        /** Dibuja los recursos, entidades y capas seleccionadas según el estado escalar de Game.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            // El repintado consulta la geometría actual y la convierte a píxeles.
            Game game = controller.game();
            double courtX = game.getCourtX(), courtY = game.getCourtY();
            double courtWidth = game.getCourtWidth(), courtHeight = game.getCourtHeight();
            double margin = 22;
            double scale = Math.min((getWidth() - 2 * margin) / courtWidth,
                    (getHeight() - 2 * margin) / courtHeight);
            double originX = (getWidth() - courtWidth * scale) / 2 - courtX * scale;
            double originY = (getHeight() - courtHeight * scale) / 2 - courtY * scale;
            if (courtImage != null) g.drawImage(courtImage,
                    (int) (originX + courtX * scale), (int) (originY + courtY * scale),
                    (int) (courtWidth * scale), (int) (courtHeight * scale), this);
            for (LayerOption option : layers) {
                if (option.enabled() && !option.label().equals("Animación de patada")
                        && !option.label().equals("Hitboxes de participantes")
                        && !option.label().equals("Hitboxes de balón")) {
                    drawLayer(g, game, option.label(), originX, originY, scale);
                }
            }
            drawActors(g, game, originX, originY, scale);
            if (isLayerEnabled("Hitboxes de participantes")) drawPlayerHitboxes(g, game, originX, originY, scale);
            if (isLayerEnabled("Hitboxes de balón")) drawBallHitboxes(g, game, originX, originY, scale);
            drawFooter(g);
            g.dispose();
        }

        /** Despacha el dibujo de la capa seleccionada por su nombre. */
        private void drawLayer(Graphics2D g, Game game, String layer,
                double originX, double originY, double scale) {
            switch (layer) {
                case "Hitbox completa de cancha" -> outline(g, new Rectangle2D.Double(originX + game.getCourtX() * scale,
                        originY + game.getCourtY() * scale, game.getCourtWidth() * scale,
                        game.getCourtHeight() * scale), Color.YELLOW, 3);
                case "Hitboxes de arcos" -> drawGoals(g, game, originX, originY, scale);
                case "Vector del balón" -> drawBallVectors(g, game, originX, originY, scale);
                case "Vida" -> drawLife(g);
                case "Barra de poder" -> drawPower(g);
                case "Marcador y tiempo" -> drawHud(g, game);
                default -> { }
            }
        }

        /** Dibuja el rectángulo de cada arco con las medidas del dominio. */
        private void drawGoals(Graphics2D g, Game game, double ox, double oy, double scale) {
            g.setColor(new Color(255, 209, 60, 210));
            g.setStroke(new BasicStroke(3));
            for (int i = 0; i < 2; i++) g.draw(new Rectangle2D.Double(ox + game.getGoalX(i) * scale,
                    oy + game.getGoalY(i) * scale, game.getGoalWidth(i) * scale,
                    game.getGoalHeight(i) * scale));
        }

        /** Traza los rectángulos físicos de los participantes en colores por lado. */
        private void drawPlayerHitboxes(Graphics2D g, Game game, double ox, double oy, double scale) {
            for (boolean right : new boolean[] {false, true}) {
                Color color = right ? RIGHT_COLOR : LEFT_COLOR;
                boolean identity = controller.playerIdentity(right);
                double x = game.getPlayerX(identity), y = game.getPlayerY(identity);
                double width = game.getPlayerWidth(identity), height = game.getPlayerHeight(identity);
                outline(g, new Rectangle2D.Double(ox + x * scale, oy + y * scale,
                        width * scale, height * scale), color, 3);
            }
        }

        /** Traza un círculo con el radio real de cada balón. */
        private void drawBallHitboxes(Graphics2D g, Game game, double ox, double oy, double scale) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            for (int i = 0; i < game.getBallCount(); i++) {
                double radius = game.getBallRadius(i) * scale;
                g.draw(new Ellipse2D.Double(ox + game.getBallX(i) * scale - radius,
                        oy + game.getBallY(i) * scale - radius, radius * 2, radius * 2));
            }
        }

        /** Representa la dirección y la magnitud de la velocidad de cada balón. */
        private void drawBallVectors(Graphics2D g, Game game, double ox, double oy, double scale) {
            g.setColor(new Color(255, 42, 230));
            g.setStroke(new BasicStroke(3));
            for (int i = 0; i < game.getBallCount(); i++) {
                double x = ox + game.getBallX(i) * scale, y = oy + game.getBallY(i) * scale;
                double dx = game.getBallVelocityX(i) * scale * 0.18;
                double dy = game.getBallVelocityY(i) * scale * 0.18;
                g.draw(new Line2D.Double(x, y, x + dx, y + dy));
                g.fill(new Ellipse2D.Double(x + dx - 3, y + dy - 3, 6, 6));
            }
        }

        /** Posiciona los personajes y los balones usando sus coordenadas físicas. */
        private void drawActors(Graphics2D g, Game game, double ox, double oy, double scale) {
            for (boolean right : new boolean[] {false, true}) {
                boolean identity = controller.playerIdentity(right);
                double x = game.getPlayerX(identity), y = game.getPlayerY(identity);
                double width = game.getPlayerWidth(identity), height = game.getPlayerHeight(identity);
                double kickProgress = isLayerEnabled("Animación de patada")
                        ? right ? rightKickProgress : leftKickProgress : 0;
                drawCharacter(g, game, identity ? rightPlayer : leftPlayer, right,
                        x, y, width, height, kickProgress,
                        ox, oy, scale);
            }
            for (int i = 0; i < game.getBallCount(); i++) {
                double diameter = game.getBallRadius(i) * 2 * scale;
                int x = (int) (ox + (game.getBallX(i) - game.getBallRadius(i)) * scale);
                int y = (int) (oy + (game.getBallY(i) - game.getBallRadius(i)) * scale);
                if (ballImage != null) g.drawImage(ballImage, x, y, (int) diameter, (int) diameter, this);
                else { g.setColor(Color.WHITE); g.fill(new Ellipse2D.Double(x, y, diameter, diameter)); }
            }
        }

        /** Consulta si una capa registrada está visible en el menú. */
        private boolean isLayerEnabled(String layer) {
            return layers.stream().anyMatch(option -> option.label().equals(layer) && option.enabled());
        }

        /** Alinea la coronilla con el borde superior de la hitbox y la pata con su base. */
        private void drawCharacter(Graphics2D g, Game game, CharacterSprite sprite, boolean right,
                double x, double y, double width, double height, double kickProgress,
                double ox, double oy, double scale) {
            if (sprite == null || sprite.head() == null || sprite.body() == null) {
                g.setColor(right ? RIGHT_COLOR : LEFT_COLOR);
                g.fill(new Rectangle2D.Double(ox + x * scale, oy + y * scale,
                        width * scale, height * scale));
                return;
            }
            double courtHeight = game.getCourtHeight();
            int headHeight = Math.max(1, (int) (courtHeight * scale * CHARACTER_HEAD_HEIGHT_RATIO));
            int headWidth = Math.max(1, headHeight * sprite.head().getWidth() / sprite.head().getHeight());
            int bodyHeight = Math.max(1, (int) (courtHeight * scale * CHARACTER_BODY_HEIGHT_RATIO));
            int bodyWidth = Math.max(1, (int) (headWidth * CHARACTER_BODY_WIDTH_RATIO));
            double centerX = ox + (x + width / 2) * scale;
            double baselineY = oy + (y + height) * scale;
            int bodyX = (int) (centerX - bodyWidth / 2.0);
            int bodyY = (int) (baselineY - bodyHeight);
            int headX = (int) (centerX - headWidth / 2.0);
            int headY = (int) (oy + y * scale);
            boolean onLeftSide = !right;
            boolean flipBody = onLeftSide != sprite.bodyFacesRightAtSource();
            boolean flipHead = right;
            double kickSwing = kickProgress > 0 ? Math.sin((1 - kickProgress) * Math.PI) : 0;
            int kickOffset = (int) ((right ? -1 : 1) * bodyWidth * 1.4 * kickSwing);
            drawCharacterPart(g, sprite.body(), bodyX + kickOffset, bodyY,
                    bodyWidth, bodyHeight, flipBody);
            drawCharacterPart(g, sprite.head(), headX, headY, headWidth, headHeight, flipHead);
        }

        /** Pinta una de las partes y la refleja horizontalmente si corresponde. */
        private void drawCharacterPart(Graphics2D g, BufferedImage image,
                int x, int y, int width, int height, boolean flipHorizontal) {
            if (!flipHorizontal) {
                g.drawImage(image, x, y, width, height, this);
                return;
            }
            Graphics2D flipped = (Graphics2D) g.create();
            flipped.translate(x + width, y);
            flipped.scale(-1, 1);
            flipped.drawImage(image, 0, 0, width, height, this);
            flipped.dispose();
        }

        /** Carga las partes que corresponden al personaje elegido. */
        private CharacterSprite loadCharacter(CharacterStyle style) {
            return new CharacterSprite(loadTrimmedSprite(style.headPath()),
                    loadTrimmedSprite(style.bodyPath()), style.bodyFacesRightAtSource());
        }

        /** Recorta el margen transparente y reutiliza el resultado en cuadros posteriores. */
        private BufferedImage loadTrimmedSprite(String resourcePath) {
            if (trimmedSprites.containsKey(resourcePath)) return trimmedSprites.get(resourcePath);
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl == null) {
                trimmedSprites.put(resourcePath, null);
                return null;
            }
            try {
                BufferedImage source = ImageIO.read(imageUrl);
                if (source == null) return null;
                int minX = source.getWidth(), minY = source.getHeight(), maxX = -1, maxY = -1;
                for (int y = 0; y < source.getHeight(); y++) {
                    for (int x = 0; x < source.getWidth(); x++) {
                        if ((source.getRGB(x, y) >>> 24) != 0) {
                            minX = Math.min(minX, x);
                            minY = Math.min(minY, y);
                            maxX = Math.max(maxX, x);
                            maxY = Math.max(maxY, y);
                        }
                    }
                }
                BufferedImage result = maxX < minX || maxY < minY ? source
                        : source.getSubimage(minX, minY, maxX - minX + 1, maxY - minY + 1);
                trimmedSprites.put(resourcePath, result);
                return result;
            } catch (java.io.IOException exception) {
                trimmedSprites.put(resourcePath, null);
                return null;
            }
        }

        /** Pinta los indicadores reservados para la vida de ambos lados. */
        private void drawLife(Graphics2D g) {
            drawGauge(g, 16, 16, 180, "Vida: API no disponible", LEFT_COLOR);
            drawGauge(g, Math.max(205, getWidth() - 196), 16, 180,
                    "Vida: API no disponible", RIGHT_COLOR);
        }

        /** Pinta los indicadores reservados para la carga de poder. */
        private void drawPower(Graphics2D g) {
            drawGauge(g, 16, 46, 180, "Poder: no implementado", new Color(177, 110, 255));
            drawGauge(g, Math.max(205, getWidth() - 196), 46, 180,
                    "Poder: no implementado", new Color(177, 110, 255));
        }

        /** Dibuja el marco y el texto de un indicador de estado. */
        private void drawGauge(Graphics2D g, int x, int y, int width, String label, Color color) {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            g.setColor(Color.WHITE);
            g.drawString(label, x, y + 10);
            g.setColor(new Color(25, 25, 25, 190));
            g.fillRoundRect(x, y + 14, width, 10, 8, 8);
            g.setColor(color);
            g.drawRoundRect(x, y + 14, width, 10, 8, 8);
        }

        /** Superpone el tiempo y el marcador dentro de la cancha de pruebas. */
        private void drawHud(Graphics2D g, Game game) {
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
            g.drawString("Goles  " + game.getGoalsLeft() + " : " + game.getGoalsRight()
                    + "     Tiempo  " + String.format("%02d:%02d", (int) game.getElapsedSeconds() / 60,
                            (int) game.getElapsedSeconds() % 60),
                    Math.max(16, (getWidth() - 260) / 2), 26);
        }

        /** Identifica el origen de los contornos que muestra el banco. */
        private void drawFooter(Graphics2D g) {
            g.setColor(new Color(255, 255, 255, 210));
            g.setFont(g.getFont().deriveFont(11f));
            g.drawString("Contornos: hitboxes de Game.",
                    12, getHeight() - 8);
        }

        /** Traza un perímetro rectangular con el color y grosor indicados. */
        private void outline(Graphics2D g, Rectangle2D rectangle, Color color, float stroke) {
            g.setColor(color);
            g.setStroke(new BasicStroke(stroke));
            g.draw(rectangle);
        }

        /** Carga una imagen del classpath cuando el recurso está disponible. */
        private Image loadImage(String resource) {
            URL url = getClass().getResource(resource);
            return url == null ? null : new ImageIcon(url).getImage();
        }
    }
}
