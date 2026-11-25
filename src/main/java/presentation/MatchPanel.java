package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import domain.Game;

/** Vista del partido: HUD y dibujo de la cancha desde la instantánea de Game. */
public class MatchPanel extends BasePanel {
    private static final String COURT_IMAGE = "/images/ui/court@4x-8.png";
    private static final String BALL_IMAGE = "/images/ball/ball@4x-8.png";
    private static final String LEFT_STATUS_IMAGE = "/images/ui/jugador_izquierdo.png";
    private static final String RIGHT_STATUS_IMAGE = "/images/ui/jugador_derecho.png";
    private static final String SCOREBOARD_IMAGE = "/images/ui/scoreboard@4x-8.png";
    private static final String LOGO_IMAGE = "/images/ui/logo@4x-8.png";
    private static final int FIELD_MARGIN = 24;
    private static final int HUD_HEIGHT = 144;
    private static final double CHARACTER_HEAD_HEIGHT_RATIO = 0.16;
    private static final double CHARACTER_BODY_HEIGHT_RATIO = 0.09;
    private static final double CHARACTER_BODY_WIDTH_RATIO = 0.62;
    private static final double CHARACTER_HEAD_BODY_OVERLAP_RATIO = 0.18;
    private static final double CHARACTER_FLOOR_RATIO = 0.985;
    private static final double PLAY_AREA_TOP_RATIO = 0.68;
    private static final double PLAY_AREA_HEIGHT_RATIO = 0.31;
    private static final double BALL_VISUAL_DIAMETER_RATIO = 0.075;

    private final Game game;
    private final NavigationController navigationController;
    private final Runnable matchFinishedHandler;
    private final Map<String, CharacterSprite> characterSprites = new HashMap<>();

    private JLabel leftScoreLabel;
    private JLabel rightScoreLabel;
    private JLabel elapsedLabel;
    private JLabel rightParticipantLabel;
    private JButton pauseButton;
    private CourtView courtView;

    /** Crea la vista conectada a la fachada y a la navegación de presentación.
     * @param game fachada que entrega el estado del partido
     * @param navigationController controlador para cambiar de pantalla
     */
    public MatchPanel(Game game, NavigationController navigationController) {
        this(game, navigationController, () -> { });
    }

    /** Crea la vista e instala la acción que se ejecuta cuando la sesión termina.
     * @param game fachada que entrega el estado del partido
     * @param navigationController controlador para cambiar de pantalla
     * @param matchFinishedHandler acción de presentación para mostrar el resultado final
     */
    public MatchPanel(Game game, NavigationController navigationController,
            Runnable matchFinishedHandler) {
        this.game = game;
        this.navigationController = navigationController;
        this.matchFinishedHandler = matchFinishedHandler;
        prepareView();
        prepareController();
    }

    /** Construye el HUD y la cancha en secciones separadas. */
    @Override
    public void prepareView() {
        setLayout(new BorderLayout(12, 10));
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        setBackground(Theme.BACKGROUND);
        prepareViewCourt();
    }

    /** Ubica marcador, tiempo y las barras de vida y poder de ambos lados. */
    private JPanel prepareViewHud() {
        JPanel hud = new JPanel(new BorderLayout(10, 8));
        hud.setOpaque(false);
        hud.add(prepareViewPlayerStatus(true), BorderLayout.WEST);
        hud.add(prepareViewScore(), BorderLayout.CENTER);
        hud.add(prepareViewPlayerStatus(false), BorderLayout.EAST);
        hud.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        return hud;
    }

    /** Crea el grupo de barras correspondiente al lado indicado. */
    private JPanel prepareViewPlayerStatus(boolean left) {
        JPanel status = new JPanel(new BorderLayout(8, 2));
        status.setOpaque(false);
        status.setPreferredSize(new Dimension(190, HUD_HEIGHT - 8));

        JLabel animal = new JLabel(left ? "JUGADOR 1" : "JUGADOR 2", SwingConstants.CENTER);
        if (!left) {
            rightParticipantLabel = animal;
        }
        animal.setFont(new Font("SansSerif", Font.BOLD, 12));
        animal.setForeground(Theme.DARK_GREEN);
        status.add(animal, BorderLayout.NORTH);

        Image statusImage = loadImage(left ? LEFT_STATUS_IMAGE : RIGHT_STATUS_IMAGE);
        status.add(new StatusArtwork(statusImage), BorderLayout.CENTER);

        return status;
    }

