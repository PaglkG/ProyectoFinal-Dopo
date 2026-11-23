package presentation.testing;
import domain.BallView;
import domain.ParticipantView;
import domain.RectangleView;
import domain.SessionView;

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
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.KeyStroke;

import domain.Game;

/** Ventana de pruebas manuales para las mecánicas ya implementadas en el dominio. */
public final class MechanicsTestPanel extends JPanel {

    private final MechanicsTestController controller;
    private final JComboBox<MechanicsTestController.SessionMode> sessionMode =
            new JComboBox<>(MechanicsTestController.SessionMode.values());
    private final JComboBox<MechanicsTestController.MatchMode> matchMode =
            new JComboBox<>(MechanicsTestController.MatchMode.values());
    private final JSpinner limit = new JSpinner(new SpinnerNumberModel(5, 0, 999, 1));
    private final JLabel status = new JLabel("Sesión lista. Usa los escenarios para probar contactos.");
    private final JLabel score = new JLabel();
    private final JLabel controlHelp = new JLabel();
    private final JCheckBox showHitboxes = new JCheckBox("Mostrar todas las hitboxes", true);
    private final FieldView fieldView = new FieldView();
    private final Timer timer = new Timer(16, event -> advanceOneStep(false));
    private final JButton runButton = new JButton("Iniciar simulación");
    private final JButton kickLeftButton = new JButton("Patear izquierda (F)");
    private final JButton kickRightButton = new JButton("Patear derecha (Enter)");
    private final Set<Integer> kickKeysDown = new HashSet<>();

    /** Construye controles, escenarios, área de juego y enlaces de teclado; después crea la sesión inicial. */
    public MechanicsTestPanel() {
        this(MechanicsTestConfiguration.defaults());
    }

    /** Construye el banco usando la configuración elegida en la pantalla inicial.
     * @param configuration configuración de modalidad, sesión y límite
     */
    public MechanicsTestPanel(MechanicsTestConfiguration configuration) {
        super(new BorderLayout(10, 10));
        controller = new MechanicsTestController(configuration);
        sessionMode.setSelectedItem(configuration.sessionMode());
        matchMode.setSelectedItem(configuration.matchMode());
        limit.setValue(configuration.limit());
        sessionMode.setEnabled(false);
        matchMode.setEnabled(false);
        limit.setEnabled(false);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        setBackground(new Color(24, 30, 39));
        add(buildSettings(), BorderLayout.NORTH);
        add(fieldView, BorderLayout.CENTER);
        add(buildScenarios(), BorderLayout.EAST);
        fieldView.add(buildFooter(), BorderLayout.SOUTH);
        configureMovementKeys();
        startSession();
    }

    /** Detiene el temporizador visual cuando se cierra la ventana. */
    public void stopSimulation() {
        timer.stop();
    }

