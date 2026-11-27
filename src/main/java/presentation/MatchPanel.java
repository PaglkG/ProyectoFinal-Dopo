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
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import presentation.DOPOSoccerGUI.PresentationState;
import presentation.MatchSnapshot.Ball;
import presentation.MatchSnapshot.Bounds;
import presentation.MatchSnapshot.Participant;

/**
 * Vista del partido con HUD y dibujo de la cancha.
 *
 * <ul>
 *   <li><strong>Recepción:</strong> {@link #refreshMatchView()} lee
 *       {@code PresentationState.getMatch()} al entrar a la cancha y después
 *       de cada paso publicado por la ventana.</li>
 *   <li><strong>Modificación:</strong> {@code prepareControllerPause()} vincula
 *       el botón de pausa con la navegación. El temporizador de
 *       {@link DOPOSoccerGUI} avanza el estado del partido.</li>
 *   <li><strong>Actualización:</strong> {@code showMatch()} aplica la
 *       configuración, el marcador, el tiempo y la muerte súbita.
 *       Las ilustraciones de jugador conservan las barras sencillas del HUD.
 *       {@code CourtView.setMatch()} entrega esa misma captura al repintado
 *       de la cancha, los arcos, los personajes y los balones.</li>
 * </ul>
 */
public class MatchPanel extends BasePanel {
    private static final String COURT_IMAGE = "/images/ui/court@4x-8.png";
    private static final String BALL_IMAGE = "/images/ball/ball@4x-8.png";
    private static final String LEFT_STATUS_IMAGE = "/images/ui/jugador_izquierdo.png";
    private static final String RIGHT_STATUS_IMAGE = "/images/ui/jugador_derecho.png";
    private static final String POWER_IMAGE_FOLDER = "/images/powers/";
    private static final String SCOREBOARD_IMAGE = "/images/ui/scoreboard@4x-8.png";
    private static final String LOGO_IMAGE = "/images/ui/logo@4x-8.png";
    private static final int FIELD_MARGIN = 24;
    private static final int HUD_HEIGHT = 144;
    private static final double CHARACTER_HEAD_HEIGHT_RATIO = 0.16;
    private static final double CHARACTER_BODY_HEIGHT_RATIO = 0.09;
    private static final double CHARACTER_BODY_WIDTH_RATIO = 0.62;
    private static final double CHARACTER_HEAD_BODY_OVERLAP_RATIO = 0.18;
    private static final double PLAY_AREA_TOP_RATIO = 0.68;
    private static final double PLAY_AREA_HEIGHT_RATIO = 0.31;
    private static final double BALL_VISUAL_DIAMETER_RATIO = 0.075;
    private static final double POWER_AURA_HEIGHT_RATIO = 1.4;

    /** Controlador que coordina los cambios de pantalla. */
    private final NavigationController navigationController;
    /** Configuración que ya se aplicó a los sprites y rótulos. */
    private MatchConfiguration displayedConfiguration;
    /** Recursos gráficos de los personajes por lado de juego. */
    private final Map<String, CharacterSprite> characterSprites = new HashMap<>();

    /** Marcador visible del lado izquierdo. */
    private JLabel leftScoreLabel;
    /** Marcador visible del lado derecho. */
    private JLabel rightScoreLabel;
    /** Texto que muestra el tiempo transcurrido. */
    private JLabel elapsedLabel;
    /** Rótulo que identifica al participante del lado derecho. */
    private JLabel rightParticipantLabel;
    /** Rótulo que identifica al participante del lado izquierdo. */
    private JLabel leftParticipantLabel;
    /** Acción que abre la pantalla de pausa. */
    private JButton pauseButton;
    /** Componente que dibuja la cancha y los participantes. */
    private CourtView courtView;

    /** Crea la vista del partido.
     * @param navigationController controlador para cambiar de pantalla
     */
    public MatchPanel(NavigationController navigationController) {
        // Construye los componentes antes de registrar el clic del botón Pausa.
        this.navigationController = navigationController;
        prepareView();
        prepareController();
    }

    /** Construye el HUD y la cancha en secciones separadas. */
    @Override
    public void prepareView() {
        // El lienzo ocupa el centro y contiene el HUD como una capa superpuesta.
        setLayout(new BorderLayout(12, 10));
        setBorder(BorderFactory.createEmptyBorder(0, 18, 0, 18));
        setBackground(Theme.BACKGROUND);
        prepareViewCourt();
    }

