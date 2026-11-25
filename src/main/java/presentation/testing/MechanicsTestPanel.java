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

/** Banco visual configurable para inspeccionar los datos de juego y las hitboxes. */
public final class MechanicsTestPanel extends JPanel {
    private static final Color BACKGROUND = new Color(25, 31, 40);
    private static final Color TEXT = new Color(238, 242, 246);
    private static final Color LEFT_COLOR = new Color(255, 100, 90);
    private static final Color RIGHT_COLOR = new Color(87, 200, 255);
    private static final double CHARACTER_HEAD_HEIGHT_RATIO = 0.16;
    private static final double CHARACTER_BODY_HEIGHT_RATIO = 0.09;
    private static final double CHARACTER_BODY_WIDTH_RATIO = 0.62;
    private static final double CHARACTER_HEAD_BODY_OVERLAP_RATIO = 0.18;
    private static final double CHARACTER_FLOOR_RATIO = 0.985;
    private static final CharacterStyle LEFT_CHARACTER = new CharacterStyle("Lobo",
            "/images/outfits/heads/Heads Scaled/wolf_referencia.png",
            "/images/outfits/shoes/PATAWOLF.png", true);
    private static final CharacterStyle RIGHT_CHARACTER = new CharacterStyle("Tigre",
            "/images/outfits/heads/Heads Scaled/tiger_escalado.png",
            "/images/outfits/shoes/tiger@4x-8.png", false);

    private final MechanicsTestController controller = new MechanicsTestController();
    private final List<TestScenario> scenarios = new ArrayList<>();
    private final List<LayerOption> layers = new ArrayList<>();
    private final JComboBox<TestScenario> scenarioMenu = new JComboBox<>();
    private final JPanel layerMenu = new JPanel();
    private final JLabel status = new JLabel("Simulación lista");
    private final JLabel score = new JLabel();
    private final GameCanvas canvas = new GameCanvas();
    private final Timer timer = new Timer(16, event -> tick());
    private final Set<Integer> kickKeysDown = new HashSet<>();
    private double leftKickProgress;
    private double rightKickProgress;

