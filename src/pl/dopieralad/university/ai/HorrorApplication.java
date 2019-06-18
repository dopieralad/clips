package pl.dopieralad.university.ai;

import CLIPSJNI.Environment;
import CLIPSJNI.PrimitiveValue;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.BreakIterator;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

class HorrorApplication implements ActionListener {
    private JLabel displayLabel;
    private JButton nextButton;
    private JButton prevButton;
    private JPanel choicesPanel;
    private ButtonGroup choicesButtons;
    private ResourceBundle autoResources;

    private Environment clips;
    private boolean isExecuting = false;
    private Thread executionThread;

    private HorrorApplication() {
        try {
            autoResources = ResourceBundle.getBundle("Horror", Locale.getDefault());
        } catch (MissingResourceException mre) {
            mre.printStackTrace();
            return;
        }

        /*================================*/
        /* Create a new JFrame container. */
        /*================================*/

        JFrame jfrm = new JFrame(autoResources.getString("AutoDemo"));

        /*=============================*/
        /* Specify FlowLayout manager. */
        /*=============================*/

        jfrm.getContentPane().setLayout(new GridLayout(3, 1));

        /*=================================*/
        /* Give the frame an initial size. */
        /*=================================*/

        jfrm.setSize(350, 200);

        /*=============================================================*/
        /* Terminate the program when the user closes the application. */
        /*=============================================================*/

        jfrm.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        /*===========================*/
        /* Create the display panel. */
        /*===========================*/

        JPanel displayPanel = new JPanel();
        displayLabel = new JLabel();
        displayPanel.add(displayLabel);

        /*===========================*/
        /* Create the choices panel. */
        /*===========================*/

        choicesPanel = new JPanel();
        choicesButtons = new ButtonGroup();

        /*===========================*/
        /* Create the buttons panel. */
        /*===========================*/

        JPanel buttonPanel = new JPanel();

        prevButton = new JButton(autoResources.getString("Prev"));
        prevButton.setActionCommand("Prev");
        buttonPanel.add(prevButton);
        prevButton.addActionListener(this);

        nextButton = new JButton(autoResources.getString("Next"));
        nextButton.setActionCommand("Next");
        buttonPanel.add(nextButton);
        nextButton.addActionListener(this);

        /*=====================================*/
        /* Add the panels to the content pane. */
        /*=====================================*/

        jfrm.getContentPane().add(displayPanel);
        jfrm.getContentPane().add(choicesPanel);
        jfrm.getContentPane().add(buttonPanel);

        /*========================*/
        /* Load the auto program. */
        /*========================*/

        clips = new Environment();

        clips.load("./resources/Horror.clp");

        clips.reset();
        runAuto();

        /*====================*/
        /* Display the frame. */
        /*====================*/

        jfrm.setVisible(true);
    }

    private void nextUIState() throws Exception {
        /*=====================*/
        /* Get the state-list. */
        /*=====================*/

        String evalStr = "(find-all-facts ((?f state-list)) TRUE)";

        String currentID = clips.eval(evalStr).get(0).getFactSlot("current").toString();

        /*===========================*/
        /* Get the current UI state. */
        /*===========================*/

        evalStr = "(find-all-facts ((?f UI-state)) " +
                "(eq ?f:id " + currentID + "))";

        PrimitiveValue fv = clips.eval(evalStr).get(0);

        /*========================================*/
        /* Determine the Next/Prev button states. */
        /*========================================*/

        if (fv.getFactSlot("state").toString().equals("final")) {
            nextButton.setActionCommand("Restart");
            nextButton.setText(autoResources.getString("Restart"));
            prevButton.setVisible(true);
        } else if (fv.getFactSlot("state").toString().equals("initial")) {
            nextButton.setActionCommand("Next");
            nextButton.setText(autoResources.getString("Next"));
            prevButton.setVisible(false);
        } else {
            nextButton.setActionCommand("Next");
            nextButton.setText(autoResources.getString("Next"));
            prevButton.setVisible(true);
        }

        /*=====================*/
        /* Set up the choices. */
        /*=====================*/

        choicesPanel.removeAll();
        choicesButtons = new ButtonGroup();

        PrimitiveValue pv = fv.getFactSlot("valid-answers");

        String selected = fv.getFactSlot("response").toString();

        for (int i = 0; i < pv.size(); i++) {
            PrimitiveValue bv = pv.get(i);
            JRadioButton rButton;

            if (bv.toString().equals(selected)) {
                rButton = new JRadioButton(autoResources.getString(bv.toString()), true);
            } else {
                rButton = new JRadioButton(autoResources.getString(bv.toString()), false);
            }

            rButton.setActionCommand(bv.toString());
            choicesPanel.add(rButton);
            choicesButtons.add(rButton);
        }

        choicesPanel.repaint();

        /*====================================*/
        /* Set the label to the display text. */
        /*====================================*/

        String theText = autoResources.getString(fv.getFactSlot("display").symbolValue());

        wrapLabelText(displayLabel, theText);

        executionThread = null;

        isExecuting = false;
    }

