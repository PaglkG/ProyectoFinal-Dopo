package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.function.Consumer;

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
 *       de la cancha, los arcos, los personajes y los balones. También detecta
 *       el impacto visual de Lightning desde la transición del poder y la vida.</li>
 * </ul>
 *
 * <h2>Teclado durante la partida</h2>
 * <ol>
 *   <li>La ventana instala los mapas de Swing en este panel desde
 *       {@code DOPOSoccerGUI.prepareControllerKeyboard()}. El {@code InputMap}
 *       traduce cada evento de presión o liberación a una clave, y el
 *       {@code ActionMap} asocia esa clave con la acción que corre Swing.</li>
 *   <li>La ventana guarda las teclas presionadas. En cada paso del temporizador
 *       usa ese estado para llamar a {@code Game.movePlayer(...)}.
 *       El salto y la patada llaman a {@code Game} en la primera pulsación.</li>
 *   <li>Los botones Patear activan la misma acción que las teclas. La ventana
 *       solicita la patada al dominio y pide aquí la animación de la pata.</li>
 *   <li>Después de avanzar el dominio, la ventana publica un {@link MatchSnapshot}
 *       y llama a {@link #refreshMatchView()}. La vista actualiza sus etiquetas
 *       y solicita un repintado de la cancha con la misma captura.</li>
 * </ol>
 *
 * <p><strong>Para ampliar el HUD:</strong> agrega los componentes y botones
 * en los métodos {@code prepareView...} que construyen el marcador o la cancha,
 * conecta sus callbacks en {@code prepareController...}, y dibuja el estado de
 * juego desde {@link MatchSnapshot}. La ventana sigue siendo dueña de las
 * entradas: sus mapas de presión/liberación llaman a {@code Game}; este panel
 * solo anima la vista tras recibir la captura publicada.</p>
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
    private static final double POWER_RECHARGE_SECONDS = 10.0;
    private static final Color POWER_CHARGE_COLOR = new Color(73, 213, 255);
    private static final Color LIFE_COLOR = new Color(246, 111, 121);
    private static final Color SUDDEN_DEATH_COLOR = new Color(255, 218, 107);
    private static final Font CLOCK_FONT = new Font("SansSerif", Font.BOLD, 16);
    private static final Font SUDDEN_DEATH_HINT_FONT = new Font("SansSerif", Font.BOLD, 13);
    private static final int SPRITE_MAX_EDGE = 512;
    private static final Color COURT_SKY = new Color(41, 171, 226);
    private static final long KICK_DURATION_NS = 240_000_000L;
    /** Ángulo visual máximo de una patada. */
    private static final double KICK_ANGLE = Math.toRadians(48);
    /** Ancho de la pata con respecto a la hitbox del personaje. */
    private static final double CHARACTER_BODY_WIDTH_RATIO = 0.65;
    private static final double CHARACTER_HEAD_BODY_OVERLAP_RATIO = 0.18;
    private static final double POWER_AURA_HEIGHT_RATIO = 1.4;

    /** Controlador que coordina los cambios de pantalla. */
    private final NavigationController navigationController;
    /** Solicita una patada a la ventana por identidad de jugador. */
    private final Consumer<Boolean> kickAction;
    /** Configuración que ya se aplicó a los sprites y rótulos. */
    private MatchConfiguration displayedConfiguration;
    /** Recursos gráficos de los personajes por lado de juego. */
    private final Map<String, CharacterSprite> characterSprites = new HashMap<>();

    /** Marcador visible del lado izquierdo. */
    private JLabel leftScoreLabel;
    /** Marcador visible del lado derecho. */
    private JLabel rightScoreLabel;
    /** Encabezado central del marcador según la modalidad y la fase. */
    private JLabel scoreTitleLabel;
    /** Reloj de tiempo transcurrido o instrucción de muerte súbita. */
    private JLabel elapsedLabel;
    /** Último estado del reloj, para actualizar los textos solo cuando cambian. */
    private int displayedSecond = -1;
    /** Modalidad temporal de la última actualización del marcador. */
    private boolean displayedTimedMatch;
    /** Fase de muerte súbita de la última actualización del marcador. */
    private boolean displayedSuddenDeath;
    /** Rótulo que identifica al participante del lado derecho. */
    private JLabel rightParticipantLabel;
    /** Rótulo que identifica al participante del lado izquierdo. */
    private JLabel leftParticipantLabel;
    /** Ilustraciones con carga de poder, organizadas por lado físico. */
    private StatusArtwork leftStatusArtwork;
    /** Ilustración que superpone la carga del participante derecho. */
    private StatusArtwork rightStatusArtwork;
    /** Acción que abre la pantalla de pausa. */
    private JButton pauseButton;
    /** Controles de patada por lado físico. El lado del bot no tiene control manual. */
    private JButton leftKickButton;
    /** Botón del lado derecho, oculto cuando ese lado corresponde al bot. */
    private JButton rightKickButton;
    /** Componente que dibuja la cancha y los participantes. */
    private CourtView courtView;

    /** Crea la vista del partido.
     * @param navigationController controlador para cambiar de pantalla
     * @param kickAction acción de patada para el jugador 1 ({@code true}) o 2
     */
    public MatchPanel(NavigationController navigationController, Consumer<Boolean> kickAction) {
        // Construye los componentes antes de registrar el clic del botón Pausa.
        this.navigationController = navigationController;
        this.kickAction = kickAction;
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

        Image image = loadImage(left ? LEFT_STATUS_IMAGE : RIGHT_STATUS_IMAGE);
        Image statusImage = image == null ? null : scaleArtwork(image, 380, 235,
                0, 0, image.getWidth(null), image.getHeight(null));
        StatusArtwork artwork = new StatusArtwork(statusImage, left);
        if (left) leftStatusArtwork = artwork;
        else rightStatusArtwork = artwork;
        status.add(artwork, BorderLayout.CENTER);

        JButton kick = new RoundedButton("Patear", Theme.GREEN, Color.WHITE, Theme.SAND_BORDER, 12);
        kick.setFocusable(false);
        if (left) leftKickButton = kick;
        else rightKickButton = kick;
        status.add(kick, BorderLayout.SOUTH);
        return status;
    }

    // Menú del partido: los botones se agregan bajo el marcador en scoreArea.
    // Para compartir el espacio inferior, agrega una fila de botones a BorderLayout.SOUTH.
    /** Inicializa el marcador, el tiempo y el acceso al menú de pausa.
     * @return panel central del HUD con el botón Pausa debajo del marcador
     */
    private JPanel prepareViewScore() {
        JPanel scoreArea = new JPanel(new BorderLayout(0, 3));
        scoreArea.setOpaque(false);
        ScoreboardArtwork score = new ScoreboardArtwork();
        // Estas etiquetas se crean una vez y reciben los valores de cada captura.
        leftScoreLabel = createScoreLabel();
        rightScoreLabel = createScoreLabel();
        scoreTitleLabel = new JLabel("GOLES", SwingConstants.CENTER);
        scoreTitleLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        scoreTitleLabel.setForeground(Color.WHITE);
        elapsedLabel = new JLabel("", SwingConstants.CENTER);
        elapsedLabel.setFont(CLOCK_FONT);
        elapsedLabel.setForeground(Color.WHITE);
        score.add(leftScoreLabel);
        score.add(rightScoreLabel);
        score.add(scoreTitleLabel);
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
        private final Image image;

        /** Crea el fondo que posiciona las etiquetas sobre la imagen del marcador. */
        private ScoreboardArtwork() {
            Image original = loadImage(SCOREBOARD_IMAGE);
            image = original == null ? null : prepareScoreboardImage(original);
            setOpaque(false);
            setLayout(null);
            setPreferredSize(new Dimension(300, 96));
        }

        /** Centra las cifras en los recuadros oscuros del arte recortado. */
        @Override
        public void doLayout() {
            int width = getWidth();
            int height = getHeight();
            leftScoreLabel.setBounds((int) (width * 0.04), (int) (height * 0.27),
                    (int) (width * 0.23), (int) (height * 0.32));
            rightScoreLabel.setBounds((int) (width * 0.76), (int) (height * 0.27),
                    (int) (width * 0.23), (int) (height * 0.32));
            scoreTitleLabel.setBounds((int) (width * 0.32), (int) (height * 0.21),
                    (int) (width * 0.43), (int) (height * 0.18));
            elapsedLabel.setBounds((int) (width * 0.32), (int) (height * 0.39),
                    (int) (width * 0.43), (int) (height * 0.19));
        }

        /** Dibuja el fondo del marcador.
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

    /** Preescala el tablero y cubre la palabra TIME para poner el rótulo de la fase activa. */
    private static Image prepareScoreboardImage(Image original) {
        BufferedImage board = scaleArtwork(original, 720, 340, 2000, 1500, 5600, 3200);
        Graphics2D graphics = board.createGraphics();
        // La placa central del sprite tiene un color uniforme alrededor del rótulo.
        graphics.setColor(new Color(board.getRGB(390, 124), true));
        graphics.fillRect(324, 73, 129, 45);
        graphics.dispose();
        return board;
    }

    /** Rellena los indicadores de vida y poder dentro del sprite original del HUD. */
    private static final class StatusArtwork extends JPanel {
        /** Contorno de la barra SURVIVAL normalizado sobre el sprite izquierdo. */
        private static final Path2D LIFE_TRACK = createLifeTrack();
        /** Contorno de la barra POWER normalizado sobre el sprite izquierdo. */
        private static final Path2D POWER_TRACK = createPowerTrack();
        /** Interior del círculo junto a POWER, medido en el mismo sprite. */
        private static final Ellipse2D POWER_INDICATOR = new Ellipse2D.Double(
                14 / 380.0, 127 / 235.0, 95 / 380.0, 95 / 235.0);
        private final Image image;
        private final boolean left;
        private double life;
        private double powerCharge;
        private boolean powerReady;

        /** Crea la ilustración y conserva el lado para orientar el relleno.
         * @param image imagen de estado del lado correspondiente
         * @param left indica si el círculo de origen de la carga está a la izquierda
         */
        private StatusArtwork(Image image, boolean left) {
            this.image = image;
            this.left = left;
            setOpaque(false);
        }

        /** Actualiza ambas barras con la misma captura del participante.
         * @param participant datos de vida y recarga publicados por la ventana
         */
        private void setMeters(Participant participant) {
            double remainingSeconds = participant.getPowerRemainingSeconds();
            double charge = Math.clamp(1 - remainingSeconds / POWER_RECHARGE_SECONDS, 0.0, 1.0);
            double lifeRatio = participant.getMaxLife() > 0
                    ? Math.clamp((double) participant.getLife() / participant.getMaxLife(), 0.0, 1.0)
                    : 0;
            boolean ready = remainingSeconds <= 0;
            if (charge == powerCharge && lifeRatio == life && ready == powerReady) return;
            powerCharge = charge;
            life = lifeRatio;
            powerReady = ready;
            repaint();
        }

        /** Dibuja las dos barras y enciende el círculo POWER al completar la carga.
         * @param graphics contexto de dibujo suministrado por Swing
         */
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
            if (powerCharge <= 0 && life <= 0 && !powerReady) return;

            Graphics2D fill = (Graphics2D) graphics.create();
            fill.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            fill.scale(getWidth(), getHeight());
            if (!left) {
                // Ambos contornos se reflejan junto con el arte del jugador derecho.
                fill.translate(1, 0);
                fill.scale(-1, 1);
            }
            fillBar(fill, LIFE_TRACK, life, LIFE_COLOR);
            fillBar(fill, POWER_TRACK, powerCharge, POWER_CHARGE_COLOR);
            if (powerReady) {
                fill.setColor(POWER_CHARGE_COLOR);
                fill.fill(POWER_INDICATOR);
            }
            fill.dispose();
        }

        /** Limita el color a la fracción activa sin salirse del contorno. */
        private static void fillBar(Graphics2D graphics, Path2D track, double fraction, Color color) {
            if (fraction <= 0) return;
            Graphics2D bar = (Graphics2D) graphics.create();
            Rectangle2D bounds = track.getBounds2D();
            bar.clip(new Rectangle2D.Double(bounds.getX(), bounds.getY(),
                    bounds.getWidth() * fraction, bounds.getHeight()));
            bar.setColor(color);
            bar.fill(track);
            bar.dispose();
        }

        /** Traza el interior de SURVIVAL con medidas del sprite llevado a 380 × 235. */
        private static Path2D createLifeTrack() {
            var track = new Path2D.Double();
            track.moveTo(13 / 380.0, 88 / 235.0);
            track.quadTo(18 / 380.0, 64 / 235.0, 33 / 380.0, 55 / 235.0);
            track.curveTo(92 / 380.0, 45 / 235.0, 260 / 380.0, 37 / 235.0,
                    302 / 380.0, 39 / 235.0);
            track.lineTo(279 / 380.0, 104 / 235.0);
            track.curveTo(165 / 380.0, 92 / 235.0, 51 / 380.0, 83 / 235.0,
                    13 / 380.0, 88 / 235.0);
            track.closePath();
            return track;
        }

        /** Traza el área azul oscura que queda dentro del borde del sprite izquierdo. */
        private static Path2D createPowerTrack() {
            var track = new Path2D.Double();
            track.moveTo(0.305, 0.714);
            track.curveTo(0.443, 0.775, 0.772, 0.758, 0.922, 0.770);
            track.quadTo(0.961, 0.810, 0.961, 0.896);
            track.lineTo(0.271, 0.896);
            track.quadTo(0.308, 0.831, 0.305, 0.714);
            track.closePath();
            return track;
        }
    }

    /** Agrega la cancha y superpone el HUD en su franja superior. */
    private void prepareViewCourt() {
        courtView = new CourtView();
        courtView.setHud(prepareViewHud());
        add(courtView, BorderLayout.CENTER);
    }

    /** Registra los clics de pausa y patada. La ventana instala los mapas de teclado. */
    @Override
    public void prepareController() {
        prepareControllerPause();
        leftKickButton.addActionListener(event -> requestKick(false));
        rightKickButton.addActionListener(event -> requestKick(true));
    }

    /** Traduce el lado del botón a la identidad que espera la ventana.
     * @param rightSide indica que se pulsó el botón del lado derecho
     */
    private void requestKick(boolean rightSide) {
        if (displayedConfiguration == null || (rightSide && displayedConfiguration.againstBot())) return;
        boolean playerOne = displayedConfiguration.againstBot() ? displayedConfiguration.playerOne() : rightSide;
        kickAction.accept(playerOne);
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

    /** Muestra el reloj para ByTime y lo sustituye cuando el dominio activa la muerte súbita.
     * @param match captura que indica modalidad, tiempo y fase
     */
    private void showMatchClock(MatchSnapshot match) {
        boolean timedMatch = "ByTime".equals(PresentationOptions.simpleName(match.getConfiguration().typeMatch()));
        boolean suddenDeath = timedMatch && match.isSuddenDeath();
        int seconds = timedMatch && !suddenDeath ? (int) match.getElapsedSeconds() : -1;
        if (seconds == displayedSecond && timedMatch == displayedTimedMatch
                && suddenDeath == displayedSuddenDeath) return;

        displayedSecond = seconds;
        displayedTimedMatch = timedMatch;
        displayedSuddenDeath = suddenDeath;
        Color digits = suddenDeath ? SUDDEN_DEATH_COLOR : Color.WHITE;
        leftScoreLabel.setForeground(digits);
        rightScoreLabel.setForeground(digits);
        scoreTitleLabel.setForeground(digits);
        scoreTitleLabel.setText(suddenDeath ? "MUERTE SÚBITA" : timedMatch ? "TIEMPO" : "GOLES");
        elapsedLabel.setFont(suddenDeath ? SUDDEN_DEATH_HINT_FONT : CLOCK_FONT);
        elapsedLabel.setText(suddenDeath ? "Último gol gana"
                : timedMatch ? String.format("%02d:%02d", seconds / 60, seconds % 60) : "");
    }

    /** Refresca el marcador, el tiempo y la cancha con el último paso publicado. */
    public void refreshMatchView() {
        // Si aún no se publicó una partida, Swing conserva los componentes iniciales.
        PresentationState.getMatch().ifPresent(this::showMatch);
    }

    /**
     * Solicita el foco para el lienzo después de que la ventana muestra
     * la tarjeta del partido. Las asociaciones de teclado viven en el panel
     * y tienen alcance de ventana enfocada, por lo que el lienzo no necesita
     * un {@code KeyListener} propio.
     */
    void focusCourtForControls() {
        courtView.requestFocusInWindow();
    }

    /** Reproduce el giro del pie para la patada solicitada desde la ventana.
     * @param rightSide indica el lado físico derecho
     */
    void animateKick(boolean rightSide) {
        courtView.animateKick(rightSide);
    }

    /** Refresca los componentes con los datos del mismo paso.
     * @param match captura publicada por la fachada de presentación
     */
    private void showMatch(MatchSnapshot match) {
        MatchConfiguration configuration = match.getConfiguration();
        // Carga los recursos de personajes al cambiar la configuración de la partida.
        if (configuration != displayedConfiguration) {
            setCharacterImages(
                    configuration.countryCodeForSide(false),
                    configuration.countryCodeForSide(true));
            setAgainstBot(configuration.againstBot());
            boolean leftIsPlayerOne = configuration.againstBot() && configuration.playerOne();
            leftKickButton.setText(leftIsPlayerOne ? "Patear (Espacio)" : "Patear (0)");
            rightKickButton.setText("Patear (Espacio)");
            rightKickButton.setVisible(!configuration.againstBot());
            displayedConfiguration = configuration;
        }
        // Los rótulos y el lienzo reciben los valores del mismo paso de simulación.
        setScore(match.getLeftGoals(), match.getRightGoals());
        showMatchClock(match);
        leftStatusArtwork.setMeters(match.getParticipant(false));
        rightStatusArtwork.setMeters(match.getParticipant(true));
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

    /** Preescala una región del arte para evitar procesar el original en cada repintado. */
    private static BufferedImage scaleArtwork(Image source, int width, int height,
            int left, int top, int right, int bottom) {
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
        Graphics2D g = result.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(source, 0, 0, width, height, left, top, right, bottom, null);
        g.dispose();
        return result;
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
        private static final double LIGHTNING_STUN_DURATION_SECONDS = 3.0;
        private static final double LIGHTNING_PROJECTILE_WINDOW_SECONDS = 4.0;
        private static final double LIGHTNING_FALL_DURATION_SECONDS = 0.42;
        private MatchSnapshot match;
        private JPanel hud;
        private final Map<String, BufferedImage> trimmedSprites = new HashMap<>();
        private final Map<BufferedImage, BufferedImage> blackSprites = new IdentityHashMap<>();
        private final BufferedImage courtImage = loadCourtArtwork();
        private final long[] kickStartedAt = new long[2];
        private final double[] pendingLightningSince = new double[] {-1, -1};
        private final double[] lightningStartedAt = new double[] {-1, -1};
        private final boolean[] previousPowerActive = new boolean[2];
        private final int[] previousLife = new int[2];
        private MatchConfiguration observedConfiguration;
        private double previousElapsedSeconds = -1;
        private final BufferedImage ballImage = loadTrimmedSprite(BALL_IMAGE);

        /** Prepara el lienzo y permite que reciba el foco al entrar en la partida. */
        private CourtView() {
            setBackground(COURT_SKY);
            setBorder(BorderFactory.createLineBorder(Theme.SAND_BORDER, 2));
            setMinimumSize(new Dimension(320, 220));
            setFocusable(true);
            setLayout(null);
        }

        /** Guarda la misma captura que utiliza el HUD para el siguiente repintado. */
        private void setMatch(MatchSnapshot match) {
            // Swing redibuja más tarde con la última captura que guardó este lienzo.
            observeLightningImpact(match);
            this.match = match;
            repaint();
        }

        /** Detecta un tiro Lightning armado y su daño posterior usando solo la captura de Game. */
        private void observeLightningImpact(MatchSnapshot next) {
            double elapsedSeconds = next.getElapsedSeconds();
            if (observedConfiguration != next.getConfiguration()
                    || elapsedSeconds < previousElapsedSeconds) {
                observedConfiguration = next.getConfiguration();
                for (int side = 0; side < 2; side++) {
                    Participant participant = next.getParticipant(side == 1);
                    previousPowerActive[side] = participant.isPowerActive();
                    previousLife[side] = participant.getLife();
                    pendingLightningSince[side] = -1;
                    lightningStartedAt[side] = -1;
                }
                previousElapsedSeconds = elapsedSeconds;
                return;
            }

            for (int attackerSide = 0; attackerSide < 2; attackerSide++) {
                boolean rightSide = attackerSide == 1;
                Participant attacker = next.getParticipant(rightSide);
                if (previousPowerActive[attackerSide] && !attacker.isPowerActive()
                        && isLightningPower(rightSide, next.getConfiguration())) {
                    pendingLightningSince[attackerSide] = elapsedSeconds;
                }
                if (pendingLightningSince[attackerSide] >= 0
                        && elapsedSeconds - pendingLightningSince[attackerSide]
                                > LIGHTNING_PROJECTILE_WINDOW_SECONDS) {
                    pendingLightningSince[attackerSide] = -1;
                }
            }

            for (int victimSide = 0; victimSide < 2; victimSide++) {
                int attackerSide = 1 - victimSide;
                Participant victim = next.getParticipant(victimSide == 1);
                int damageTaken = previousLife[victimSide] - victim.getLife();
                if (pendingLightningSince[attackerSide] >= 0 && damageTaken > 0) {
                    Participant attacker = next.getParticipant(attackerSide == 1);
                    int lightningDamage = (int) Math.round(12 * (1 + 0.10 * (attacker.getKickLevel() - 1)));
                    int expectedLifeLoss = Math.min(lightningDamage, previousLife[victimSide]);
                    if (damageTaken == expectedLifeLoss) {
                        lightningStartedAt[victimSide] = elapsedSeconds;
                        pendingLightningSince[attackerSide] = -1;
                    }
                }
            }

            for (int side = 0; side < 2; side++) {
                Participant participant = next.getParticipant(side == 1);
                previousPowerActive[side] = participant.isPowerActive();
                previousLife[side] = participant.getLife();
            }
            previousElapsedSeconds = elapsedSeconds;
        }

        /** Comprueba el poder configurado para un lado según el orden recibido por Game. */
        private boolean isLightningPower(boolean rightSide, MatchConfiguration configuration) {
            int index = configuration.againstBot() ? (rightSide ? 1 : 0) : (rightSide ? 0 : 1);
            if (configuration.parametersPowers().size() <= index) return false;
            String powerName = configuration.parametersPowers().get(index);
            return "Lightning".equals(PresentationOptions.simpleName(powerName));
        }

        /** Indica si el tiempo de electrocución visual aún no ha terminado. */
        private boolean isLightningAffected(boolean rightSide) {
            double started = lightningStartedAt[rightSide ? 1 : 0];
            return started >= 0 && match.getElapsedSeconds() - started < LIGHTNING_STUN_DURATION_SECONDS;
        }

        /** Oscurece un sprite conservando su transparencia y reutiliza el resultado entre repintados. */
        private BufferedImage blackSprite(BufferedImage source) {
            return blackSprites.computeIfAbsent(source, image -> new RescaleOp(
                    new float[] {0, 0, 0, 1}, new float[] {0, 0, 0, 0}, null).filter(image, null));
        }

        /** Dibuja el rayo descendente durante el inicio visual del impacto. */
        private void drawFallingLightning(Graphics2D g, boolean rightSide, Bounds bounds) {
            double started = lightningStartedAt[rightSide ? 1 : 0];
            if (started < 0) return;
            double elapsed = match.getElapsedSeconds() - started;
            if (elapsed >= LIGHTNING_FALL_DURATION_SECONDS) return;
            double progress = elapsed / LIGHTNING_FALL_DURATION_SECONDS;
            double size = bounds.getWidth() * 0.45;
            double x = bounds.getX() + bounds.getWidth() / 2 - size / 2;
            double y = bounds.getY() - size * 2.2 + size * 2.6 * progress;
            Path2D bolt = new Path2D.Double();
            bolt.moveTo(x + size * 0.62, y);
            bolt.lineTo(x + size * 0.18, y + size * 0.95);
            bolt.lineTo(x + size * 0.51, y + size * 0.95);
            bolt.lineTo(x + size * 0.36, y + size * 1.65);
            bolt.lineTo(x + size * 0.88, y + size * 0.68);
            bolt.lineTo(x + size * 0.57, y + size * 0.68);
            bolt.closePath();
            g.setColor(new Color(255, 236, 75));
            g.fill(bolt);
            g.setColor(Color.WHITE);
            g.draw(bolt);
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
            if (courtImage == null || match.getGoals().size() < 2) return;

            // El sprite se dibuja entero. Reservamos cielo sobre su borde para el techo físico.
            AffineTransform projection = courtProjection();
            double skyPadding = requiredSkyPadding(projection);
            double availableHeight = getHeight() - HUD_HEIGHT;
            double aspect = (double) courtImage.getWidth() / courtImage.getHeight();
            double width = Math.min(getWidth() - 2.0 * FIELD_MARGIN,
                    availableHeight * aspect / (1 + skyPadding));
            if (width <= 0) return;
            double height = width / aspect;
            double x = (getWidth() - width) / 2;
            double y = HUD_HEIGHT + (availableHeight - height * (1 - skyPadding)) / 2;

            Graphics2D g = (Graphics2D) graphics.create();
            try {
                g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
                drawSprite(g, courtImage, x, y, width, height, false);

                // Las bocas y el suelo de Game se proyectan sobre las bocas pintadas del sprite.
                // Los arcos ya están en la ilustración y no necesitan otra figura superpuesta.
                // La proyección devuelve coordenadas relativas al sprite.
                g.translate(x, y);
                g.scale(width, height);
                g.transform(projection);
                drawChargedPower(g, false);
                drawChargedPower(g, true);
                drawPlayer(g, false);
                drawPlayer(g, true);
                for (Ball ball : match.getBalls()) {
                    drawBall(g, ball);
                }
            } finally {
                g.dispose();
            }
        }

        /** Devuelve la fracción de cielo por encima del sprite que necesita el techo físico.
         * Usa el punto más alto de las dos esquinas por la inclinación de la proyección.
         * @param projection transformación de coordenadas físicas a coordenadas del sprite
         * @return altura adicional necesaria en proporción a la altura del sprite
         */
        private double requiredSkyPadding(AffineTransform projection) {
            Bounds court = match.getCourt();
            double leftY = projection.getShearY() * court.getX()
                    + projection.getScaleY() * court.getY() + projection.getTranslateY();
            double rightY = leftY + projection.getShearY() * court.getWidth();
            return Math.max(0, -Math.min(leftY, rightY));
        }

        /**
         * Ancla las bocas físicas a los postes interiores de la ilustración original.
         * Los postes se midieron en el original de 7681 × 4321 píxeles.
         * Con el desnivel pintado entre los arcos, la misma proyección inclina
         * ligeramente el suelo y conserva el contacto de todos los sprites con él.
         * @return transformación a coordenadas normalizadas del sprite
         */
        private AffineTransform courtProjection() {
            Bounds left = match.getGoals().get(0);
            Bounds right = match.getGoals().get(1);
            double leftMouth = left.getX() + left.getWidth();
            double rightMouth = right.getX();
            double floor = left.getY() + left.getHeight();
            double worldGap = rightMouth - leftMouth;
            double spriteLeft = 1050 / 7681.0;
            double spriteFloor = 3665 / 4321.0;
            double scaleX = ((6395 - 1050) / 7681.0) / worldGap;
            double scaleY = ((3665 - 2323) / 4321.0) / left.getHeight();
            double slope = ((3541 - 3665) / 4321.0) / worldGap;
            return new AffineTransform(scaleX, slope, 0, scaleY,
                    spriteLeft - leftMouth * scaleX,
                    spriteFloor - leftMouth * slope - floor * scaleY);
        }

        /** Escala una vez el fondo completo sin recortar sus arcos ni alterar su composición. */
        private BufferedImage loadCourtArtwork() {
            Image image = loadImage(COURT_IMAGE);
            return image == null ? null : scaleArtwork(image, 1280, 720,
                    0, 0, image.getWidth(null), image.getHeight(null));
        }

        /** Registra el comienzo de la patada visual en el lado indicado. */
        private void animateKick(boolean right) {
            kickStartedAt[right ? 1 : 0] = System.nanoTime();
            repaint();
        }

        /** Devuelve el giro de la pata, que sube hacia el rival y regresa al apoyo. */
        private double kickRotation(boolean right) {
            long started = kickStartedAt[right ? 1 : 0];
            long elapsed = System.nanoTime() - started;
            if (started == 0 || elapsed >= KICK_DURATION_NS) return 0;
            return (right ? 1 : -1) * KICK_ANGLE * Math.sin(Math.PI * elapsed / KICK_DURATION_NS);
        }

        /**
         * Usa la hitbox para anclar coronilla, pies y ancho máximo del sprite.
         * Reparte la altura según las proporciones de los recortes de cabeza y pata
         * para evitar estirar una sola pieza. La pata se estrecha en proporción
         * a la hitbox y se mantiene en la zona inferior del personaje. Mientras
         * dura el impacto Lightning, dibuja la silueta negra y el rayo descendente.
         */
        private void drawPlayer(Graphics2D g, boolean right) {
            Bounds bounds = match.getParticipant(right).getBounds();
            CharacterSprite sprite = characterSprites.get(right ? "right" : "left");
            boolean electrocuted = isLightningAffected(right);
            if (sprite == null || sprite.head() == null || sprite.body() == null) {
                g.setColor(electrocuted ? Color.BLACK : Theme.DARK_GREEN);
                g.fill(new Rectangle2D.Double(bounds.getX(), bounds.getY(),
                        bounds.getWidth(), bounds.getHeight()));
                drawFallingLightning(g, right, bounds);
                return;
            }

            double headWidth = bounds.getWidth();
            double bodyWidth = bounds.getWidth() * CHARACTER_BODY_WIDTH_RATIO;
            double naturalHeadHeight = headWidth * sprite.head().getHeight() / sprite.head().getWidth();
            double naturalBodyHeight = bodyWidth * sprite.body().getHeight() / sprite.body().getWidth();
            double visibleBodyFraction = 1 - CHARACTER_HEAD_BODY_OVERLAP_RATIO;
            double heightScale = bounds.getHeight() / (naturalHeadHeight + naturalBodyHeight * visibleBodyFraction);
            double headHeight = naturalHeadHeight * heightScale;
            double bodyHeight = naturalBodyHeight * heightScale;
            double bodyX = bounds.getX() + (bounds.getWidth() - bodyWidth) / 2;
            double bodyY = bounds.getY() + bounds.getHeight() - bodyHeight;
            boolean flipBody = !right != sprite.bodyFacesRightAtSource();

            // El giro se ancla dentro del tercio superior de la pata para limitar su extensión visual.
            // La hitbox y el alcance real de la patada los sigue resolviendo el dominio.
            double rotation = kickRotation(right);
            BufferedImage body = electrocuted ? blackSprite(sprite.body()) : sprite.body();
            BufferedImage head = electrocuted ? blackSprite(sprite.head()) : sprite.head();
            if (rotation == 0) {
                drawSprite(g, body, bodyX, bodyY, bodyWidth, bodyHeight, flipBody);
            } else {
                Graphics2D foot = (Graphics2D) g.create();
                foot.rotate(rotation, bodyX + bodyWidth / 2, bodyY + bodyHeight * 0.30);
                drawSprite(foot, body, bodyX, bodyY, bodyWidth, bodyHeight, flipBody);
                foot.dispose();
            }
            drawSprite(g, head, bounds.getX(), bounds.getY(), headWidth, headHeight, right);
            drawFallingLightning(g, right, bounds);
        }

        /** Centra el aura decorativa mientras el poder solicitado espera el balón. */
        private void drawChargedPower(Graphics2D g, boolean right) {
            Participant participant = match.getParticipant(right);
            if (!participant.isPowerActive()) return;

            CharacterSprite sprite = characterSprites.get(right ? "right" : "left");
            if (sprite == null || sprite.powerAura() == null) return;

            Bounds bounds = participant.getBounds();
            double size = bounds.getHeight() * POWER_AURA_HEIGHT_RATIO;
            drawSprite(g, sprite.powerAura(), bounds.getX() + (bounds.getWidth() - size) / 2,
                    bounds.getY() + (bounds.getHeight() - size) / 2, size, size, false);
        }

        /** Dibuja un sprite en coordenadas del dominio sin redondear la posición a píxeles. */
        private void drawSprite(Graphics2D g, BufferedImage image,
                double x, double y, double width, double height, boolean flipHorizontal) {
            AffineTransform placement = AffineTransform.getTranslateInstance(flipHorizontal ? x + width : x, y);
            placement.scale((flipHorizontal ? -width : width) / image.getWidth(), height / image.getHeight());
            g.drawImage(image, placement, this);
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
                // Reduce los originales grandes una vez, antes de guardarlos para cada frame.
                int edge = Math.max(source.getWidth(), source.getHeight());
                if (edge > SPRITE_MAX_EDGE) {
                    double ratio = (double) SPRITE_MAX_EDGE / edge;
                    source = scaleArtwork(source, Math.max(1, (int) Math.round(source.getWidth() * ratio)),
                            Math.max(1, (int) Math.round(source.getHeight() * ratio)),
                            0, 0, source.getWidth(), source.getHeight());
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

        /** Dibuja el balón recortado con el centro y el diámetro de su hitbox circular. */
        private void drawBall(Graphics2D g, Ball ball) {
            double diameter = ball.getRadius() * 2;
            double x = ball.getX() - ball.getRadius();
            double y = ball.getY() - ball.getRadius();
            if (ballImage != null) {
                drawSprite(g, ballImage, x, y, diameter, diameter, false);
            } else {
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(x, y, diameter, diameter));
            }
        }


    }
}
