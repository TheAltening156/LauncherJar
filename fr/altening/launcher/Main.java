package fr.altening.launcher;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.imageio.ImageIO;
import javax.management.RuntimeErrorException;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.altening.utils.Utils;
import fr.litarvan.openauth.microsoft.MicrosoftAuthenticator;

@SuppressWarnings("serial")
public class Main extends JFrame{
    public static String launcherVersion = "1.2.3";
    public Auth auth;
    public static Main window;
    public static JTextField nameField;
    public static JButton launchButton;
    public static JButton btnAction;
    
    public static void main(String[] args) {
		if (!Utils.workdir.exists()) {
			Utils.workdir.mkdirs();
			System.out.println("[Launcher] Created " + Utils.workdir);
		} 
		SwingUtilities.invokeLater(() -> {
			(window = new Main()).setVisible(true);
		});
    	
    }
    
    public Main() {
        // Configuration de la fenêtre
        setTitle("Honertis Launcher " + launcherVersion);
        setSize(500, 250);
        setLocationRelativeTo(null); // Centrer la fenêtre
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        try {
        	setIconImage(ImageIO.read(Main.class.getResource("/assets/icon32.png")).getScaledInstance(32, 32, 0));
        } catch (IOException e) {
        	e.printStackTrace();
        }
        
        // Fond avec panneau personnalisé
        JPanel mainPanel = new JPanel();
        mainPanel.setBackground(new Color(34, 34, 34));
        mainPanel.setLayout(new BorderLayout(0, 20));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
        setContentPane(mainPanel);

        // Texte d'accueil
        JLabel text = new JLabel("Bienvenue sur le lanceur de Honertis !", SwingConstants.CENTER);
        text.setForeground(Color.WHITE);
        text.setFont(new Font("Segoe UI", Font.BOLD, 16));
        text.setBorder(null);
        text.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(new Color(34, 34, 34));
        topBar.add(text, BorderLayout.CENTER);
        mainPanel.add(topBar, BorderLayout.PAGE_START);

        JLabel logoLabel = new JLabel("Honertis Launcher " + launcherVersion);
        logoLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        logoLabel.setForeground(Color.WHITE);
        logoLabel.setHorizontalAlignment(SwingConstants.CENTER);
        mainPanel.add(logoLabel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(new Color(34, 34, 34));
        mainPanel.add(centerPanel, BorderLayout.CENTER);

        JPanel nameLinePanel = new JPanel();
        nameLinePanel.setLayout(new BoxLayout(nameLinePanel, BoxLayout.X_AXIS));
        nameLinePanel.setBackground(new Color(34, 34, 34));

        nameField = new JTextField();
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        nameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        nameField.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        String placeholder = "Pseudo (cracké)";
        nameField.setText(placeholder);
        nameField.setForeground(Color.GRAY);

        nameField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (nameField.getText().equals(placeholder)) {
                	nameField.setText("");
                    nameField.setForeground(Color.BLACK);
                }
            }

            @Override
            public void focusLost(FocusEvent e) {
                if (nameField.getText().isEmpty()) {
                	nameField.setText(placeholder);
                    nameField.setForeground(Color.GRAY);
                }
            }
        });

        btnAction = new JButton("Microsoft");
        btnAction.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnAction.setPreferredSize(new Dimension(100, 40));
        btnAction.setMaximumSize(new Dimension(120, 40));
        btnAction.setBackground(new Color(98, 142, 203));
        btnAction.setForeground(Color.WHITE);
        btnAction.setFocusPainted(false);
        btnAction.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnAction.setToolTipText("Connexion Microsoft");

        nameLinePanel.add(nameField);
        nameLinePanel.add(Box.createRigidArea(new Dimension(10, 0)));
        nameLinePanel.add(btnAction);

        centerPanel.add(nameLinePanel);
        centerPanel.add(Box.createRigidArea(new Dimension(0, 20)));

        launchButton = new JButton("Lancer le jeu");
        launchButton.setBackground(new Color(70, 130, 180));
        launchButton.setForeground(Color.WHITE);
        launchButton.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        launchButton.setFocusPainted(false);
        launchButton.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        launchButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        launchButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        SwingUtilities.invokeLater(() -> {
            launchButton.requestFocusInWindow();
        });
        
        centerPanel.add(launchButton);
        JPanel bottomLinePanel = new JPanel();
        bottomLinePanel.setLayout(new BoxLayout(bottomLinePanel, BoxLayout.X_AXIS));
        bottomLinePanel.setBackground(new Color(34, 34, 34));
        
        JComboBox<String> versionCombo = new JComboBox<String>();
        SwingUtilities.invokeLater(() -> {
        	for (String ver : Utils.getTagsVersions()) 
        		versionCombo.addItem(ver);
        	
        });
                
        versionCombo.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        versionCombo.setMaximumSize(new Dimension(120, 40));
        versionCombo.setPreferredSize(new Dimension(120, 40));
        versionCombo.setBackground(new Color(60, 60, 60));
        versionCombo.setForeground(Color.WHITE);
        versionCombo.setFocusable(false);
        
        bottomLinePanel.add(launchButton);
        bottomLinePanel.add(Box.createRigidArea(new Dimension(20, 0))); 
        bottomLinePanel.add(versionCombo);
        centerPanel.add(bottomLinePanel);
        
        MicrosoftAuthenticator authenticator = new MicrosoftAuthenticator();
        btnAction.addActionListener(e -> {
        	launchButton.setEnabled(false);
        	btnAction.setEnabled(false);
        	nameField.setEnabled(false);
            try {
            	authenticator.loginWithAsyncWebview().thenAccept(result -> {
                	if (result != null) {
                		if (this.auth == null) {
                        	this.auth = new Auth(result.getProfile().getName(), result.getProfile().getId(), result.getAccessToken(), true);
                        	JOptionPane.showMessageDialog(null, "Connecté avec le compte " + result.getProfile().getName(), "Connexion avec succès !", 1);
                    	} else {
                            JOptionPane.showMessageDialog(null, "Une erreur est survenue, veuillez r\u00e9essayer.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    	}
                	} else {
                    	JOptionPane.showMessageDialog(null, "Veuillez r\u00e9executer le launcher pour executer la connexion microsoft de nouveau.", "Info", 1);
                    	nameField.setEnabled(true);
                	}
                }).exceptionally(ex -> {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Erreur", JOptionPane.ERROR_MESSAGE);
                    nameField.setEnabled(true);
                    return null;
                });
            }
            catch (Error exc) {
            	exc.printStackTrace();
                JOptionPane.showMessageDialog(null, exc.getStackTrace(), "Erreur", JOptionPane.ERROR_MESSAGE);
                nameField.setEnabled(true);
            }
            launchButton.setEnabled(true);
        });
        launchButton.addActionListener(e -> {
            String username = nameField.getText().trim();
            if (this.auth == null) {
                if (username.isEmpty() || !Utils.isValidMinecraftUsername(username)) {
                    JOptionPane.showMessageDialog(null, "Veuillez entrer un nom d'utilisateur valide.\nOu authentifiez vous avec votre compte microsoft.", "Erreur", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                if (!username.isEmpty() && Utils.isValidMinecraftUsername(username)) {
                	launchButton.setEnabled(false);
                	btnAction.setEnabled(false);
                    this.auth = new Auth(username, "", "0", false);
                    this.startGame(versionCombo);
                    this.auth = null;
                }
            }
            if (this.auth != null && this.auth.isMicrosoftAccount()) {
            	launchButton.setEnabled(false);
                this.startGame(versionCombo);
            }
        });
    }
    private void startGame(JComboBox<String> versionCombo) {
    	if (auth != null) {
            String selectedVersion = (String) versionCombo.getSelectedItem();
			Utils.launch(auth, selectedVersion);
        }
	}
}
