package pl.dopieralad.university.ai;

import CLIPSJNI.PrimitiveValue;
import pl.dopieralad.university.ai.clips.ClipsDecorator;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;

import static pl.dopieralad.university.ai.util.ThrowingRunnable.withException;

class HorrorApplication implements ActionListener {

    private static final ResourceBundle messages = ResourceBundle.getBundle("Horror", Locale.getDefault());

    private JTextPane textPane;
    private JButton nextButton;
    private JButton prevButton;
    private JPanel choicesPanel;
    private ButtonGroup choicesButtons;

    private ClipsDecorator clips = new ClipsDecorator();

    public static void main(String[] args) {
        SwingUtilities.invokeLater(HorrorApplication::new);
    }

    private HorrorApplication() {
        JFrame frame = new JFrame(messages.getString("AutoDemo"));
        frame.getContentPane().setLayout(new GridLayout(3, 1));
        frame.setSize(350, 200);
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

        choicesPanel = new JPanel();
        choicesButtons = new ButtonGroup();

        JPanel buttonPanel = new JPanel();

        prevButton = new JButton(messages.getString("Prev"));
        prevButton.setActionCommand("Prev");
        buttonPanel.add(prevButton);
        prevButton.addActionListener(this);

        nextButton = new JButton(messages.getString("Next"));
        nextButton.setActionCommand("Next");
        buttonPanel.add(nextButton);
        nextButton.addActionListener(this);

        frame.getContentPane().add(displayPanel);
        frame.getContentPane().add(choicesPanel);
        frame.getContentPane().add(buttonPanel);

        clips.load("./resources/Horror.clp");
        clips.reset();
        runAuto();

        frame.setVisible(true);
    }

    private void runAuto() {
        clips.runAsync().thenRun(withException(this::nextUiState));
    }

    private void nextUiState() throws Exception {
        PrimitiveValue primitiveValue = clips.getCurrentState();

        /*========================================*/
        /* Determine the Next/Prev button states. */
        /*========================================*/

        final String state = primitiveValue.getFactSlot("state").toString();
        if ("final".equals(state)) {
            nextButton.setActionCommand("Restart");
            nextButton.setText(messages.getString("Restart"));
            prevButton.setVisible(true);
        } else if ("initial".equals(state)) {
            nextButton.setActionCommand("Next");
            nextButton.setText(messages.getString("Next"));
            prevButton.setVisible(false);
        } else {
            nextButton.setActionCommand("Next");
            nextButton.setText(messages.getString("Next"));
            prevButton.setVisible(true);
        }

        choicesPanel.removeAll();
        choicesButtons = new ButtonGroup();

        PrimitiveValue validAnswers = primitiveValue.getFactSlot("valid-answers");

        String selected = primitiveValue.getFactSlot("response").toString();

        for (int i = 0; i < validAnswers.size(); i++) {
            PrimitiveValue validAnswer = validAnswers.get(i);
            JRadioButton radioButton;

            if (validAnswer.toString().equals(selected)) {
                radioButton = new JRadioButton(messages.getString(validAnswer.toString()), true);
            } else {
                radioButton = new JRadioButton(messages.getString(validAnswer.toString()), false);
            }

            radioButton.setActionCommand(validAnswer.toString());
            choicesPanel.add(radioButton);
            choicesButtons.add(radioButton);
        }

        choicesPanel.repaint();

        final String text = messages.getString(primitiveValue.getFactSlot("display").symbolValue());

        textPane.setText(text);
    }

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        withException(() -> onActionPerformed(actionEvent)).run();
    }

    private void onActionPerformed(ActionEvent actionEvent) throws Exception {
        final String currentId = clips.getCurrentId();
        final String actionCommand = actionEvent.getActionCommand();

        if ("Next".equals(actionCommand)) {
            if (choicesButtons.getButtonCount() == 0) {
                clips.assertString(String.format("(next %s)", currentId));
            } else {
                final String answer = choicesButtons.getSelection().getActionCommand();
                clips.assertString(String.format("(next %s %s)", currentId, answer));
            }

            runAuto();
        } else if ("Restart".equals(actionCommand)) {
            clips.reset();
            runAuto();
        } else if ("Prev".equals(actionCommand)) {
            clips.assertString(String.format("(prev %s)", currentId));
            runAuto();
        }
    }
}