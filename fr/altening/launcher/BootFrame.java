package fr.altening.launcher;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.UIManager;

public class BootFrame extends JFrame {
	private JProgressBar progressBar;
	public JLabel label;
	public JLabel label2;

	public BootFrame() {
	    try {
	        UIManager.setLookAndFeel(
	            UIManager.getSystemLookAndFeelClassName()
	        );
	    } catch (Exception ignored) {}

	    setTitle("Launcher");
	    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	    setSize(420, 120);
	    setResizable(false);
	    setLocationRelativeTo(null);

	    label = new JLabel("Lancement du jeu...", JLabel.CENTER);
	    label.setFont(label.getFont().deriveFont(Font.PLAIN, 14f));

	    label2 = new JLabel("Initialisation...", JLabel.CENTER);
	    label2.setFont(label2.getFont().deriveFont(Font.PLAIN, 12f));

	    progressBar = new JProgressBar();
	    progressBar.setIndeterminate(true);

	    JPanel textPanel = new JPanel(new GridBagLayout());
	    GridBagConstraints gbc = new GridBagConstraints();

	    gbc.gridx = 0;
	    gbc.gridy = 0;
	    gbc.insets = new Insets(0, 0, 5, 0);
	    gbc.anchor = GridBagConstraints.CENTER;

	    textPanel.add(label, gbc);

	    gbc.gridy = 1;
	    textPanel.add(label2, gbc);

	    setLayout(new BorderLayout());
	    add(textPanel, BorderLayout.CENTER);
	    add(progressBar, BorderLayout.SOUTH);

	    setVisible(true);
	}

    
    public static void main(String[] args) {
		new BootFrame().setVisible(true);
	}
}