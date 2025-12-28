
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

public class Main extends JFrame{
	public File workdir = new File(Utils.getAppData() + "\\HonertisLauncher");
    private JTextField pathField;
    private JLabel currentText;
    private JProgressBar progressBar;

    public Main() {
    	if (!workdir.exists()) workdir.mkdirs();
        setTitle("Installateur du launcher Honertis");
        setSize(550, 750);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        setResizable(false);

        // Fond dégradé
        JPanel mainPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                GradientPaint gp = new GradientPaint(0, 0, new Color(25, 25, 25),
                                                     0, getHeight(), new Color(25, 25, 25));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setLayout(new BorderLayout(20, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        add(mainPanel, BorderLayout.CENTER);

        // Titre
        JLabel title = new JLabel("Honertis Launcher Installer", SwingConstants.CENTER);
        title.setFont(new Font("Arial", Font.BOLD, 30));
        title.setForeground(Color.WHITE);
        mainPanel.add(title, BorderLayout.NORTH);

        // Chemin
        JPanel pathPanel = new JPanel(new BorderLayout(5, 5));
        pathPanel.setOpaque(false);
        pathField = new JTextField(Utils.getAppData() + "");
        
        currentText = new JLabel("Installation du launcher...", SwingConstants.CENTER);
        currentText.setFont(new Font("Arial", Font.BOLD, 22));
        currentText.setForeground(Color.WHITE);
        mainPanel.add(currentText);
        pathPanel.add(currentText, BorderLayout.CENTER);
        mainPanel.add(pathPanel, BorderLayout.CENTER);

        // Installation
        JPanel installPanel = new JPanel(new BorderLayout(5, 5));
        installPanel.setOpaque(false);
        progressBar = new JProgressBar();
        progressBar.setStringPainted(true);
        progressBar.setForeground(new Color(60, 179, 113));
        installPanel.add(progressBar, BorderLayout.SOUTH);
        mainPanel.add(installPanel, BorderLayout.SOUTH);
        
        new Thread(() -> {
            for (int i = 0; i <= 100; i++) {
                final int progress = i;
                SwingUtilities.invokeLater(() -> progressBar.setValue(progress));
                try {
					Thread.sleep(50);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
            }
            SwingUtilities.invokeLater(() -> {
            	try {
					Utils.download("https://github.com/TheAltening156/HonertisLauncher/releases/latest/download/HonertisLauncher.jar", new File(workdir, "Launcher.jar"));
                    JOptionPane.showMessageDialog(this, "Installation terminée !");
				} catch (IOException e1) {
					e1.printStackTrace();
					JOptionPane.showMessageDialog(this, "Une erreur est survenue pendant l'installation !");
					JOptionPane.showMessageDialog(this, e1.getMessage());
				}
            });
        }).start();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new Main().setVisible(true);
        });
    }
}
