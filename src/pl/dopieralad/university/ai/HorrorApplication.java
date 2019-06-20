package pl.dopieralad.university.ai;

import CLIPSJNI.PrimitiveValue;
import pl.dopieralad.university.ai.clips.ClipsDecorator;

import javax.swing.*;
import java.awt.*;
import java.util.Locale;
import java.util.ResourceBundle;

import static pl.dopieralad.university.ai.util.ThrowingRunnable.withException;

class HorrorApplication {

    private static final ResourceBundle messages = ResourceBundle.getBundle("Horror", Locale.getDefault());

    private JTextPane textPane;
    private JButton startButton;
    private JButton goBackButton;
    private JButton restartButton;
    private JPanel buttonsPanel;

    private ClipsDecorator clips = new ClipsDecorator();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HorrorApplication::new);
    }

    private HorrorApplication() {
        JFrame frame = new JFrame(messages.getString("Title"));
        frame.getContentPane().setLayout(new GridLayout(3, 1));
        frame.setSize(500, 150);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        textPane = new JTextPane();
        textPane.setOpaque(false);
        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.setBackground(UIManager.getColor("Label.background"));
        textPane.setFont(UIManager.getFont("Label.font"));
        textPane.setBorder(UIManager.getBorder("Label.border"));

        JPanel displayPanel = new JPanel();
        displayPanel.add(textPane);

        buttonsPanel = new JPanel();

        startButton = new JButton(messages.getString("Start"));
        startButton.addActionListener(event -> withException(() -> {
            final String currentId = clips.getCurrentId();
            clips.assertString(String.format("(next %s)", currentId));
            runAuto();
        }).run());

        goBackButton = new JButton(messages.getString("GoBack"));
        goBackButton.addActionListener(event -> withException(() -> {
            final String currentId = clips.getCurrentId();
            clips.assertString(String.format("(prev %s)", currentId));
            runAuto();
        }).run());

        restartButton = new JButton(messages.getString("Restart"));
        restartButton.addActionListener(event -> {
            clips.reset();
            runAuto();
        });

        frame.getContentPane().add(displayPanel);
        frame.getContentPane().add(buttonsPanel);

        clips.load("./resources/Horror.clp");
        clips.reset();
        runAuto();

        frame.setVisible(true);
    }

    private void runAuto() {
        clips.runAsync().thenRun(withException(this::nextUiState));
    }

    private void nextUiState() throws Exception {
        final PrimitiveValue currentState = clips.getCurrentState();

        buttonsPanel.removeAll();

        final String state = currentState.getFactSlot("state").toString();
        if ("final".equals(state)) {
            buttonsPanel.add(goBackButton);
            buttonsPanel.add(restartButton);
        } else if ("initial".equals(state)) {
            buttonsPanel.add(startButton);
        } else {
            buttonsPanel.add(goBackButton);
        }

        PrimitiveValue validAnswers = currentState.getFactSlot("valid-answers");

        for (int i = 0; i < validAnswers.size(); i++) {
            PrimitiveValue validAnswer = validAnswers.get(i);

            final JButton answerButton = new JButton(messages.getString(validAnswer.toString()));
            answerButton.addActionListener(event -> withException(() -> {
                final String currentId = clips.getCurrentId();
                clips.assertString(String.format("(next %s %s)", currentId, validAnswer));
                runAuto();
            }).run());

            buttonsPanel.add(answerButton);
        }

        buttonsPanel.repaint();

        final String text = messages.getString(currentState.getFactSlot("display").symbolValue());

        textPane.setText(text);
    }
}