package presentation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Image;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

import domain.Game;

/** Presenta el marcador final y datos disponibles de la partida terminada. */
public class ResultsPanel extends BasePanel {
    private static final String COURT_IMAGE = "/images/ui/court@4x-8.png";
    private static final int CHARACTER_IMAGE_SIZE = 62;
    private static final int COUNTRY_FLAG_SIZE = 28;

    private final NavigationController navigationController;
    private JLabel leftAnimalLabel;
    private JLabel rightAnimalLabel;
    private JLabel leftHeadLabel;
    private JLabel rightHeadLabel;
    private JLabel leftCountryFlagLabel;
    private JLabel rightCountryFlagLabel;
    private JLabel leftScoreLabel;
    private JLabel rightScoreLabel;
    private JLabel outcomeLabel;
    private JLabel finalScoreLabel;
    private JLabel durationValueLabel;
    private JLabel modeValueLabel;
    private JLabel courtValueLabel;
    private JButton storeButton;
    private JButton mainMenuButton;
    private String leftWinnerText = "¡VICTORIA DEL EQUIPO IZQUIERDO!";
    private String rightWinnerText = "¡VICTORIA DEL EQUIPO DERECHO!";

    /** Crea la pantalla de resultados y conecta sus botones de navegación. */
    public ResultsPanel(NavigationController navigationController) {
        this.navigationController = navigationController;
        prepareView();
        prepareController();
    }

    /** Construye el fondo, el marcador y la tarjeta de resumen. */
    @Override
    public void prepareView() {
        setLayout(new BorderLayout());
        setBackground(Theme.BACKGROUND);

        JPanel background = new CourtBackground();
        background.setLayout(new BorderLayout(0, 8));
        background.setBorder(BorderFactory.createEmptyBorder(12, 22, 18, 22));
        background.add(prepareViewHeader(), BorderLayout.NORTH);
        background.add(prepareViewResult(), BorderLayout.CENTER);
        add(background, BorderLayout.CENTER);
    }

    /** Crea la franja de título y marcador final. */
    private JPanel prepareViewHeader() {
        JPanel header = new JPanel(new BorderLayout(14, 0));
        header.setOpaque(false);

        JLabel title = new JLabel("06  ·  RESULTADOS");
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setForeground(Color.WHITE);
        header.add(title, BorderLayout.NORTH);

        RoundedPanel scoreboard = new RoundedPanel(Theme.BACKGROUND, 18);
        scoreboard.setLayout(new GridLayout(1, 5, 6, 0));
        scoreboard.setBorder(BorderFactory.createEmptyBorder(7, 14, 7, 14));
        JPanel leftCharacter = prepareViewCharacter(true);
        JPanel rightCharacter = prepareViewCharacter(false);
        leftScoreLabel = createScoreLabel();
        rightScoreLabel = createScoreLabel();
        scoreboard.add(leftCharacter);
        scoreboard.add(leftScoreLabel);
        scoreboard.add(createScoreSeparator());
        scoreboard.add(rightScoreLabel);
        scoreboard.add(rightCharacter);
        header.add(scoreboard, BorderLayout.CENTER);
        return header;
    }

    /** Arma el nombre, la bandera y la cabeza en el orden correspondiente a cada lado. */
    private JPanel prepareViewCharacter(boolean left) {
        JPanel character = new JPanel();
        character.setOpaque(false);
        character.setLayout(new BoxLayout(character, BoxLayout.X_AXIS));

        JLabel animalLabel = createAnimalLabel();
        JLabel headLabel = createImageLabel(CHARACTER_IMAGE_SIZE);
        JLabel flagLabel = createImageLabel(COUNTRY_FLAG_SIZE);
        if (left) {
            leftAnimalLabel = animalLabel;
            leftHeadLabel = headLabel;
            leftCountryFlagLabel = flagLabel;
        } else {
            rightAnimalLabel = animalLabel;
            rightHeadLabel = headLabel;
            rightCountryFlagLabel = flagLabel;
        }

        JPanel text = new JPanel();
        text.setOpaque(false);
        text.setLayout(new BoxLayout(text, BoxLayout.Y_AXIS));
        text.add(animalLabel);
        text.add(flagLabel);

        if (left) {
            character.add(text);
            character.add(Box.createHorizontalStrut(7));
            character.add(headLabel);
        } else {
            character.add(headLabel);
            character.add(Box.createHorizontalStrut(7));
            character.add(text);
        }
        return character;
    }

