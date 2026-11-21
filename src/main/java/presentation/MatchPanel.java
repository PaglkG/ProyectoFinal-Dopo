package presentation;

import java.awt.BasicStroke;
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
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingConstants;

import domain.BallView;
import domain.Game;
import domain.ParticipantView;
import domain.RectangleView;
import domain.SessionView;

/** Vista del partido: HUD y dibujo de la cancha desde la instantánea de Game. */
public class MatchPanel extends BasePanel {
    private static final Color FIELD_GREEN = new Color(0x277C4D);
    private static final Color LEFT_HITBOX = new Color(0x37D5E7);
    private static final Color RIGHT_HITBOX = new Color(0xFF5E75);
    private static final Color BOUNDARY_HITBOX = new Color(0xFFE45E);
    private static final Color GOAL_HITBOX = new Color(0xFF9D42);
    private static final int FIELD_MARGIN = 24;

    private final Game game;
    private final Map<String, Image> characterImages = new HashMap<>();

    private JLabel scoreLabel;
    private JLabel elapsedLabel;
    private JLabel leftParticipantLabel;
    private CourtView courtView;

    /** Crea la vista conectada a la fachada y a la navegación de presentación. */
    public MatchPanel(Game game) {
        this.game = game;
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
        status.setPreferredSize(new Dimension(230, 66));

        JLabel animal = new JLabel(left ? "JUGADOR 2" : "JUGADOR 1", SwingConstants.CENTER);
        if (left) {
            leftParticipantLabel = animal;
        }
        animal.setFont(new Font("SansSerif", Font.BOLD, 12));
        animal.setForeground(Theme.DARK_GREEN);
        status.add(animal, BorderLayout.NORTH);

        JPanel bars = new JPanel(new java.awt.GridLayout(2, 1, 0, 3));
        bars.setOpaque(false);
        JProgressBar lifeBar = createUnavailableBar(new Color(0xD9534F));
        JProgressBar powerBar = createUnavailableBar(Theme.GREEN);
        JLabel lifeLabel = new JLabel("VIDA · no conectada", SwingConstants.LEFT);
        JLabel powerLabel = new JLabel("PODER · no conectado", SwingConstants.LEFT);
        styleStatusLabel(lifeLabel);
        styleStatusLabel(powerLabel);
        bars.add(createLabeledBar(lifeLabel, lifeBar));
        bars.add(createLabeledBar(powerLabel, powerBar));
        status.add(bars, BorderLayout.CENTER);

        return status;
    }