    /** Crea los selectores de sesión y modalidad, el límite y los controles de simulación. */
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
        showHitboxes.setOpaque(false);
        showHitboxes.setForeground(Color.WHITE);
        showHitboxes.addActionListener(event -> fieldView.repaint());
        settings.add(showHitboxes);
        runButton.addActionListener(event -> toggleSimulation());
        settings.add(runButton);
        JButton save = new JButton("Guardar partida");
        save.addActionListener(event -> saveGame());
        settings.add(save);
        JButton open = new JButton("Abrir partida");
        open.addActionListener(event -> openGame());
        settings.add(open);
        return settings;
    }

    /** Solicita un archivo y guarda el estado actual de la sesión probada. */
    private void saveGame() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                controller.save(ensureSaveExtension(chooser.getSelectedFile()));
                status.setText("Partida guardada.");
            } catch (RuntimeException exception) {
                status.setText("No se pudo guardar: " + exception.getMessage());
            } finally {
                refreshView();
            }
        }
    }

    /** Solicita un archivo y restaura su sesión en el banco de pruebas. */
    private void openGame() {
        JFileChooser chooser = new JFileChooser();
        if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                timer.stop();
                runButton.setText("Iniciar simulación");
                controller.open(chooser.getSelectedFile());
                status.setText("Partida abierta.");
            } catch (RuntimeException exception) {
                status.setText("No se pudo abrir: " + exception.getMessage());
                JOptionPane.showMessageDialog(this, exception.getMessage(),
                        "Error al abrir la partida", JOptionPane.ERROR_MESSAGE);
            } finally {
                refreshView();
            }
        }
    }

    /** Conserva una extensión .ser visible al guardar desde el banco. */
    private java.io.File ensureSaveExtension(java.io.File file) {
        String name = file.getName();
        return name.toLowerCase().endsWith(".ser")
                ? file : new java.io.File(file.getAbsoluteFile().getParentFile(), name + ".ser");
    }

    /** Construye la lista desplazable de patadas y escenarios manuales disponibles. */
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
        addScenario(panel, "Rebote del balón en límite lateral", MechanicsTestController.Scenario.BALL_SIDE_REBOUND);
        addScenario(panel, "Colisión entre dos balones", MechanicsTestController.Scenario.BALL_BALL_COLLISION);
        addScenario(panel, "Jugador fuera de cancha", MechanicsTestController.Scenario.PARTICIPANT_OUTSIDE);
        addScenario(panel, "Jugador toca límite lateral", MechanicsTestController.Scenario.PARTICIPANT_TOUCHES_SIDE);
        addScenario(panel, "Jugadores superpuestos", MechanicsTestController.Scenario.PARTICIPANTS_OVERLAP);
        addScenario(panel, "Preparar patada al rival", MechanicsTestController.Scenario.KICK_OPPONENT);
        addScenario(panel, "Muerte súbita y gol ganador", MechanicsTestController.Scenario.SUDDEN_DEATH_WIN);
        JScrollPane scroll = new JScrollPane(panel);
        scroll.setPreferredSize(new Dimension(220, 400));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setOpaque(false);
        scroll.setOpaque(false);
        return wrapScenarioScroll(scroll);
    }

    /** Inserta la lista desplazable en un panel contenedor transparente. */
    private JPanel wrapScenarioScroll(JScrollPane scroll) {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setOpaque(false);
        wrapper.add(scroll, BorderLayout.CENTER);
        return wrapper;
    }

    /** Añade un botón que ejecuta el escenario, comunica errores y refresca la vista. */
    private void addScenario(JPanel panel, String title, MechanicsTestController.Scenario scenario) {
        JButton button = new JButton(title);
        button.setFocusPainted(false);
        button.addActionListener(event -> {
            timer.stop();
            runButton.setText("Iniciar simulación");
            try {
                if (scenario == MechanicsTestController.Scenario.BALL_BALL_COLLISION
                        && controller.getSessionView().balls().size() < 2) {
                    status.setText("Este escenario requiere una sesión con al menos dos balones.");
                    refreshView();
                    return;
                }
                controller.runScenario(scenario);
                status.setText("Escenario ejecutado: " + title + ".");
                if (!controller.getSessionView().finished()) {
                    timer.start();
                    runButton.setText("Pausar simulación");
                }
            } catch (RuntimeException exception) {
                status.setText("Excepción detectada: " + exception.getMessage());
            } finally {
                refreshView();
                focusGameField();
            }
        });
        panel.add(button);
    }

    /** Construye la zona inferior con marcador, estado e instrucciones de control. */
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
        hints.add(label("Hitboxes: amarillo cancha · naranja arcos · cian/magenta jugadores · blanco balón."));
        footer.add(hints, BorderLayout.SOUTH);
        return footer;
    }

    /** Asocia teclas de movimiento, salto y patada con las acciones del controlador. */
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

    /** Vincula la pulsación y liberación de una tecla con un control de movimiento. */
    private void bindKey(int keyCode, String name, PresentationMovement.Control control) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), name + "-pressed");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), name + "-released");
        getActionMap().put(name + "-pressed", new AbstractAction() {
            /** Envía al controlador el inicio del estado presionado. */
            @Override
            public void actionPerformed(ActionEvent event) {
                controller.setMovementControl(control, true);
            }
        });
        getActionMap().put(name + "-released", new AbstractAction() {
            /** Envía al controlador la liberación del control. */
            @Override
            public void actionPerformed(ActionEvent event) {
                controller.setMovementControl(control, false);
            }
        });
    }

    /** Vincula una tecla de patada y evita repetirla mientras siga presionada. */
    private void bindKickKey(int keyCode, String name, boolean leftPlayer) {
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, false), name + "-pressed");
        getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(keyCode, 0, true), name + "-released");
        getActionMap().put(name + "-pressed", new AbstractAction() {
            /** Ejecuta una sola patada por pulsación de la tecla. */
            @Override
            public void actionPerformed(ActionEvent event) {
                if (!kickKeysDown.add(keyCode)) return;
                performKick(leftPlayer);
            }
        });
        getActionMap().put(name + "-released", new AbstractAction() {
            /** Elimina la tecla del conjunto de teclas de patada presionadas. */
            @Override
            public void actionPerformed(ActionEvent event) {
                kickKeysDown.remove(keyCode);
            }
        });
    }

    /** Intenta la patada del lado elegido y actualiza el mensaje y el dibujo. */
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

    /** Crea una etiqueta con el estilo de texto usado en el panel. */
    private JLabel label(String text) {
        JLabel label = new JLabel(text);
        label.setForeground(Color.WHITE);
        return label;
    }

    /** Detiene la partida anterior, crea otra con las opciones actuales e inicia su temporizador. */
    private void startSession() {
        timer.stop();
        runButton.setText("Iniciar simulación");
        controller.restartConfiguredSession();
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

    /** Pausa el temporizador o lo reanuda y devuelve el foco al campo de juego. */
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

    /** Avanza la sesión una vez, detiene el temporizador al terminar o ante una excepción y refresca la vista. */
    private void advanceOneStep(boolean showStepStatus) {
        try {
            controller.step();
            SessionView session = controller.getSessionView();
            if (session.finished()) {
                timer.stop();
                runButton.setText("Iniciar simulación");
                String mode = matchMode.getSelectedItem() == MechanicsTestController.MatchMode.TIME
                        ? "por tiempo" : "por goles";
                status.setText("La partida " + mode + " terminó.");
            } else if (showStepStatus) {
                status.setText("Actualización aplicada.");
            }
        } catch (RuntimeException exception) {
            timer.stop();
            runButton.setText("Iniciar simulación");
            status.setText("Excepción detectada: " + exception.getMessage());
        }
        refreshView();
    }

    /** Actualiza el texto del marcador y solicita redibujar el campo. */
    private void refreshView() {
        SessionView session = controller.getSessionView();
        BallView ball = session.balls().get(0);
        score.setText(String.format("Izquierda %d  :  %d Derecha  ·  %.2f s  ·  balón vx %.1f / vy %.1f%s",
                session.goalsLeft(), session.goalsRight(), session.elapsedSeconds(),
                ball.velocityX(), ball.velocityY(),
                session.finished() ? "     ·     PARTIDA TERMINADA" : ""));
        fieldView.repaint();
    }

    /** Solicita el foco del campo en el hilo de eventos de Swing. */
    private void focusGameField() {
        SwingUtilities.invokeLater(fieldView::requestFocusInWindow);
    }

    private final class FieldView extends JPanel {
        /** Crea el área gráfica y configura su tamaño, fondo y borde. */
        private FieldView() {
            setLayout(new BorderLayout());
            setPreferredSize(new Dimension(760, 420));
            setBackground(new Color(32, 91, 61));
            setBorder(BorderFactory.createLineBorder(new Color(110, 140, 116), 2));
            setFocusable(true);
        }

        /** Dibuja estadio, cancha, arcos, participantes, balón e indicador de velocidad. */
        @Override
        public void paintComponent(Graphics graphics) {
            super.paintComponent(graphics);
            Graphics2D g = (Graphics2D) graphics.create();
            g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            drawStadium(g);
            SessionView session = controller.getSessionView();
            RectangleView court = session.court();
            double x = court.x();
            double y = court.y();
            double width = court.width();
            double height = court.height();
            double scale = Math.min((getWidth() - 50.0) / width, (getHeight() * .52) / height);
            double offsetX = (getWidth() - width * scale) / 2;
            double offsetY = getHeight() - height * scale;

            g.setPaint(new java.awt.GradientPaint(0, (float) offsetY, new Color(87, 190, 48),
                    0, (float) (offsetY + height * scale), new Color(25, 128, 18)));
            g.fill(new Rectangle2D.Double(offsetX, offsetY, width * scale, height * scale));
            for (int stripe = 0; stripe < 8; stripe++) {
                if (stripe % 2 == 0) {
                    g.setColor(new Color(190, 245, 115, 32));
                    g.fill(new Rectangle2D.Double(offsetX + stripe * width * scale / 8, offsetY,
                            width * scale / 8, height * scale));
                }
            }
            g.setColor(Color.WHITE);
            g.setStroke(new BasicStroke(2f));
            g.draw(new Rectangle2D.Double(offsetX, offsetY, width * scale, height * scale));
            g.draw(new Ellipse2D.Double(offsetX + width * scale * .375,
                    offsetY + height * scale * .25, width * scale * .25, height * scale * .5));
            g.drawLine((int) (offsetX + width * scale / 2), (int) offsetY,
                    (int) (offsetX + width * scale / 2), (int) (offsetY + height * scale));
            for (var goal : session.goals()) {
                drawRect(g, goal.x() - x, goal.y() - y, goal.width(), goal.height(),
                        offsetX, offsetY, scale, new Color(237, 191, 87, 120), true);
            }
            drawGoal(g, offsetX - 14, offsetY + height * scale * .42, 28, height * scale * .58);
            drawGoal(g, offsetX + width * scale - 14, offsetY + height * scale * .42, 28, height * scale * .58);
            drawParticipants(g, session.participants().stream().filter(player -> player.side().equals("left")).toList(),
                    x, y, offsetX, offsetY, scale,
                    new Color(71, 151, 255));
            drawParticipants(g, session.participants().stream().filter(player -> player.side().equals("right")).toList(),
                    x, y, offsetX, offsetY, scale,
                    new Color(255, 105, 105));
            for (BallView ball : session.balls()) {
                double radius = ball.radius();
                double ballX = offsetX + (ball.x() - radius - x) * scale;
                double ballY = offsetY + (ball.y() - radius - y) * scale;
                g.setColor(Color.WHITE);
                g.fill(new Ellipse2D.Double(ballX, ballY, radius * 2 * scale, radius * 2 * scale));
                g.setColor(Color.DARK_GRAY);
                g.draw(new Ellipse2D.Double(ballX, ballY, radius * 2 * scale, radius * 2 * scale));
                drawVelocityIndicator(g, ballX + radius * scale, ballY + radius * scale, ball.velocityY());
            }
            if (showHitboxes.isSelected()) {
                drawHitboxOverlay(g, session, x, y, offsetX, offsetY, scale);
            }
            g.dispose();
        }

        /** Dibuja los límites, arcos, hitboxes de jugadores y balones sobre los sprites. */
        private void drawHitboxOverlay(Graphics2D g, SessionView session,
                double courtX, double courtY, double offsetX, double offsetY, double scale) {
            g.setStroke(new BasicStroke(1.5f));
            for (int index = 0; index < session.boundaries().size(); index++) {
                RectangleView box = session.boundaries().get(index);
                drawRect(g, box.x() - courtX, box.y() - courtY, box.width(), box.height(),
                        offsetX, offsetY, scale, new Color(255, 236, 80), false);
            }
            for (RectangleView goal : session.goals()) {
                drawRect(g, goal.x() - courtX, goal.y() - courtY, goal.width(), goal.height(),
                        offsetX, offsetY, scale, new Color(255, 153, 40), false);
            }
            for (ParticipantView participant : session.participants()) {
                drawRect(g, participant.x() - courtX, participant.y() - courtY,
                        participant.width(), participant.height(), offsetX, offsetY, scale,
                        participant.side().equals("left") ? Color.CYAN : Color.MAGENTA, false);
            }
            for (BallView ball : session.balls()) {
                double diameter = ball.radius() * 2 * scale;
                double screenX = offsetX + (ball.x() - ball.radius() - courtX) * scale;
                double screenY = offsetY + (ball.y() - ball.radius() - courtY) * scale;
                g.setColor(Color.WHITE);
                g.draw(new Ellipse2D.Double(screenX, screenY, diameter, diameter));
            }
        }

        /** Dibuja el fondo decorativo del estadio y su tribuna. */
        private void drawStadium(Graphics2D g) {
            int w = getWidth(), h = getHeight();
            g.setPaint(new java.awt.GradientPaint(0, 0, new Color(74, 41, 45), 0, h * .55f,
                    new Color(205, 133, 75)));
            g.fillRect(0, 0, w, h);
            g.setColor(new Color(224, 177, 100));
            g.fillRect(0, h / 3, w, 12);
            g.setColor(new Color(111, 66, 54));
            g.fillRect(0, h / 3 + 12, w, 22);
            java.util.Random crowd = new java.util.Random(19);
            Color[] seats = {new Color(224, 79, 44), new Color(239, 184, 54),
                    new Color(48, 130, 105), new Color(230, 220, 184)};
            for (int row = 0; row < 7; row++) {
                for (int col = 0; col < w / 8; col++) {
                    g.setColor(seats[crowd.nextInt(seats.length)]);
                    int x = col * 8 + crowd.nextInt(3);
                    int y = row * 13 + 16 + crowd.nextInt(4);
                    g.fillOval(x, y, 4, 5);
                }
            }
            g.setPaint(new java.awt.GradientPaint(0, h * .47f, new Color(104, 196, 48),
                    0, h, new Color(35, 135, 22)));
            g.fillRect(0, (int) (h * .47), w, h - (int) (h * .47));
            g.setColor(new Color(4, 19, 94));
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(26, w / 13)));
            String first = "SOCCER";
            int titleX = (w - g.getFontMetrics().stringWidth(first)) / 2;
            int titleY = h / 3 + 2;
            g.setStroke(new BasicStroke(8, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g.drawString(first, titleX, titleY);
            g.setColor(new Color(255, 215, 57));
            g.drawString(first, titleX, titleY);
            g.setColor(Color.WHITE);
            g.setFont(new Font(Font.SANS_SERIF, Font.BOLD, Math.max(20, w / 20)));
            String second = "HEADS";
            g.drawString(second, (w - g.getFontMetrics().stringWidth(second)) / 2, titleY + 38);
        }

        /** Dibuja el marco y la red de un arco en coordenadas de pantalla. */
        private void drawGoal(Graphics2D g, double x, double y, double width, double height) {
            g.setColor(new Color(224, 250, 255, 70));
            g.fill(new Rectangle2D.Double(x, y, width, height));
            g.setColor(new Color(213, 248, 255));
            g.setStroke(new BasicStroke(4f));
            g.draw(new Rectangle2D.Double(x, y, width, height));
            g.setStroke(new BasicStroke(1f));
            for (int i = 1; i < 5; i++) g.drawLine((int)x, (int)(y + i * height / 5), (int)(x + width), (int)(y + i * height / 5));
            for (int i = 1; i < 4; i++) g.drawLine((int)(x + i * width / 4), (int)y, (int)(x + i * width / 4), (int)(y + height));
        }

        /** Dibuja una flecha vertical cuyo sentido y longitud reflejan la velocidad del balón. */
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

        /**
         * Dibuja cada participante con una representación arcade simplificada,
         * escalando su hitbox del dominio al tamaño actual del campo.
         * @param g contexto gráfico donde se dibuja
         * @param participants participantes que se representan
         * @param courtX coordenada horizontal de origen de la cancha
         * @param courtY coordenada vertical de origen de la cancha
         * @param offsetX desplazamiento horizontal en pantalla
         * @param offsetY desplazamiento vertical en pantalla
         * @param scale escala entre unidades de dominio y píxeles
         * @param color color del cuerpo del participante
         */
        private void drawParticipants(Graphics2D g, java.util.List<ParticipantView> participants,
                double courtX, double courtY, double offsetX, double offsetY, double scale, Color color) {
            for (ParticipantView participant : participants) {
                double x = offsetX + (participant.x() - courtX) * scale;
                double y = offsetY + (participant.y() - courtY) * scale;
                double width = participant.width() * scale;
                double height = participant.height() * scale;
                double headSize = Math.min(width * .96, height * .46);
                double headX = x + (width - headSize) / 2;
                double headY = y + height * .02;

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

        /**
         * Convierte un rectángulo de coordenadas de cancha a coordenadas de
         * pantalla, lo rellena si se solicita y siempre dibuja su contorno.
         * @param g contexto gráfico
         * @param x coordenada horizontal relativa a la cancha
         * @param y coordenada vertical relativa a la cancha
         * @param width ancho del rectángulo en unidades de cancha
         * @param height alto del rectángulo en unidades de cancha
         * @param offsetX desplazamiento horizontal de la cancha en pantalla
         * @param offsetY desplazamiento vertical de la cancha en pantalla
         * @param scale escala aplicada a las dimensiones
         * @param color color de relleno y contorno
         * @param fill indica si se rellena antes de dibujar el contorno
         */
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