    /*########################*/
    /* ActionListener Methods */
    /*########################*/

    public void actionPerformed(
            ActionEvent ae) {
        try {
            onActionPerformed(ae);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void runAuto() {
        Runnable runThread = () -> {
            clips.run();

            SwingUtilities.invokeLater(() -> {
                try {
                    nextUIState();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        };

        isExecuting = true;

        executionThread = new Thread(runThread);

        executionThread.start();
    }

    private void onActionPerformed(
            ActionEvent ae) throws Exception {
        if (isExecuting) return;

        /*=====================*/
        /* Get the state-list. */
        /*=====================*/

        String evalStr = "(find-all-facts ((?f state-list)) TRUE)";

        String currentID = clips.eval(evalStr).get(0).getFactSlot("current").toString();

        /*=========================*/
        /* Handle the Next button. */
        /*=========================*/


        final String actionCommand = ae.getActionCommand();
        if ("Next".equals(actionCommand)) {
            if (choicesButtons.getButtonCount() == 0) {
                clips.assertString("(next " + currentID + ")");
            } else {
                clips.assertString("(next " + currentID + " " +
                        choicesButtons.getSelection().getActionCommand() +
                        ")");
            }

            runAuto();
        } else if ("Restart".equals(actionCommand)) {
            clips.reset();
            runAuto();
        } else if ("Prev".equals(actionCommand)) {
            clips.assertString("(prev " + currentID + ")");
            runAuto();
        }
    }

    private void wrapLabelText(
            JLabel label,
            String text) {
        FontMetrics fm = label.getFontMetrics(label.getFont());
        Container container = label.getParent();
        int containerWidth = container.getWidth();
        int textWidth = SwingUtilities.computeStringWidth(fm, text);
        int desiredWidth;

        if (textWidth <= containerWidth) {
            desiredWidth = containerWidth;
        } else {
            int lines = (textWidth + containerWidth) / containerWidth;

            desiredWidth = textWidth / lines;
        }

        BreakIterator boundary = BreakIterator.getWordInstance();
        boundary.setText(text);

        StringBuffer trial = new StringBuffer();
        StringBuilder real = new StringBuilder("<html><center>");

        int start = boundary.first();
        for (int end = boundary.next(); end != BreakIterator.DONE;
             start = end, end = boundary.next()) {
            String word = text.substring(start, end);
            trial.append(word);
            int trialWidth = SwingUtilities.computeStringWidth(fm, trial.toString());
            if (trialWidth > containerWidth) {
                trial = new StringBuffer(word);
                real.append("<br>");
                real.append(word);
            } else if (trialWidth > desiredWidth) {
                trial = new StringBuffer();
                real.append(word);
                real.append("<br>");
            } else {
                real.append(word);
            }
        }

        real.append("</html>");

        label.setText(real.toString());
    }

    public static void main(String[] args) {
        // Create the frame on the event dispatching thread.
        SwingUtilities.invokeLater(HorrorApplication::new);
    }
}