    /** Crea una barra visual sin porcentaje para no representar datos inexistentes. */
    /** Crea el marco de una barra cuyo valor todavía no ofrece la fachada Game. */
    private JProgressBar createUnavailableBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(0);
        bar.setStringPainted(false);
        bar.setForeground(color);
        bar.setBackground(new Color(0xE4E4E4));
        bar.setBorder(BorderFactory.createLineBorder(new Color(0xA7A7A7)));
        bar.setPreferredSize(new Dimension(150, 12));
        bar.setToolTipText("Game aún no expone este valor a la presentación");
        return bar;
    }

    /** Agrupa un rótulo y su barra sin añadir comportamiento de juego. */
    /** Compone un rótulo con su espacio de barra correspondiente. */
    private JPanel createLabeledBar(JLabel label, JProgressBar bar) {
        JPanel row = new JPanel(new BorderLayout(7, 0));
        row.setOpaque(false);
        label.setPreferredSize(new Dimension(92, 14));
        row.add(label, BorderLayout.WEST);
        row.add(bar, BorderLayout.CENTER);
        return row;
    }

    /** Aplica la tipografía común a rótulos de vida y poder. */
    private void styleStatusLabel(JLabel label) {
        label.setFont(new Font("SansSerif", Font.BOLD, 9));
        label.setForeground(Theme.LABEL_COLOR);
    }

    /** Inicializa marcador y tiempo que provienen de la instantánea del dominio. */
    private JPanel prepareViewScore() {
        JPanel score = new JPanel(new java.awt.GridLayout(2, 1, 0, 2));
        score.setOpaque(false);
        scoreLabel = new JLabel("0  :  0", SwingConstants.CENTER);
        scoreLabel.setFont(new Font("SansSerif", Font.BOLD, 24));
        scoreLabel.setForeground(Theme.DARK_GREEN);
        elapsedLabel = new JLabel("00:00", SwingConstants.CENTER);
        elapsedLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
        elapsedLabel.setForeground(Theme.LABEL_COLOR);
        score.add(scoreLabel);
        score.add(elapsedLabel);
        return score;
    }

    /** Agrega la cancha, que pinta las posiciones y hitboxes consultadas a Game. */
    private void prepareViewCourt() {
        courtView = new CourtView();
        add(courtView, BorderLayout.CENTER);
    }

    /** Este panel no registra acciones mientras el movimiento del partido está pendiente. */
    @Override
    public void prepareController() {
        // Esta vista solo representa el estado consultado en Game.
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
        scoreLabel.setText(session.goalsLeft() + "  :  " + session.goalsRight());
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

    /** Dibuja cancha y entidades usando solamente los valores de SessionView. */
    private final class CourtView extends JPanel {
        private SessionView session;

        private CourtView() {
            setBackground(FIELD_GREEN.darker());
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

            drawField(g, court, offsetX, offsetY, scale);
            drawPlayers(g, session, court, offsetX, offsetY, scale);
            drawBalls(g, session, court, offsetX, offsetY, scale);
            drawHitboxes(g, session, court, offsetX, offsetY, scale);
            g.dispose();
        }

        /** Pinta el terreno y sus marcas centradas dentro del área disponible. */
        private void drawField(Graphics2D g, RectangleView court,
                double offsetX, double offsetY, double scale) {
            double width = court.width() * scale;
            double height = court.height() * scale;
            g.setColor(FIELD_GREEN);
            g.fill(new Rectangle2D.Double(offsetX, offsetY, width, height));
            for (int stripe = 0; stripe < 8; stripe += 2) {
                g.setColor(new Color(255, 255, 255, 16));
                g.fill(new Rectangle2D.Double(offsetX + stripe * width / 8, offsetY,
                        width / 8, height));
            }
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.draw(new Rectangle2D.Double(offsetX, offsetY, width, height));
            g.drawLine((int) (offsetX + width / 2), (int) offsetY,
                    (int) (offsetX + width / 2), (int) (offsetY + height));
            g.draw(new Ellipse2D.Double(offsetX + width * .375, offsetY + height * .25,
                    width * .25, height * .5));
        }

        /** Pinta el animal asociado al lado y contornea su hitbox real. */
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
                } else {
                    g.setColor(player.side().equals("left") ? LEFT_HITBOX : RIGHT_HITBOX);
                    g.fill(box);
                }
                g.setColor(player.side().equals("left") ? LEFT_HITBOX : RIGHT_HITBOX);
                g.setStroke(new BasicStroke(2f));
                g.draw(box);
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

        /** Distingue visualmente límites y arcos usando los rectángulos de la instantánea. */
        private void drawHitboxes(Graphics2D g, SessionView state, RectangleView court,
                double offsetX, double offsetY, double scale) {
            for (RectangleView boundary : state.boundaries()) {
                drawWorldRectangle(g, boundary, court, offsetX, offsetY, scale,
                        new Color(255, 228, 94, 70), BOUNDARY_HITBOX);
            }
            for (RectangleView goal : state.goals()) {
                drawWorldRectangle(g, goal, court, offsetX, offsetY, scale,
                        new Color(255, 157, 66, 60), GOAL_HITBOX);
            }
        }

        /** Dibuja un área de la cancha con su color de relleno y borde identificador. */
        private void drawWorldRectangle(Graphics2D g, RectangleView rectangle, RectangleView court,
                double offsetX, double offsetY, double scale, Color fill, Color border) {
            Rectangle2D box = worldRectangle(rectangle.x(), rectangle.y(),
                    rectangle.width(), rectangle.height(), court, offsetX, offsetY, scale);
            g.setColor(fill);
            g.fill(box);
            g.setColor(border);
            g.setStroke(new BasicStroke(1.5f));
            g.draw(box);
        }

        /** Convierte un rectángulo del mundo del juego a coordenadas de pantalla. */
        private Rectangle2D worldRectangle(double x, double y, double width, double height,
                RectangleView court, double offsetX, double offsetY, double scale) {
            return new Rectangle2D.Double(offsetX + (x - court.x()) * scale,
                    offsetY + (y - court.y()) * scale, width * scale, height * scale);
        }
    }
}
