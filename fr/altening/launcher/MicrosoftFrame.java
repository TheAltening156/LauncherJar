package fr.altening.launcher;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fr.altening.AccountData;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticationException;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;

public class MicrosoftFrame {

	public boolean enableName = true;
	
	public MicrosoftFrame(Map<String, AccountData> list) {
		SwingUtilities.invokeLater(() -> {
			try {
				UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
			} catch (Exception ignored) {
			}
			JFrame frame = new JFrame("Connexion");
			frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
			frame.setSize(400, 150);
			frame.setLocationRelativeTo(null);
			frame.setResizable(false);
			frame.setUndecorated(false);
			try {
				frame.setIconImage(
						ImageIO.read(getClass().getResource("/assets/icon32.png")).getScaledInstance(32, 32, 0));
			} catch (IOException e) {
				e.printStackTrace();
			}
			JPanel mainPanel = new JPanel(new BorderLayout(10, 10));

			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));

			JLabel label = new JLabel("Voulez vous vous reconnecter à");

			JComboBox<String> comboBox = new JComboBox<>();
			for (String entry : list.keySet()) {
				comboBox.addItem(entry);
			}
			comboBox.setPreferredSize(new Dimension(150, 25));

			topPanel.add(label);
			topPanel.add(comboBox);

			JButton yesButton = new JButton("Oui");
			JButton noButton = new JButton("Non");
			JButton cancelButton = new JButton("Annuler");
			
			JPanel buttonPanel = new JPanel();
			buttonPanel.add(yesButton);
			buttonPanel.add(noButton);
			buttonPanel.add(cancelButton);

			yesButton.addActionListener(e -> {
				String choix = (String) comboBox.getSelectedItem();
				try {
					Main.main.loginMicrosoft(new MicrosoftAuthenticator().loginWithRefreshToken(list.get(choix).refreshToken));
					enableName = false;
				} catch (MicrosoftAuthenticationException e1) {
					e1.printStackTrace();
                    JOptionPane.showMessageDialog(Main.main, e1.getStackTrace(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    enableName = true;
				}
                frame.dispose();
			});

			noButton.addActionListener(e -> {
				Main.main.microsoftLoginFrame();
                frame.dispose();
			});
			
			cancelButton.addActionListener(e -> {
				frame.dispose();
			});
			
			frame.addWindowListener(new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					Main.main.launchButton.setEnabled(true);
					Main.main.nameField.setEnabled(enableName && Main.main.auth == null);
					Main.main.btnAction.setEnabled(true);
				}
			});

			mainPanel.add(topPanel, BorderLayout.CENTER);
			mainPanel.add(buttonPanel, BorderLayout.SOUTH);

			frame.add(mainPanel);
			frame.setVisible(true);
		});
	}

}
