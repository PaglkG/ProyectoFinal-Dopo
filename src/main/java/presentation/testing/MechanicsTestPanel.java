package presentation.testing;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.KeyStroke;

import domain.Ball;
import domain.GameSession;
import domain.SessionParticipant;
import domain.SoccerField;
import domain.exception.GameException;

/** Ventana de pruebas manuales para las mecánicas ya implementadas en el dominio. */
public final class MechanicsTestPanel extends JPanel {

    private final MechanicsTestController controller = new MechanicsTestController();
    private final JComboBox<MechanicsTestController.SessionMode> sessionMode =
            new JComboBox<>(MechanicsTestController.SessionMode.values());
    private final JComboBox<MechanicsTestController.MatchMode> matchMode =
            new JComboBox<>(MechanicsTestController.MatchMode.values());
    private final JSpinner limit = new JSpinner(new SpinnerNumberModel(5, 0, 999, 1));
    private final JLabel status = new JLabel("Sesión lista. Usa los escenarios para probar contactos.");
    private final JLabel score = new JLabel();
    private final JLabel controlHelp = new JLabel();
    private final FieldView fieldView = new FieldView();
    private final Timer timer = new Timer(16, event -> advanceOneStep(false));
    private final JButton runButton = new JButton("Iniciar simulación");
    private final JButton kickLeftButton = new JButton("Patear izquierda (F)");
    private final JButton kickRightButton = new JButton("Patear derecha (Enter)");
    private final Set<Integer> kickKeysDown = new HashSet<>();