    /** Construye los menús, registra las capas iniciales y conecta los controles.
     */
    public MechanicsTestPanel() {
        super(new BorderLayout(10, 10));
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

    /** Inicia la actualización periódica de la sesión.
     */
    public void start() { timer.start(); }

    /** Detiene la actualización periódica de la sesión.
     */
    public void stop() { timer.stop(); }

    private JPanel buildToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout(8, 4));
        toolbar.setOpaque(false);
        JPanel controls = new JPanel();
        controls.setOpaque(false);
        JButton runScenario = new JButton("Preparar prueba");
        runScenario.addActionListener(event -> runSelectedScenario());
        JButton reset = new JButton("Reiniciar");
        reset.addActionListener(event -> {
            controller.reset();
            leftKickProgress = rightKickProgress = 0;
            status.setText("Ronda reiniciada");
            refreshLabels();
            canvas.repaint();
        });
        JButton pause = new JButton("Pausar / continuar");
        pause.addActionListener(event -> {
            if (timer.isRunning()) timer.stop(); else timer.start();
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
                layer.setEnabled(toggle.isSelected());
                canvas.repaint();
            });
            layerMenu.add(toggle);
        }
        JLabel help = new JLabel("<html>A / D: mover izquierda<br>← / →: mover derecha<br>F / Enter: patear</html>");
        help.setForeground(TEXT);
        layerMenu.add(help);
        JScrollPane scroll = new JScrollPane(layerMenu);
        scroll.setPreferredSize(new Dimension(245, 100));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return scroll;
    }

    private void registerDefaultScenarios() {
        addScenario("Partida libre", () -> controller.reset());
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

    private void runSelectedScenario() {
        TestScenario selected = (TestScenario) scenarioMenu.getSelectedItem();
        if (selected == null) return;
        try {
            selected.action().run();
            status.setText("Prueba: " + selected.label());
            refreshLabels();
            canvas.repaint();
        } catch (RuntimeException exception) {
            status.setText("No se pudo preparar: " + exception.getMessage());
        }
    }

    private void tick() {
        controller.update();
        leftKickProgress = Math.max(0, leftKickProgress - 0.08);
        rightKickProgress = Math.max(0, rightKickProgress - 0.08);
        refreshLabels();
        canvas.repaint();
    }

    private void refreshLabels() {
        Game game = controller.game();
        score.setText(game.getGoalsLeft() + "  :  " + game.getGoalsRight()
                + "     " + String.format("%02d:%02d", (int) game.getElapsedSeconds() / 60,
                        (int) game.getElapsedSeconds() % 60));
    }

    private void bindKeys() {
        bindControl(KeyEvent.VK_A, "left-player-left", false, false);
        bindControl(KeyEvent.VK_D, "left-player-right", false, true);
        bindControl(KeyEvent.VK_LEFT, "right-player-left", true, false);
        bindControl(KeyEvent.VK_RIGHT, "right-player-right", true, true);
        bindKick(KeyEvent.VK_F, "left-kick", false);
        bindKick(KeyEvent.VK_ENTER, "right-kick", true);
    }

    private void bindControl(int keyCode, String name, boolean rightPlayer, boolean rightDirection) {
        String pressedName = name + "-pressed";
        String releasedName = name + "-released";
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedName);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedName);
        getActionMap().put(pressedName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                controller.setMovementControl(rightPlayer, rightDirection, true);
            }
        });
        getActionMap().put(releasedName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                controller.setMovementControl(rightPlayer, rightDirection, false);
            }
        });
    }

    private void bindKick(int keyCode, String name, boolean rightSide) {
        String pressedName = name + "-pressed";
        String releasedName = name + "-released";
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), pressedName);
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), releasedName);
        getActionMap().put(pressedName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                if (kickKeysDown.add(keyCode)) kick(rightSide);
            }
        });
        getActionMap().put(releasedName, new AbstractAction() {
            @Override public void actionPerformed(java.awt.event.ActionEvent event) {
                kickKeysDown.remove(keyCode);
            }
        });
    }

    private void kick(boolean rightSide) {
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

    /** Lienzo que representa cancha, entidades, hitboxes y capas opcionales.
     */
    private final class GameCanvas extends JPanel {
        private final Image courtImage = loadImage("/images/ui/court@4x-8.png");
        private final Image ballImage = loadImage("/images/ball/ball@4x-8.png");
        private final Map<String, BufferedImage> trimmedSprites = new HashMap<>();
        private final CharacterSprite leftPlayer = loadCharacter(LEFT_CHARACTER);
        private final CharacterSprite rightPlayer = loadCharacter(RIGHT_CHARACTER);

        /** Prepara el lienzo y carga recursos gráficos del proyecto. */
        GameCanvas() {
            setBackground(new Color(25, 135, 95));
            setMinimumSize(new Dimension(500, 360));
            setBorder(BorderFactory.createLineBorder(new Color(226, 208, 156), 2));
        }

        /** Dibuja los recursos, entidades y capas seleccionadas según el estado escalar de Game.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
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

        private void drawGoals(Graphics2D g, Game game, double ox, double oy, double scale) {
            g.setColor(new Color(255, 209, 60, 210));
            g.setStroke(new BasicStroke(3));
            for (int i = 0; i < 2; i++) g.draw(new Rectangle2D.Double(ox + game.getGoalX(i) * scale,
                    oy + game.getGoalY(i) * scale, game.getGoalWidth(i) * scale,
                    game.getGoalHeight(i) * scale));
        }

        private void drawPlayerHitboxes(Graphics2D g, Game game, double ox, double oy, double scale) {
            for (boolean right : new boolean[] {false, true}) {
                Color color = right ? RIGHT_COLOR : LEFT_COLOR;
                double x = game.getPlayerX(right), y = game.getPlayerY(right);
                double width = game.getPlayerWidth(right), height = game.getPlayerHeight(right);
                outline(g, new Rectangle2D.Double(ox + x * scale, oy + y * scale,
                        width * scale, height * scale), color, 3);
            }
        }

        private void drawBallHitboxes(Graphics2D g, Game game, double ox, double oy, double scale) {
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2));
            for (int i = 0; i < game.getBallCount(); i++) {
                double radius = game.getBallRadius(i) * scale;
                g.draw(new Ellipse2D.Double(ox + game.getBallX(i) * scale - radius,
                        oy + game.getBallY(i) * scale - radius, radius * 2, radius * 2));
            }
        }

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

        private void drawActors(Graphics2D g, Game game, double ox, double oy, double scale) {
            for (boolean right : new boolean[] {false, true}) {
                double x = game.getPlayerX(right), y = game.getPlayerY(right);
                double width = game.getPlayerWidth(right), height = game.getPlayerHeight(right);
                double kickProgress = isLayerEnabled("Animación de patada")
                        ? right ? rightKickProgress : leftKickProgress : 0;
                drawCharacter(g, game, right ? rightPlayer : leftPlayer, right,
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

        private boolean isLayerEnabled(String layer) {
            return layers.stream().anyMatch(option -> option.label().equals(layer) && option.enabled());
        }

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
            int headY = bodyY - headHeight + (int) (headHeight * CHARACTER_HEAD_BODY_OVERLAP_RATIO);
            boolean onLeftSide = !right;
            boolean flipBody = onLeftSide != sprite.bodyFacesRightAtSource();
            boolean flipHead = right;
            double kickSwing = kickProgress > 0 ? Math.sin((1 - kickProgress) * Math.PI) : 0;
            int kickOffset = (int) ((right ? -1 : 1) * bodyWidth * 1.4 * kickSwing);
            drawCharacterPart(g, sprite.body(), bodyX + kickOffset, bodyY,
                    bodyWidth, bodyHeight, flipBody);
            drawCharacterPart(g, sprite.head(), headX, headY, headWidth, headHeight, flipHead);
        }

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

        private CharacterSprite loadCharacter(CharacterStyle style) {
            return new CharacterSprite(loadTrimmedSprite(style.headPath()),
                    loadTrimmedSprite(style.bodyPath()), style.bodyFacesRightAtSource());
        }

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

        private void drawLife(Graphics2D g) {
            drawGauge(g, 16, 16, 180, "Vida: API no disponible", LEFT_COLOR);
            drawGauge(g, Math.max(205, getWidth() - 196), 16, 180,
                    "Vida: API no disponible", RIGHT_COLOR);
        }

        private void drawPower(Graphics2D g) {
            drawGauge(g, 16, 46, 180, "Poder: no implementado", new Color(177, 110, 255));
            drawGauge(g, Math.max(205, getWidth() - 196), 46, 180,
                    "Poder: no implementado", new Color(177, 110, 255));
        }

        private void drawGauge(Graphics2D g, int x, int y, int width, String label, Color color) {
            g.setFont(g.getFont().deriveFont(Font.BOLD, 11f));
            g.setColor(Color.WHITE);
            g.drawString(label, x, y + 10);
            g.setColor(new Color(25, 25, 25, 190));
            g.fillRoundRect(x, y + 14, width, 10, 8, 8);
            g.setColor(color);
            g.drawRoundRect(x, y + 14, width, 10, 8, 8);
        }

        private void drawHud(Graphics2D g, Game game) {
            g.setColor(Color.WHITE);
            g.setFont(g.getFont().deriveFont(Font.BOLD, 15f));
            g.drawString("Goles  " + game.getGoalsLeft() + " : " + game.getGoalsRight()
                    + "     Tiempo  " + String.format("%02d:%02d", (int) game.getElapsedSeconds() / 60,
                            (int) game.getElapsedSeconds() % 60),
                    Math.max(16, (getWidth() - 260) / 2), 26);
        }

        private void drawFooter(Graphics2D g) {
            g.setColor(new Color(255, 255, 255, 210));
            g.setFont(g.getFont().deriveFont(11f));
            g.drawString("Los colores y contornos representan las hitboxes que entrega Game.", 12, getHeight() - 8);
        }

        private void outline(Graphics2D g, Rectangle2D rectangle, Color color, float stroke) {
            g.setColor(color);
            g.setStroke(new BasicStroke(stroke));
            g.draw(rectangle);
        }

        private Image loadImage(String resource) {
            URL url = getClass().getResource(resource);
            return url == null ? null : new ImageIcon(url).getImage();
        }
    }
}
