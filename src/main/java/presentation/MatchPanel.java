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
import java.awt.geom.Rectangle2D;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import domain.BallView;
import domain.Game;
import domain.ParticipantView;
import domain.RectangleView;
import domain.SessionView;

/** Vista del partido: HUD y dibujo de la cancha desde la instantánea de Game. */
public class MatchPanel extends BasePanel {
    private static final String COURT_IMAGE = "/images/ui/court@4x-8.png";
    private static final String LEFT_STATUS_IMAGE = "/images/ui/jugador_izquierdo.png";
    private static final String RIGHT_STATUS_IMAGE = "/images/ui/jugador_derecho.png";
    private static final String SCOREBOARD_IMAGE = "/images/ui/scoreboard@4x-8.png";
    private static final String LOGO_IMAGE = "/images/ui/logo@4x-8.png";
    private static final int FIELD_MARGIN = 24;

    private final Game game;
    private final NavigationController navigationController;
    private final Map<String, Image> characterImages = new HashMap<>();

    private JLabel leftScoreLabel;
    private JLabel rightScoreLabel;
    private JLabel elapsedLabel;
    private JLabel leftParticipantLabel;
    private JButton pauseButton;
    private CourtView courtView;

    /** Crea la vista conectada a la fachada y a la navegación de presentación. */
    public MatchPanel(Game game, NavigationController navigationController) {
        this.game = game;
        this.navigationController = navigationController;
        prepareView();
        prepareController();
    }

    /** Construye el HUD y la cancha en secciones separadas. */
    @Override
    public void prepareView() {
        setLayout(new BorderLayout(12, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 18, 14, 18));
        setBackground(Theme.BACKGROUND);
        prepareViewHud();
        prepareViewCourt();
    }

    /** Ubica marcador, tiempo y las barras de vida y poder de ambos lados. */
    private void prepareViewHud() {
        JPanel hud = new JPanel(new BorderLayout(10, 8));
        hud.setOpaque(false);
        hud.add(prepareViewPlayerStatus(true), BorderLayout.WEST);
        hud.add(prepareViewScore(), BorderLayout.CENTER);
        hud.add(prepareViewPlayerStatus(false), BorderLayout.EAST);
        add(hud, BorderLayout.NORTH);
    }

    /** Crea el grupo de barras correspondiente al lado indicado. */
    private JPanel prepareViewPlayerStatus(boolean left) {
        JPanel status = new JPanel(new BorderLayout(8, 2));
        status.setOpaque(false);
        status.setPreferredSize(new Dimension(230, 156));

        JLabel animal = new JLabel(left ? "JUGADOR 2" : "JUGADOR 1", SwingConstants.CENTER);
        if (left) {
            leftParticipantLabel = animal;
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
            setPreferredSize(new Dimension(300, 118));
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

    /** Presenta el arte del jugador y deja libres las zonas visuales de vida y poder. */
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

    /** Agrega la cancha, que pinta las posiciones y hitboxes consultadas a Game. */
    private void prepareViewCourt() {
        courtView = new CourtView();
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

    /** Asigna las imágenes seleccionadas a los lados de la cancha. */
    public void setCharacterImages(String leftImage, String rightImage) {
        characterImages.clear();
        characterImages.put("left", loadImage(leftImage));
        characterImages.put("right", loadImage(rightImage));
        if (courtView != null) {
            courtView.repaint();
        }
    }

    /** Actualiza el nombre del lado izquierdo según la modalidad seleccionada. */
    public void setAgainstBot(boolean againstBot) {
        leftParticipantLabel.setText(againstBot ? "MÁQUINA" : "JUGADOR 2");
    }

    /** Refresca marcador, tiempo y geometría una vez que Game tiene una sesión. */
    public void refreshMatchView() {
        SessionView session = game.inspectSession();
        leftScoreLabel.setText(Integer.toString(session.goalsLeft()));
        rightScoreLabel.setText(Integer.toString(session.goalsRight()));
        int elapsed = (int) session.elapsedSeconds();
        elapsedLabel.setText(String.format("%02d:%02d", elapsed / 60, elapsed % 60));
        courtView.setSession(session);
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

    /** Dibuja cancha y entidades usando solamente los valores de SessionView. */
    private final class CourtView extends JPanel {
        private SessionView session;
        private final Image courtImage = loadImage(COURT_IMAGE);

        private CourtView() {
            setBackground(new Color(0x29A8DB));
            setBorder(BorderFactory.createLineBorder(Theme.SAND_BORDER, 2));
            setMinimumSize(new Dimension(320, 220));
        }

        private void setSession(SessionView session) {
            this.session = session;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            if (session == null) {
                return;
            }
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            RectangleView court = session.court();
            double scale = Math.min((getWidth() - FIELD_MARGIN * 2.0) / court.width(),
                    (getHeight() - FIELD_MARGIN * 2.0) / court.height());
            double drawWidth = court.width() * scale;
            double drawHeight = court.height() * scale;
            double offsetX = (getWidth() - drawWidth) / 2.0;
            double offsetY = (getHeight() - drawHeight) / 2.0;

            drawCourtImage(g, offsetX, offsetY, drawWidth, drawHeight);
            drawPlayers(g, session, court, offsetX, offsetY, scale);
            drawBalls(g, session, court, offsetX, offsetY, scale);
            g.dispose();
        }

        /** Pinta el recurso completo de la cancha dentro de los límites del juego. */
        private void drawCourtImage(Graphics2D g, double x, double y, double width, double height) {
            if (courtImage != null) {
                g.drawImage(courtImage, (int) x, (int) y, (int) width, (int) height, this);
            }
        }

        /** Pinta cada sprite en la posición de su hitbox, sin mostrar el contorno. */
        private void drawPlayers(Graphics2D g, SessionView state, RectangleView court,
                double offsetX, double offsetY, double scale) {
            for (ParticipantView player : state.participants()) {
                Rectangle2D box = worldRectangle(player.x(), player.y(), player.width(), player.height(),
                        court, offsetX, offsetY, scale);
                Image image = characterImages.get(player.side());
                if (image != null) {
                    double imageScale = Math.min(box.getWidth() / image.getWidth(this),
                            box.getHeight() / image.getHeight(this));
                    int imageWidth = Math.max(1, (int) (image.getWidth(this) * imageScale));
                    int imageHeight = Math.max(1, (int) (image.getHeight(this) * imageScale));
                    int imageX = (int) (box.getCenterX() - imageWidth / 2.0);
                    g.drawImage(image, imageX, (int) box.getY(), imageWidth, imageHeight, this);
                }
            }
        }

        /** Dibuja cada balón en las coordenadas y con el radio reportados por Game. */
        private void drawBalls(Graphics2D g, SessionView state, RectangleView court,
                double offsetX, double offsetY, double scale) {
            for (BallView ball : state.balls()) {
                double diameter = ball.radius() * 2 * scale;
                double x = offsetX + (ball.x() - ball.radius() - court.x()) * scale;
                double y = offsetY + (ball.y() - ball.radius() - court.y()) * scale;
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(x, y, diameter, diameter));
                g.setColor(Theme.DARK_GREEN);
                g.draw(new Ellipse2D.Double(x, y, diameter, diameter));
            }
        }

        /** Convierte un rectángulo del mundo del juego a coordenadas de pantalla. */
        private Rectangle2D worldRectangle(double x, double y, double width, double height,
                RectangleView court, double offsetX, double offsetY, double scale) {
            return new Rectangle2D.Double(offsetX + (x - court.x()) * scale,
                    offsetY + (y - court.y()) * scale, width * scale, height * scale);
        }
    }
}