    /** Inicializa marcador y tiempo sobre el arte del tablero de puntuación. */
    private JPanel prepareViewScore() {
        JPanel scoreArea = new JPanel(new BorderLayout(0, 3));
        scoreArea.setOpaque(false);
        ScoreboardArtwork score = new ScoreboardArtwork();
        leftScoreLabel = createScoreLabel();
        rightScoreLabel = createScoreLabel();
        elapsedLabel = new JLabel("00:00", SwingConstants.CENTER);
        elapsedLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        elapsedLabel.setForeground(Color.WHITE);
        score.add(leftScoreLabel);
        score.add(rightScoreLabel);
        score.add(elapsedLabel);
        scoreArea.add(score, BorderLayout.CENTER);

        pauseButton = new RoundedButton("Pausa", Theme.GREEN, Color.WHITE,
                Theme.SAND_BORDER, 12);
        pauseButton.setIcon(loadIcon(LOGO_IMAGE, 22, 22));
        pauseButton.setIconTextGap(8);
        pauseButton.setToolTipText("Abrir menú de pausa");
        pauseButton.setPreferredSize(new Dimension(150, 32));
        scoreArea.add(pauseButton, BorderLayout.SOUTH);
        return scoreArea;
    }

    /** Crea una cifra de marcador que se ubicará dentro del panel de cada lado. */
    private JLabel createScoreLabel() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(Color.WHITE);
        return label;
    }

    /** Dibuja la composición del marcador por encima de un fondo transparente. */
    private final class ScoreboardArtwork extends JPanel {
        private final Image image = loadImage(SCOREBOARD_IMAGE);

        private ScoreboardArtwork() {
            setOpaque(false);
            setLayout(null);
            setPreferredSize(new Dimension(300, 96));
        }

        @Override
        public void doLayout() {
            int width = getWidth();
            int height = getHeight();
            leftScoreLabel.setBounds((int) (width * 0.12), (int) (height * 0.38),
                    (int) (width * 0.18), (int) (height * 0.36));
            elapsedLabel.setBounds((int) (width * 0.39), (int) (height * 0.42),
                    (int) (width * 0.22), (int) (height * 0.3));
            rightScoreLabel.setBounds((int) (width * 0.70), (int) (height * 0.38),
                    (int) (width * 0.18), (int) (height * 0.36));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(),
                        2000, 1500, 5600, 3200, this);
            }
        }
    }

    /** Presenta las ilustraciones decorativas de vida y poder del HUD. */
    private static final class StatusArtwork extends JPanel {
        private final Image image;

        private StatusArtwork(Image image) {
            this.image = image;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
        }
    }

    /** Agrega la cancha y superpone el HUD en su franja superior. */
    private void prepareViewCourt() {
        courtView = new CourtView();
        courtView.setHud(prepareViewHud());
        add(courtView, BorderLayout.CENTER);
    }

    /** Este panel no registra acciones mientras el movimiento del partido está pendiente. */
    @Override
    public void prepareController() {
        prepareControllerPause();
    }

    /** Abre la pantalla de pausa desde el botón ubicado bajo el marcador. */
    private void prepareControllerPause() {
        pauseButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.PAUSE));
    }

    /** Asigna las imágenes seleccionadas a los lados de la cancha.
     * @param leftImage ruta de la imagen para el lado izquierdo
     * @param rightImage ruta de la imagen para el lado derecho
     */
    public void setCharacterImages(String leftImage, String rightImage) {
        characterSprites.clear();
        characterSprites.put("left", courtView.loadCharacterSprite(leftImage));
        characterSprites.put("right", courtView.loadCharacterSprite(rightImage));
        if (courtView != null) {
            courtView.repaint();
        }
    }

    /** Actualiza el nombre del lado derecho según la modalidad seleccionada.
     * @param againstBot indica si ese lado está controlado por un bot
     */
    public void setAgainstBot(boolean againstBot) {
        rightParticipantLabel.setText(againstBot ? "MÁQUINA" : "JUGADOR 2");
    }

    /** Refresca marcador, tiempo y geometría una vez que Game tiene una sesión. */
    public void refreshMatchView() {
        leftScoreLabel.setText(Integer.toString(game.getGoalsRight()));
        rightScoreLabel.setText(Integer.toString(game.getGoalsLeft()));
        int elapsed = (int) game.getElapsedSeconds();
        elapsedLabel.setText(String.format("%02d:%02d", elapsed / 60, elapsed % 60));
        courtView.setGame(game);
        if (game.isFinished()) {
            matchFinishedHandler.run();
        }
    }

    /** Carga un recurso de imagen del classpath o devuelve null si no existe. */
    private Image loadImage(String resourcePath) {
        if (resourcePath == null) {
            return null;
        }
        URL url = getClass().getResource(resourcePath);
        return url == null ? null : new ImageIcon(url).getImage();
    }

    /** Carga el logo con un tamaño adecuado para un control de la interfaz. */
    private ImageIcon loadIcon(String resourcePath, int width, int height) {
        Image image = loadImage(resourcePath);
        if (image == null) {
            return null;
        }
        return new ImageIcon(image.getScaledInstance(width, height, Image.SCALE_SMOOTH));
    }

    /** Agrupa las imágenes visuales recortadas de cabeza y cuerpo de un animal.
     * @param head imagen visual recortada de la cabeza
     * @param body imagen visual recortada de las patas o cuerpo
     * @param bodyFacesRightAtSource indica si las patas originales apuntan a la derecha
     */
    private static final class CharacterSprite {
        private final BufferedImage head;
        private final BufferedImage body;
        private final boolean bodyFacesRightAtSource;

        private CharacterSprite(BufferedImage head, BufferedImage body, boolean bodyFacesRightAtSource) {
            this.head = head;
            this.body = body;
            this.bodyFacesRightAtSource = bodyFacesRightAtSource;
        }

        private BufferedImage head() { return head; }
        private BufferedImage body() { return body; }
        private boolean bodyFacesRightAtSource() { return bodyFacesRightAtSource; }
    }

    /** Dibuja la cancha, los personajes decorativos y el balón de la instantánea. */
    private final class CourtView extends JPanel {
        private Game game;
        private JPanel hud;
        private final Map<String, BufferedImage> trimmedSprites = new HashMap<>();
        private final Image courtImage = loadImage(COURT_IMAGE);
        private final Image ballImage = loadImage(BALL_IMAGE);

        private CourtView() {
            setBackground(new Color(0x29A8DB));
            setBorder(BorderFactory.createLineBorder(Theme.SAND_BORDER, 2));
            setMinimumSize(new Dimension(320, 220));
            setLayout(null);
        }

        private void setGame(Game game) {
            this.game = game;
            revalidate();
            repaint();
        }

        /** Agrega el HUD como componente superpuesto en la parte superior. */
        private void setHud(JPanel hud) {
            this.hud = hud;
            add(hud);
        }

        @Override
        public void doLayout() {
            if (hud == null) {
                return;
            }
            hud.setBounds(0, 0, getWidth(), Math.min(HUD_HEIGHT, getHeight()));
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (game == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double courtX = game.getCourtX(), courtY = game.getCourtY();
            double courtWidth = game.getCourtWidth(), courtHeight = game.getCourtHeight();
            double scale = courtScale(courtWidth, courtHeight);
            double drawWidth = courtWidth * scale;
            double drawHeight = courtHeight * scale;
            double offsetX = (getWidth() - drawWidth) / 2.0;
            double offsetY = getHeight() - drawHeight;

            drawCourtImage(g, offsetX, offsetY, drawWidth, drawHeight);
            drawGoals(g, courtX, courtY, offsetX, offsetY, scale);
            drawDecorativeCharacters(g, courtHeight, courtWidth, offsetX, offsetY, scale);
            drawBalls(g, courtX, courtY, offsetX, offsetY, scale);
            g.dispose();
        }

        /** Pinta el recurso completo de la cancha dentro de los límites del juego. */
        private void drawCourtImage(Graphics2D g, double x, double y, double width, double height) {
            if (courtImage != null) {
                g.drawImage(courtImage, (int) x, (int) y, (int) width, (int) height, this);
            }
        }

        /** Dibuja los arcos en las posiciones entregadas por la fachada Game.
         * @param g contexto gráfico de la cancha
         * @param goals geometría de los arcos
         * @param court rectángulo exterior de la cancha
         * @param offsetX desplazamiento horizontal del campo dibujado
         * @param offsetY desplazamiento vertical del campo dibujado
         * @param scale escala aplicada a las coordenadas del dominio
         */
        private void drawGoals(Graphics2D g, double courtX, double courtY,
                double offsetX, double offsetY, double scale) {
            g.setColor(new Color(255, 211, 67, 190));
            for (int index = 0; index < 2; index++) {
                int x = (int) (offsetX + (game.getGoalX(index) - courtX) * scale);
                int y = (int) (offsetY + (game.getGoalY(index) - courtY) * scale);
                int width = (int) (game.getGoalWidth(index) * scale);
                int height = (int) (game.getGoalHeight(index) * scale);
                g.drawRect(x, y, width, height);
            }
        }

        /** Dibuja los animales como decoración, sin leer ni crear hitboxes. */
        private void drawDecorativeCharacters(Graphics2D g, double courtHeight, double courtWidth,
                double offsetX, double offsetY, double scale) {
            drawDecorativeCharacter(g, characterSprites.get("left"), courtHeight, courtWidth,
                    offsetX, offsetY, scale, 0.25);
            drawDecorativeCharacter(g, characterSprites.get("right"), courtHeight, courtWidth,
                    offsetX, offsetY, scale, 0.75);
        }

        /** Compone la cabeza estandarizada y sus patas dentro del área visual del personaje. */
        private void drawDecorativeCharacter(Graphics2D g, CharacterSprite sprite,
                double courtHeight, double courtWidth,
                double offsetX, double offsetY, double scale, double horizontalRatio) {
            if (sprite == null || sprite.head() == null || sprite.body() == null) {
                return;
            }

            int headHeight = Math.max(1,
                    (int) (courtHeight * scale * CHARACTER_HEAD_HEIGHT_RATIO));
            int headWidth = Math.max(1, (int) (headHeight
                    * sprite.head().getWidth() / (double) sprite.head().getHeight()));
            int bodyHeight = Math.max(1,
                    (int) (courtHeight * scale * CHARACTER_BODY_HEIGHT_RATIO));
            int bodyWidth = Math.max(1, (int) (headWidth * CHARACTER_BODY_WIDTH_RATIO));
            double centerX = offsetX + courtWidth * horizontalRatio * scale;
            double baselineY = offsetY + courtHeight * scale * CHARACTER_FLOOR_RATIO;
            int bodyX = (int) (centerX - bodyWidth / 2.0);
            int bodyY = (int) (baselineY - bodyHeight);
            int headX = (int) (centerX - headWidth / 2.0);
            int headY = bodyY - headHeight
                    + (int) (headHeight * CHARACTER_HEAD_BODY_OVERLAP_RATIO);
            boolean onLeftSide = horizontalRatio < 0.5;
            boolean flipBody = onLeftSide != sprite.bodyFacesRightAtSource();
            boolean flipHead = horizontalRatio > 0.5;

            drawCharacterPart(g, sprite.body(), bodyX, bodyY, bodyWidth, bodyHeight, flipBody);
            drawCharacterPart(g, sprite.head(), headX, headY, headWidth, headHeight, flipHead);
        }

        /** Dibuja una parte del personaje y la refleja cuando mira hacia la izquierda.
         * @param g contexto gráfico de la cancha
         * @param image sprite recortado que se va a dibujar
         * @param x coordenada horizontal de destino
         * @param y coordenada vertical de destino
         * @param width ancho de destino
         * @param height alto de destino
         * @param flipHorizontal indica si debe reflejarse horizontalmente
         */
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

        /** Carga el par de sprites escalados según la especie indicada por la configuración. */
        private CharacterSprite loadCharacterSprite(String configuredHeadPath) {
            String name = configuredHeadPath == null ? ""
                    : configuredHeadPath.substring(configuredHeadPath.lastIndexOf('/') + 1)
                            .toLowerCase(Locale.ROOT);
            String headPath;
            String bodyPath;
            if (name.contains("tiger")) {
                headPath = "/images/outfits/heads/Heads Scaled/tiger_escalado.png";
                bodyPath = "/images/outfits/shoes/tiger@4x-8.png";
            } else if (name.contains("leopard")) {
                headPath = "/images/outfits/heads/Heads Scaled/leopard_escalado.png";
                bodyPath = "/images/outfits/shoes/leopard@4x-8.png";
            } else if (name.contains("bear")) {
                headPath = "/images/outfits/heads/Heads Scaled/bear_escalado.png";
                bodyPath = "/images/outfits/shoes/bear@4x-8.png";
            } else if (name.contains("wolf")) {
                headPath = "/images/outfits/heads/Heads Scaled/wolf_referencia.png";
                bodyPath = "/images/outfits/shoes/PATAWOLF.png";
            } else {
                return null;
            }
            boolean bodyFacesRightAtSource = name.contains("wolf");
            return new CharacterSprite(loadTrimmedSprite(headPath), loadTrimmedSprite(bodyPath),
                    bodyFacesRightAtSource);
        }

        /** Recorta solo el espacio transparente para alinear y escalar el sprite visual. */
        private BufferedImage loadTrimmedSprite(String resourcePath) {
            if (trimmedSprites.containsKey(resourcePath)) {
                return trimmedSprites.get(resourcePath);
            }
            URL imageUrl = getClass().getResource(resourcePath);
            if (imageUrl == null) {
                trimmedSprites.put(resourcePath, null);
                return null;
            }
            try {
                BufferedImage source = ImageIO.read(imageUrl);
                if (source == null) {
                    trimmedSprites.put(resourcePath, null);
                    return null;
                }
                int minX = source.getWidth();
                int minY = source.getHeight();
                int maxX = -1;
                int maxY = -1;
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
                if (maxX < minX || maxY < minY) {
                    trimmedSprites.put(resourcePath, source);
                    return source;
                }
                BufferedImage trimmed = source.getSubimage(minX, minY,
                        maxX - minX + 1, maxY - minY + 1);
                trimmedSprites.put(resourcePath, trimmed);
                return trimmed;
            } catch (IOException exception) {
                trimmedSprites.put(resourcePath, null);
                return null;
            }
        }

        /** Dibuja cada balón en las coordenadas y con el radio reportados por Game. */
        private void drawBalls(Graphics2D g, double courtX, double courtY,
                double offsetX, double offsetY, double scale) {
            double courtHeight = game.getCourtHeight();
            for (int index = 0; index < game.getBallCount(); index++) {
                double radius = game.getBallRadius(index);
                double diameter = Math.max(radius * 2 * scale,
                        courtHeight * scale * BALL_VISUAL_DIAMETER_RATIO);
                double x = offsetX + (game.getBallX(index) - courtX) * scale - diameter / 2.0;
                double normalizedY = (game.getBallY(index) - courtY) / courtHeight;
                double visualYRatio = PLAY_AREA_TOP_RATIO
                        + normalizedY * PLAY_AREA_HEIGHT_RATIO;
                double y = offsetY + visualYRatio * courtHeight * scale - diameter / 2.0;
                if (ballImage != null) {
                    g.drawImage(ballImage, (int) x, (int) y, (int) diameter, (int) diameter, this);
                } else {
                    g.setColor(Color.WHITE);
                    g.fill(new Ellipse2D.Double(x, y, diameter, diameter));
                    g.setColor(Theme.DARK_GREEN);
                    g.draw(new Ellipse2D.Double(x, y, diameter, diameter));
                }
            }
        }

        /** Calcula la escala que mantiene la proporción del campo en el área disponible. */
        private double courtScale(double courtWidth, double courtHeight) {
            return Math.min((getWidth() - FIELD_MARGIN * 2.0) / courtWidth,
                    getHeight() / courtHeight);
        }

    }
}