    /** Ubica marcador, tiempo e ilustraciones sencillas de ambos lados. */
    private JPanel prepareViewHud() {
        // Las barras quedan a los lados del reloj y del marcador central.
        JPanel hud = new JPanel(new BorderLayout(10, 8));
        hud.setOpaque(false);
        hud.add(prepareViewPlayerArtwork(true), BorderLayout.WEST);
        hud.add(prepareViewScore(), BorderLayout.CENTER);
        hud.add(prepareViewPlayerArtwork(false), BorderLayout.EAST);
        hud.setBorder(BorderFactory.createEmptyBorder(4, 8, 0, 8));
        return hud;
    }

    /** Crea la ilustración de estado correspondiente al lado indicado. */
    private JPanel prepareViewPlayerArtwork(boolean left) {
        JPanel status = new JPanel(new BorderLayout(8, 2));
        status.setOpaque(false);
        status.setPreferredSize(new Dimension(190, HUD_HEIGHT - 8));

        JLabel animal = new JLabel(left ? "JUGADOR 2" : "JUGADOR 1", SwingConstants.CENTER);
        if (left) leftParticipantLabel = animal;
        else rightParticipantLabel = animal;
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
        // Estas etiquetas se crean una vez y reciben los valores de cada captura.
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

    /** Recibe el texto a través de las etiquetas del panel exterior.
     * {@code doLayout()} posiciona las etiquetas y {@code paintComponent}
     * dibuja el fondo del marcador. */
    private final class ScoreboardArtwork extends JPanel {
        private final Image image = loadImage(SCOREBOARD_IMAGE);

        private ScoreboardArtwork() {
            setOpaque(false);
            setLayout(null);
            setPreferredSize(new Dimension(300, 96));
        }

        @Override
        public void doLayout() {
            // Posiciona las etiquetas de forma proporcional a la imagen del tablero.
            int width = getWidth();
            int height = getHeight();
            leftScoreLabel.setBounds((int) (width * 0.12), (int) (height * 0.38),
                    (int) (width * 0.18), (int) (height * 0.36));
            elapsedLabel.setBounds((int) (width * 0.39), (int) (height * 0.42),
                    (int) (width * 0.22), (int) (height * 0.3));
            rightScoreLabel.setBounds((int) (width * 0.70), (int) (height * 0.38),
                    (int) (width * 0.18), (int) (height * 0.36));
        }

        /** Dibuja el fondo del marcador.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(),
                        2000, 1500, 5600, 3200, this);
            }
        }
    }

    /** Dibuja la ilustración de estado al tamaño del componente. */
    private static final class StatusArtwork extends JPanel {
        private final Image image;

        private StatusArtwork(Image image) {
            this.image = image;
            setOpaque(false);
        }

        /** Dibuja la ilustración de estado del participante.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(Graphics graphics) {
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

    /** Registra la acción para pausar el partido. */
    @Override
    public void prepareController() {
        prepareControllerPause();
    }

    /** Abre la pantalla de pausa desde el botón ubicado bajo el marcador. */
    private void prepareControllerPause() {
        // La navegación a Pausa detiene el reloj en la ventana principal.
        pauseButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.PAUSE));
    }

    /** Asigna los recursos de cada personaje según el código de país.
     * @param leftCode código del personaje izquierdo
     * @param rightCode código del personaje derecho
     */
    private void setCharacterImages(String leftCode, String rightCode) {
        characterSprites.clear();
        characterSprites.put("left", courtView.loadCharacterSprite(leftCode));
        characterSprites.put("right", courtView.loadCharacterSprite(rightCode));
        if (courtView != null) {
            courtView.repaint();
        }
    }

    /** Actualiza ambas etiquetas según la modalidad seleccionada.
     * @param againstBot indica si el lado derecho está controlado por un bot
     */
    private void setAgainstBot(boolean againstBot) {
        leftParticipantLabel.setText(againstBot ? "HUMANO" : "JUGADOR 2");
        rightParticipantLabel.setText(againstBot ? "MÁQUINA" : "JUGADOR 1");
    }

    /** Muestra el marcador del partido por lado de la cancha.
     * @param leftGoals goles del lado izquierdo
     * @param rightGoals goles del lado derecho
     */
    private void setScore(int leftGoals, int rightGoals) {
        leftScoreLabel.setText(Integer.toString(leftGoals));
        rightScoreLabel.setText(Integer.toString(rightGoals));
    }

    /** Muestra el tiempo de juego transcurrido en formato minutos y segundos.
     * @param elapsedSeconds segundos transcurridos desde el inicio del partido
     */
    private void setElapsedSeconds(double elapsedSeconds) {
        // Se descarta la fracción de segundo. El reloj muestra tiempo transcurrido.
        int seconds = (int) elapsedSeconds;
        elapsedLabel.setText(String.format("%02d:%02d", seconds / 60, seconds % 60));
    }

    /** Destaca el reloj mientras la partida está en muerte súbita.
     * @param suddenDeath indica si la muerte súbita está activa
     */
    private void setSuddenDeath(boolean suddenDeath) {
        // El dominio decide la fase. Aquí solo cambia el aspecto del reloj.
        elapsedLabel.setForeground(suddenDeath ? Color.ORANGE : Color.WHITE);
        elapsedLabel.setToolTipText(suddenDeath ? "Muerte súbita" : null);
    }

    /** Refresca el marcador, el tiempo y la cancha con el último paso publicado. */
    public void refreshMatchView() {
        // Si aún no se publicó una partida, Swing conserva los componentes iniciales.
        PresentationState.getMatch().ifPresent(this::showMatch);
    }

    /** Devuelve el foco a la cancha para que sus controles de teclado reciban las teclas. */
    void focusCourtForControls() {
        courtView.requestFocusInWindow();
    }

    /** Refresca los componentes con los datos del mismo paso. */
    private void showMatch(MatchSnapshot match) {
        MatchConfiguration configuration = match.getConfiguration();
        // Carga los recursos de personajes al cambiar la configuración de la partida.
        if (configuration != displayedConfiguration) {
            setCharacterImages(
                    configuration.countryCodeForSide(false),
                    configuration.countryCodeForSide(true));
            setAgainstBot(configuration.againstBot());
            displayedConfiguration = configuration;
        }
        // Los rótulos y el lienzo reciben los valores del mismo paso de simulación.
        setScore(match.getLeftGoals(), match.getRightGoals());
        setElapsedSeconds(match.getElapsedSeconds());
        setSuddenDeath(match.isSuddenDeath());
        courtView.setMatch(match);
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

    /** Agrupa las imágenes visuales recortadas de cabeza y cuerpo de un animal. */
    private static final class CharacterSprite {
        private final BufferedImage head;
        private final BufferedImage body;
        private final BufferedImage powerAura;
        private final boolean bodyFacesRightAtSource;

        /** Registra la cabeza, el cuerpo y la orientación del recurso original.
         * @param head imagen visual recortada de la cabeza
         * @param body imagen visual recortada de las patas o cuerpo
         * @param bodyFacesRightAtSource indica si las patas originales apuntan a la derecha
         */
        private CharacterSprite(BufferedImage head, BufferedImage body, BufferedImage powerAura,
                boolean bodyFacesRightAtSource) {
            this.head = head;
            this.body = body;
            this.powerAura = powerAura;
            this.bodyFacesRightAtSource = bodyFacesRightAtSource;
        }

        private BufferedImage head() { return head; }
        private BufferedImage body() { return body; }
        private BufferedImage powerAura() { return powerAura; }
        private boolean bodyFacesRightAtSource() { return bodyFacesRightAtSource; }
    }

    /** Recibe la captura en {@code setMatch()}, solicita un repintado y lee
     * su geometría en {@code paintComponent} para dibujar la cancha y sus entidades. */
    private final class CourtView extends JPanel {
        private MatchSnapshot match;
        private JPanel hud;
        private final Map<String, BufferedImage> trimmedSprites = new HashMap<>();
        private final Image courtImage = loadImage(COURT_IMAGE);
        private final Image ballImage = loadImage(BALL_IMAGE);

        private CourtView() {
            setBackground(new Color(0x29A8DB));
            setBorder(BorderFactory.createLineBorder(Theme.SAND_BORDER, 2));
            setMinimumSize(new Dimension(320, 220));
            setFocusable(true);
            setLayout(null);
        }

        /** Guarda la misma captura que utiliza el HUD para el siguiente repintado. */
        private void setMatch(MatchSnapshot match) {
            // Swing redibuja más tarde con la última captura que guardó este lienzo.
            this.match = match;
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
            // El HUD cubre la franja superior y conserva el ancho del lienzo.
            hud.setBounds(0, 0, getWidth(), Math.min(HUD_HEIGHT, getHeight()));
        }

        /** Dibuja el fondo y las entidades de la última captura recibida.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (match == null) {
                return;
            }
            Bounds court = match.getCourt();
            double scale = courtScale(court.getWidth(), court.getHeight());
            if (scale <= 0) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            double drawWidth = court.getWidth() * scale;
            double drawHeight = court.getHeight() * scale;
            // Centra la ilustración en horizontal y la ancla al borde inferior.
            double offsetX = (getWidth() - drawWidth) / 2.0;
            double offsetY = getHeight() - drawHeight;

            // El orden coloca arcos y personajes sobre el fondo y deja los balones al frente.
            drawCourtImage(g, offsetX, offsetY, drawWidth, drawHeight);
            drawGoals(g, court, offsetX, offsetY, scale);
            drawChargedPower(g, court, offsetX, offsetY, scale, false);
            drawChargedPower(g, court, offsetX, offsetY, scale, true);
            drawPlayer(g, court, offsetX, offsetY, scale, false);
            drawPlayer(g, court, offsetX, offsetY, scale, true);
            for (Ball ball : match.getBalls()) {
                drawBall(g, court, offsetX, offsetY, scale, ball);
            }
            g.dispose();
        }

        /** Pinta el recurso completo de la cancha dentro de los límites del juego. */
        private void drawCourtImage(Graphics2D g, double x, double y, double width, double height) {
            if (courtImage != null) {
                g.drawImage(courtImage, (int) x, (int) y, (int) width, (int) height, this);
            }
        }

        /** Dibuja los arcos en las posiciones recibidas.
         * @param g contexto gráfico de la cancha
         * @param court rectángulo exterior de la cancha
         * @param offsetX desplazamiento horizontal del campo dibujado
         * @param offsetY desplazamiento vertical del campo dibujado
         * @param scale escala aplicada a las coordenadas del dominio
         */
        private void drawGoals(Graphics2D g, Bounds court,
                double offsetX, double offsetY, double scale) {
            g.setColor(new Color(255, 211, 67, 190));
            for (Bounds goal : match.getGoals()) {
                // X usa el ancho completo. Y y el alto usan la franja jugable de la imagen.
                int x = (int) (offsetX + (goal.getX() - court.getX()) * scale);
                int y = (int) playY(goal.getY(), court, offsetY, scale);
                int width = (int) (goal.getWidth() * scale);
                int height = (int) (goal.getHeight() * scale * PLAY_AREA_HEIGHT_RATIO);
                g.drawRect(x, y, width, height);
            }
        }

        /** Dibuja el personaje con los pies anclados al borde inferior de la hitbox recibida. */
        private void drawPlayer(Graphics2D g, Bounds court,
                double offsetX, double offsetY, double scale, boolean right) {
            CharacterSprite sprite = characterSprites.get(right ? "right" : "left");
            if (sprite == null || sprite.head() == null || sprite.body() == null) {
                return;
            }

            Bounds bounds = match.getParticipant(right).getBounds();
            // La cabeza y la pata se centran en X y se apoyan en el borde inferior de la hitbox.
            double centerX = offsetX + (bounds.getX() + bounds.getWidth() / 2 - court.getX()) * scale;
            double baselineY = playY(bounds.getY() + bounds.getHeight(), court, offsetY, scale);
            double spriteHeight = bounds.getHeight() * scale;
            // Reparte el tamaño visual entre cabeza y pata y conserva la proporción de la cabeza.
            double headRatio = CHARACTER_HEAD_HEIGHT_RATIO
                    / (CHARACTER_HEAD_HEIGHT_RATIO + CHARACTER_BODY_HEIGHT_RATIO);
            int headHeight = Math.max(1, (int) (spriteHeight * headRatio));
            int headWidth = Math.max(1, (int) (headHeight
                    * sprite.head().getWidth() / (double) sprite.head().getHeight()));
            int bodyHeight = Math.max(1, (int) (spriteHeight * (1 - headRatio)));
            int bodyWidth = Math.max(1, (int) (headWidth * CHARACTER_BODY_WIDTH_RATIO));
            int bodyX = (int) (centerX - bodyWidth / 2.0);
            int bodyY = (int) (baselineY - bodyHeight);
            int headX = (int) (centerX - headWidth / 2.0);
            int headY = bodyY - headHeight
                    + (int) (headHeight * CHARACTER_HEAD_BODY_OVERLAP_RATIO);
            boolean flipBody = !right != sprite.bodyFacesRightAtSource();

            // Pinta la pata primero para que la cabeza cubra la unión entre ambas imágenes.
            drawCharacterPart(g, sprite.body(), bodyX, bodyY, bodyWidth, bodyHeight, flipBody);
            drawCharacterPart(g, sprite.head(), headX, headY, headWidth, headHeight, right);
        }

        /** Dibuja detrás del personaje el aura de su poder cuando la recarga está completa. */
        private void drawChargedPower(Graphics2D g, Bounds court,
                double offsetX, double offsetY, double scale, boolean right) {
            Participant participant = match.getParticipant(right);
            if (participant.getPowerRemainingSeconds() > 0) return;

            CharacterSprite sprite = characterSprites.get(right ? "right" : "left");
            if (sprite == null || sprite.powerAura() == null) return;

            Bounds bounds = participant.getBounds();
            double centerX = offsetX + (bounds.getX() + bounds.getWidth() / 2 - court.getX()) * scale;
            double spriteHeight = bounds.getHeight() * scale;
            double baselineY = playY(bounds.getY() + bounds.getHeight(), court, offsetY, scale);
            double centerY = baselineY - spriteHeight / 2.0;
            int size = Math.max(1, (int) (spriteHeight * POWER_AURA_HEIGHT_RATIO));
            g.drawImage(sprite.powerAura(), (int) (centerX - size / 2.0),
                    (int) (centerY - size / 2.0), size, size, this);
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
            // Refleja una copia del contexto sin invertir el resto de la cancha.
            flipped.translate(x + width, y);
            flipped.scale(-1, 1);
            flipped.drawImage(image, 0, 0, width, height, this);
            flipped.dispose();
        }

        /** Carga el par de sprites escalados según la especie indicada por la configuración. */
        private CharacterSprite loadCharacterSprite(String countryCode) {
            // El catálogo aporta las dos imágenes y la orientación original de la pata.
            String head = PresentationOptions.character(countryCode, "preview", "");
            String body = PresentationOptions.character(countryCode, "body", "");
            if (head.isEmpty() || body.isEmpty()) return null;
            String headPath = "/images/outfits/heads/Heads Scaled/" + head;
            String bodyPath = "/images/outfits/shoes/" + body;
            boolean bodyFacesRightAtSource = Boolean.parseBoolean(
                    PresentationOptions.character(countryCode, "bodyFacesRight", "false"));
            String powerPath = POWER_IMAGE_FOLDER + PresentationOptions.character(countryCode, "head", "");
            return new CharacterSprite(loadTrimmedSprite(headPath), loadTrimmedSprite(bodyPath),
                    loadTrimmedSprite(powerPath), bodyFacesRightAtSource);
        }

        /** Recorta solo el espacio transparente para alinear y escalar el sprite visual. */
        private BufferedImage loadTrimmedSprite(String resourcePath) {
            // Guarda tanto los recortes como los recursos que no pudieron cargarse.
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
                // El canal alfa delimita el rectángulo visible que se va a recortar.
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

        /** Proyecta el centro del balón sobre la superficie jugable de la ilustración. */
        private void drawBall(Graphics2D g, Bounds court, double offsetX,
                double offsetY, double scale, Ball ball) {
            // El tamaño visual tiene un mínimo. Restar medio diámetro conserva el centro físico.
            double diameter = Math.max(ball.getRadius() * 2 * scale,
                    court.getHeight() * scale * BALL_VISUAL_DIAMETER_RATIO);
            double x = offsetX + (ball.getX() - court.getX()) * scale - diameter / 2.0;
            double y = playY(ball.getY(), court, offsetY, scale) - diameter / 2.0;
            if (ballImage != null) {
                g.drawImage(ballImage, (int) x, (int) y, (int) diameter, (int) diameter, this);
            } else {
                Ellipse2D shape = new Ellipse2D.Double(x, y, diameter, diameter);
                g.setColor(Color.WHITE);
                g.fill(shape);
                g.setColor(Theme.DARK_GREEN);
                g.draw(shape);
            }
        }

        /** Convierte una coordenada vertical de juego en una coordenada de la ilustración. */
        private double playY(double y, Bounds court, double offsetY, double scale) {
            // Desplaza y comprime la coordenada vertical dentro de la franja de juego del fondo.
            return offsetY + (court.getHeight() * PLAY_AREA_TOP_RATIO
                    + (y - court.getY()) * PLAY_AREA_HEIGHT_RATIO) * scale;
        }

        /** Calcula la escala que mantiene la proporción del campo en el área disponible. */
        private double courtScale(double courtWidth, double courtHeight) {
            // La dimensión con menos espacio fija una única escala para ambos ejes.
            return Math.min((getWidth() - FIELD_MARGIN * 2.0) / courtWidth,
                    getHeight() / courtHeight);
        }

    }
}