    public MechanicsTestPanel() {
        super(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(new Color(24, 30, 39));
        add(buildSettings(), BorderLayout.NORTH);
        add(fieldView, BorderLayout.CENTER);
        add(buildScenarios(), BorderLayout.EAST);
        add(buildFooter(), BorderLayout.SOUTH);
        configureMovementKeys();
        startSession();
    }

    /** Detiene el temporizador visual cuando se cierra la ventana. */
    public void stopSimulation() {
        timer.stop();
    }

    private JPanel buildSettings() {
        JPanel settings = new JPanel();
        settings.setOpaque(false);
        settings.add(label("Sesión"));
        settings.add(sessionMode);
        settings.add(label("Modalidad"));
        settings.add(matchMode);
        settings.add(label("Límite (goles/minutos)"));
        settings.add(limit);
        JButton create = new JButton("Nueva sesión");
        create.addActionListener(event -> startSession());
        settings.add(create);
        JButton step = new JButton("Un paso");
        step.addActionListener(event -> advanceOneStep(true));
        settings.add(step);
        runButton.addActionListener(event -> toggleSimulation());
        settings.add(runButton);
        return settings;
    }

    private JPanel buildScenarios() {
        JPanel panel = new JPanel(new GridLayout(0, 1, 6, 6));
        panel.setOpaque(false);
        panel.setPreferredSize(new Dimension(205, 0));
        panel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(90, 108, 127)), "Acciones y escenarios", 0, 0,
                new Font(Font.SANS_SERIF, Font.BOLD, 13), Color.WHITE));
        kickLeftButton.addActionListener(event -> performKick(true));
        kickRightButton.addActionListener(event -> performKick(false));
        panel.add(kickLeftButton);
        panel.add(kickRightButton);
        addScenario(panel, "Gol en arco izquierdo", MechanicsTestController.Scenario.LEFT_GOAL);
        addScenario(panel, "Gol en arco derecho", MechanicsTestController.Scenario.RIGHT_GOAL);
        addScenario(panel, "Contacto jugador-balón", MechanicsTestController.Scenario.PLAYER_BALL_CONTACT);
        addScenario(panel, "Rebote en el suelo", MechanicsTestController.Scenario.GROUND_BOUNCE);
        addScenario(panel, "Contacto seguro con límite", MechanicsTestController.Scenario.SAFE_FIELD_CONTACT);
        addScenario(panel, "Balón fuera de cancha", MechanicsTestController.Scenario.BALL_OUTSIDE);
        addScenario(panel, "Jugador fuera de cancha", MechanicsTestController.Scenario.PARTICIPANT_OUTSIDE);
        addScenario(panel, "Jugador toca límite lateral", MechanicsTestController.Scenario.PARTICIPANT_TOUCHES_SIDE);
        addScenario(panel, "Jugadores superpuestos", MechanicsTestController.Scenario.PARTICIPANTS_OVERLAP);
        addScenario(panel, "Muerte súbita y gol ganador", MechanicsTestController.Scenario.SUDDEN_DEATH_WIN);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(220, 400));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return wrapScenarioScroll(scroll);
    }

    private JPanel wrapScenarioScroll(JScrollPane scroll) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    private void addScenario(JPanel panel, String title, MechanicsTestController.Scenario scenario) {
        JButton button = new JButton(title);
        button.setFocusPainted(false);
        button.addActionListener(event -> {
            timer.stop();
            runButton.setText("Iniciar simulación");
            try {
                controller.runScenario(scenario);
                status.setText("Escenario ejecutado: " + title + ".");
                if (!controller.getSession().isFinished()) {
                    timer.start();
                    runButton.setText("Pausar simulación");
                }
            } catch (GameException exception) {
                status.setText("Excepción detectada: " + exception.getMessage());
            } finally {
                refreshView();
                focusGameField();
            }
        });
        panel.add(button);
    }

    private JPanel buildFooter() {
        JPanel footer = new JPanel(new BorderLayout(8, 4));
        footer.setOpaque(false);
        score.setForeground(Color.WHITE);
        score.setFont(score.getFont().deriveFont(Font.BOLD, 16f));
        status.setForeground(new Color(209, 220, 231));
        footer.add(score, BorderLayout.NORTH);
        footer.add(status, BorderLayout.CENTER);
        JPanel hints = new JPanel(new GridLayout(0, 1, 2, 2));
        hints.setOpaque(false);
        controlHelp.setForeground(new Color(185, 220, 255));
        hints.add(controlHelp);
        hints.add(label("Simulación visual: 150 u/s · salto 110 · gravedad 180. Poderes aún pendientes."));
        footer.add(hints, BorderLayout.SOUTH);
        return footer;
    }

    private void configureMovementKeys() {
        bindKey(KeyEvent.VK_A, "move-left-left", PresentationMovement.Control.LEFT_LEFT);
        bindKey(KeyEvent.VK_D, "move-left-right", PresentationMovement.Control.LEFT_RIGHT);
        bindKey(KeyEvent.VK_W, "jump-left", PresentationMovement.Control.LEFT_JUMP);
        bindKey(KeyEvent.VK_LEFT, "move-right-left", PresentationMovement.Control.RIGHT_LEFT);
        bindKey(KeyEvent.VK_RIGHT, "move-right-right", PresentationMovement.Control.RIGHT_RIGHT);
        bindKey(KeyEvent.VK_UP, "jump-right", PresentationMovement.Control.RIGHT_JUMP);
        bindKickKey(KeyEvent.VK_F, "kick-left", true);
        bindKickKey(KeyEvent.VK_ENTER, "kick-right", false);
    }

    private void bindKey(int keyCode, String name, PresentationMovement.Control control) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), name + "-pressed");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), name + "-released");
        getActionMap().put(name + "-pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                controller.setMovementControl(control, true);
            }
        });
        getActionMap().put(name + "-released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                controller.setMovementControl(control, false);
            }
        });
    }

    private void bindKickKey(int keyCode, String name, boolean leftPlayer) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), name + "-pressed");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), name + "-released");
        getActionMap().put(name + "-pressed", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!kickKeysDown.add(keyCode)) return;
                performKick(leftPlayer);
            }
        });
        getActionMap().put(name + "-released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent event) {
                kickKeysDown.remove(keyCode);
            }
        });
    }

    private void performKick(boolean leftPlayer) {
        boolean kicked = leftPlayer ? controller.kickLeft() : controller.kickRight();
        String side = leftPlayer ? "izquierdo" : "derecho";
        if (kicked) {
            status.setText("Patada del jugador " + side + ".");
        } else if (leftPlayer
                && sessionMode.getSelectedItem() == MechanicsTestController.SessionMode.PLAYER_VS_BOT) {
            status.setText("En jugador contra bot, el jugador controlable está a la derecha.");
        } else {
            status.setText("Acércate: el balón debe quedar a 8 unidades del jugador.");
        }
        refreshView();
    }

    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    private void startSession() {
        timer.stop();
        runButton.setText("Iniciar simulación");
        controller.startNewSession(
                (MechanicsTestController.SessionMode) sessionMode.getSelectedItem(),
                (MechanicsTestController.MatchMode) matchMode.getSelectedItem(),
                (Integer) limit.getValue());
        kickKeysDown.clear();
        status.setText("Sesión nueva. Simulación en marcha; usa las teclas para jugar.");
        boolean playerVsPlayer = sessionMode.getSelectedItem()
                == MechanicsTestController.SessionMode.PLAYER_VS_PLAYER;
        kickLeftButton.setEnabled(playerVsPlayer);
        controlHelp.setText(playerVsPlayer
                ? "Izquierdo: A/D/W · patear F     ·     Derecho: ←/→/↑ · patear Enter"
                : "Humano (derecha): ←/→/↑ · patear Enter     ·     El bot permanece quieto");
        timer.start();
        runButton.setText("Pausar simulación");
        refreshView();
        focusGameField();
    }

    private void toggleSimulation() {
        if (timer.isRunning()) {
            timer.stop();
            runButton.setText("Iniciar simulación");
        } else {
            timer.start();
            runButton.setText("Pausar simulación");
            focusGameField();
        }
    }

    private void advanceOneStep(boolean showStepStatus) {
        try {
            controller.step();
            GameSession session = controller.getSession();
            if (session.isFinished()) {
                timer.stop();
                runButton.setText("Iniciar simulación");
                String mode = matchMode.getSelectedItem() == MechanicsTestController.MatchMode.TIME
                        ? "por tiempo" : "por goles";
                status.setText("La partida " + mode + " terminó.");
            } else if (showStepStatus) {
                status.setText("Actualización aplicada.");
            }
        } catch (GameException exception) {
            timer.stop();
            runButton.setText("Iniciar simulación");
            status.setText("Excepción detectada: " + exception.getMessage());
        }
        refreshView();
    }

    private void refreshView() {
        GameSession session = controller.getSession();
        Ball ball = session.getField().getBalls()[0];
        score.setText(String.format("Izquierda %d  :  %d Derecha  ·  %.2f s  ·  balón vx %.1f / vy %.1f%s",
                session.getGoalsLeft(), session.getGoalsRight(), session.getElapsedSeconds(),
                ball.getVelocityX(), ball.getVelocityY(),
                session.isFinished() ? "     ·     PARTIDA TERMINADA" : ""));
        fieldView.repaint();
    }

    private void focusGameField() {
        SwingUtilities.invokeLater(fieldView::requestFocusInWindow);
    }

    private final class FieldView extends JPanel {
        private FieldView() {
            setPreferredSize(new Dimension(760, 420));
            setBackground(new Color(32, 91, 61));
            setBorder(BorderFactory.createLineBorder(new Color(110, 140, 116), 2));
            setFocusable(true);
        }

        @Override
        protected void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            SoccerField field = controller.getSession().getField();
            var bounds = field.getHitboxField();
            double x = bounds[0].getX();
            double y = bounds[0].getY();
            double width = bounds[0].getWidth();
            double height = bounds[2].getHeight();
            double scale = Math.min((getWidth() - 50.0) / width, (getHeight() - 50.0) / height);
            double offsetX = (getWidth() - width * scale) / 2;
            double offsetY = (getHeight() - height * scale) / 2;

            g.setColor(new Color(230, 243, 228));
            g.setStroke(new BasicStroke(2f));
            g.draw(new Rectangle2D.Double(offsetX, offsetY, width * scale, height * scale));
            g.draw(new Ellipse2D.Double(offsetX + width * scale * .375,
                    offsetY + height * scale * .25, width * scale * .25, height * scale * .5));
            g.drawLine((int) (offsetX + width * scale / 2), (int) offsetY,
                    (int) (offsetX + width * scale / 2), (int) (offsetY + height * scale));
            for (var goal : field.getGoalHitboxes()) {
                drawRect(g, goal.getX() - x, goal.getY() - y, goal.getWidth(), goal.getHeight(),
                        offsetX, offsetY, scale, new Color(237, 191, 87, 120), true);
            }
            drawParticipants(g, controller.getSession().getParticipantsLeft(), x, y, offsetX, offsetY, scale,
                    new Color(71, 151, 255));
            drawParticipants(g, controller.getSession().getParticipantsRight(), x, y, offsetX, offsetY, scale,
                    new Color(255, 105, 105));
            for (Ball ball : field.getBalls()) {
                double radius = ball.getRadius();
                double ballX = offsetX + (ball.getX() - radius - x) * scale;
                double ballY = offsetY + (ball.getY() - radius - y) * scale;
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(ballX, ballY, radius * 2 * scale, radius * 2 * scale));
                g.setColor(Color.DARK_GRAY);
                g.draw(new Ellipse2D.Double(ballX, ballY, radius * 2 * scale, radius * 2 * scale));
                drawVelocityIndicator(g, ballX + radius * scale, ballY + radius * scale, ball.getVelocityY());
            }
            g.dispose();
        }

        private void drawVelocityIndicator(Graphics2D g, double x, double y, double velocityY) {
            double direction = velocityY < 0 ? -1 : 1;
            double length = 8 + Math.min(36, Math.abs(velocityY) * 0.7);
            double endY = y + direction * length;
            g.setColor(new Color(255, 211, 91));
            g.setStroke(new BasicStroke(2f));
            g.drawLine((int) x, (int) y, (int) x, (int) endY);
            g.drawLine((int) x, (int) endY, (int) (x - 4), (int) (endY - direction * 5));
            g.drawLine((int) x, (int) endY, (int) (x + 4), (int) (endY - direction * 5));
        }

        private void drawParticipants(Graphics2D g, java.util.List<SessionParticipant> participants,
                double courtX, double courtY, double offsetX, double offsetY, double scale, Color color) {
            for (SessionParticipant participant : participants) {
                var box = participant.getHitbox();
                double x = offsetX + (box.getX() - courtX) * scale;
                double y = offsetY + (box.getY() - courtY) * scale;
                double width = box.getWidth() * scale;
                double height = box.getHeight() * scale;
                double headSize = Math.min(width * .96, height * .46);
                double headX = x + (width - headSize) / 2;
                double headY = y + height * .02;

                // Silueta arcade de cabeza grande y cuerpo de futbolista.
                g.setColor(new Color(49, 38, 34));
                g.fill(new Ellipse2D.Double(headX, headY, headSize, headSize));
                g.setColor(new Color(245, 202, 158));
                g.fill(new Ellipse2D.Double(headX + headSize * .08, headY + headSize * .10,
                        headSize * .84, headSize * .82));
                g.setColor(color);
                double bodyX = x + width * .22;
                double bodyY = y + height * .43;
                double bodyWidth = width * .56;
                double bodyHeight = height * .38;
                g.fill(new Rectangle2D.Double(bodyX, bodyY, bodyWidth, bodyHeight));
                g.fill(new Rectangle2D.Double(x + width * .28, y + height * .79,
                        width * .16, height * .19));
                g.fill(new Rectangle2D.Double(x + width * .56, y + height * .79,
                        width * .16, height * .19));
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(headX + headSize * .30, headY + headSize * .43,
                        Math.max(2, headSize * .08), Math.max(2, headSize * .08)));
                g.fill(new Ellipse2D.Double(headX + headSize * .62, headY + headSize * .43,
                        Math.max(2, headSize * .08), Math.max(2, headSize * .08)));
                g.setColor(new Color(30, 31, 35));
                g.fill(new Ellipse2D.Double(headX + headSize * .33, headY + headSize * .45,
                        Math.max(1.5, headSize * .04), Math.max(1.5, headSize * .04)));
                g.fill(new Ellipse2D.Double(headX + headSize * .65, headY + headSize * .45,
                        Math.max(1.5, headSize * .04), Math.max(1.5, headSize * .04)));
            }
        }

        private void drawRect(Graphics2D g, double x, double y, double width, double height,
                double offsetX, double offsetY, double scale, Color color, boolean fill) {
            Rectangle2D rectangle = new Rectangle2D.Double(offsetX + x * scale,
                    offsetY + y * scale, width * scale, height * scale);
            g.setColor(color);
            if (fill) g.fill(rectangle);
            g.setColor(new Color(color.getRed(), color.getGreen(), color.getBlue()));
            g.draw(rectangle);
        }
    }
}