    /** Construye la tarjeta central con ganador, marcador y datos disponibles. */
    private JPanel prepareViewResult() {
        JPanel holder = new JPanel(new java.awt.GridBagLayout());
        holder.setOpaque(false);

        RoundedPanel card = new RoundedPanel(Theme.BACKGROUND, 32);
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBorder(BorderFactory.createEmptyBorder(20, 30, 20, 30));
        card.setPreferredSize(new Dimension(500, 410));

        JLabel trophy = new JLabel("🏆", SwingConstants.CENTER);
        trophy.setFont(new Font("SansSerif", Font.PLAIN, 36));
        trophy.setAlignmentX(CENTER_ALIGNMENT);
        card.add(trophy);
        card.add(Box.createVerticalStrut(4));

        outcomeLabel = new JLabel("PARTIDO FINALIZADO", SwingConstants.CENTER);
        outcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        outcomeLabel.setForeground(Theme.DARK_GREEN);
        outcomeLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(outcomeLabel);

        finalScoreLabel = new JLabel("Marcador final: 0 - 0", SwingConstants.CENTER);
        finalScoreLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
        finalScoreLabel.setForeground(Theme.LABEL_COLOR);
        finalScoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        card.add(Box.createVerticalStrut(5));
        card.add(finalScoreLabel);
        card.add(Box.createVerticalStrut(18));
        card.add(prepareViewSummary());
        card.add(Box.createVerticalGlue());

        storeButton = new RoundedButton("Tienda de mejoras", Theme.SAND,
                Theme.DARK_GREEN, Theme.SAND_BORDER, 14);
        storeButton.setAlignmentX(CENTER_ALIGNMENT);
        storeButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(storeButton);
        card.add(Box.createVerticalStrut(8));

        mainMenuButton = new RoundedButton("Volver al menú principal", Theme.GREEN,
                Color.WHITE, Theme.GREEN, 14);
        mainMenuButton.setAlignmentX(CENTER_ALIGNMENT);
        mainMenuButton.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        card.add(mainMenuButton);

        holder.add(card);
        return holder;
    }

    /** Muestra duración, modalidad y tamaño de cancha, datos disponibles en la sesión. */
    private JPanel prepareViewSummary() {
        JPanel summary = new JPanel(new GridLayout(1, 3, 8, 0));
        summary.setOpaque(false);
        summary.setMaximumSize(new Dimension(Integer.MAX_VALUE, 82));
        durationValueLabel = createSummaryCell(summary, "TIEMPO JUGADO");
        modeValueLabel = createSummaryCell(summary, "MODALIDAD");
        courtValueLabel = createSummaryCell(summary, "CANCHA");
        return summary;
    }

    /** Añade una celda de resumen y devuelve su etiqueta de valor. */
    private JLabel createSummaryCell(JPanel summary, String title) {
        RoundedPanel cell = new RoundedPanel(Theme.INNER_BACKGROUND, 14);
        cell.setLayout(new BorderLayout(0, 5));
        cell.setBorder(BorderFactory.createEmptyBorder(9, 5, 9, 5));
        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
        titleLabel.setForeground(Theme.LABEL_COLOR);
        JLabel valueLabel = new JLabel("—", SwingConstants.CENTER);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
        valueLabel.setForeground(Theme.DARK_GREEN);
        cell.add(titleLabel, BorderLayout.NORTH);
        cell.add(valueLabel, BorderLayout.CENTER);
        summary.add(cell);
        return valueLabel;
    }

    private JLabel createAnimalLabel() {
        JLabel label = new JLabel("", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(Theme.DARK_GREEN);
        return label;
    }

    private JLabel createImageLabel(int size) {
        JLabel label = new JLabel();
        label.setPreferredSize(new Dimension(size, size));
        label.setMinimumSize(new Dimension(size, size));
        label.setMaximumSize(new Dimension(size, size));
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setVerticalAlignment(SwingConstants.CENTER);
        return label;
    }

    private JLabel createScoreLabel() {
        JLabel label = new JLabel("0", SwingConstants.CENTER);
        label.setFont(new Font("SansSerif", Font.BOLD, 24));
        label.setForeground(Theme.DARK_GREEN);
        return label;
    }

    private JLabel createScoreSeparator() {
        JLabel separator = new JLabel(":", SwingConstants.CENTER);
        separator.setFont(new Font("SansSerif", Font.BOLD, 22));
        separator.setForeground(Theme.LABEL_COLOR);
        return separator;
    }

    /** Actualiza nombres, marcador y configuración para la partida preparada. */
    public void setMatchConfiguration(MatchConfiguration configuration) {
        CharacterInfo leftCharacter = characterInfo(configuration.playerTwoCountryCode());
        CharacterInfo rightCharacter = characterInfo(configuration.playerOneCountryCode());
        leftWinnerText = "¡VICTORIA PARA " + leftCharacter.winnerName() + "!";
        rightWinnerText = "¡VICTORIA PARA " + rightCharacter.winnerName() + "!";
        setCharacter(leftAnimalLabel, leftHeadLabel, leftCountryFlagLabel,
                configuration.playerTwoCountryCode(), configuration.playerTwoHeadImage());
        setCharacter(rightAnimalLabel, rightHeadLabel, rightCountryFlagLabel,
                configuration.playerOneCountryCode(), configuration.playerOneHeadImage());

        boolean byTime = configuration.typeMatch().contains("ByTime");
        modeValueLabel.setText(byTime ? "Por tiempo" : "Por goles");
        if (configuration.parametersRules().size() >= 2) {
            courtValueLabel.setText(configuration.parametersRules().get(1) + " × "
                    + configuration.parametersRules().get(0));
        }
    }

    /** Actualiza la vista con el marcador y el tiempo expuestos por Game.
     * @param game fachada que entrega el estado de la sesión
     */
    public void updateResult(Game game) {
        int leftGoals = game.getGoalsLeft();
        int rightGoals = game.getGoalsRight();
        leftScoreLabel.setText(Integer.toString(leftGoals));
        rightScoreLabel.setText(Integer.toString(rightGoals));
        finalScoreLabel.setText("Marcador final: " + leftGoals + " - " + rightGoals);

        if (leftGoals == rightGoals) {
            outcomeLabel.setText("¡EMPATE!");
        } else {
            outcomeLabel.setText(leftGoals > rightGoals
                    ? leftWinnerText
                    : rightWinnerText);
        }

        int elapsedSeconds = (int) game.getElapsedSeconds();
        durationValueLabel.setText(String.format("%02d:%02d",
                elapsedSeconds / 60, elapsedSeconds % 60));
    }

    /** Configura los botones de tienda y retorno al menú principal. */
    @Override
    public void prepareController() {
        prepareControllerStore();
        prepareControllerMainMenu();
    }

    private void prepareControllerStore() {
        storeButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.STORE_IMPROVEMENT));
    }

    private void prepareControllerMainMenu() {
        mainMenuButton.addActionListener(event ->
                navigationController.showScreen(NavigationController.MAIN_MENU));
    }

    private void setCharacter(JLabel animalLabel, JLabel headLabel, JLabel flagLabel,
            String countryCode, String imageFile) {
        CharacterInfo character = characterInfo(countryCode);
        animalLabel.setText(character.animal());
        animalLabel.setToolTipText(character.country());
        URL imageUrl = getClass().getResource("/images/outfits/heads/" + imageFile);
        if (imageUrl != null) {
            Image image = new ImageIcon(imageUrl).getImage().getScaledInstance(
                    CHARACTER_IMAGE_SIZE, CHARACTER_IMAGE_SIZE, Image.SCALE_SMOOTH);
            headLabel.setIcon(new ImageIcon(image));
        } else {
            headLabel.setIcon(null);
        }

        String flagFile = countryFlagFileName(countryCode);
        URL flagUrl = flagFile == null ? null
                : getClass().getResource("/images/characters/countries/" + flagFile);
        if (flagUrl != null) {
            Image flag = new ImageIcon(flagUrl).getImage().getScaledInstance(
                    COUNTRY_FLAG_SIZE, COUNTRY_FLAG_SIZE, Image.SCALE_SMOOTH);
            flagLabel.setIcon(new ImageIcon(flag));
        } else {
            flagLabel.setIcon(null);
        }
        flagLabel.setToolTipText(character.country());
    }

    private String countryFlagFileName(String countryCode) {
        return switch (countryCode == null ? "" : countryCode.toUpperCase(java.util.Locale.ROOT)) {
            case "KR" -> "corea-del-sur@4x-8.png";
            case "CM" -> "camerun@4x-8.png";
            case "NG" -> "nigeria@4x-8.png";
            case "US" -> "estados-unidos@4x-8.png";
            default -> null;
        };
    }

    private CharacterInfo characterInfo(String countryCode) {
        return switch (countryCode == null ? "" : countryCode.toUpperCase(java.util.Locale.ROOT)) {
            case "KR" -> new CharacterInfo("Tigre", "Corea del Sur", "EL TIGRE");
            case "CM" -> new CharacterInfo("Leopardo", "Camerún", "EL LEOPARDO");
            case "NG" -> new CharacterInfo("Lobo", "Nigeria", "EL LOBO");
            case "US" -> new CharacterInfo("Oso", "Estados Unidos", "EL OSO");
            default -> new CharacterInfo("Personaje", "País seleccionado", "EL PERSONAJE");
        };
    }

    /** Datos de texto usados para identificar al personaje en los resultados. */
    private static final class CharacterInfo {
        private final String animal;
        private final String country;
        private final String winnerName;

        /** Crea los datos descriptivos del personaje.
         * @param animal animal asociado
         * @param country país asociado
         * @param winnerName nombre que se muestra como ganador
         */
        private CharacterInfo(String animal, String country, String winnerName) {
            this.animal = animal;
            this.country = country;
            this.winnerName = winnerName;
        }

        private String animal() { return animal; }
        private String country() { return country; }
        private String winnerName() { return winnerName; }
    }

    /** Dibuja la cancha como fondo y la oscurece para resaltar la tarjeta. */
    private static final class CourtBackground extends JPanel {
        private final Image image = loadCourtImage();

        private CourtBackground() {
            setOpaque(true);
            setBackground(new Color(0x247FA0));
        }

        @Override
        protected void paintComponent(java.awt.Graphics graphics) {
            super.paintComponent(graphics);
            if (image != null) {
                graphics.drawImage(image, 0, 0, getWidth(), getHeight(), this);
            }
            java.awt.Graphics2D g = (java.awt.Graphics2D) graphics.create();
            g.setColor(new Color(15, 35, 35, 105));
            g.fillRect(0, 0, getWidth(), getHeight());
            g.dispose();
        }

        private static Image loadCourtImage() {
            URL url = ResultsPanel.class.getResource(COURT_IMAGE);
            return url == null ? null : new ImageIcon(url).getImage();
        }
    }
}
