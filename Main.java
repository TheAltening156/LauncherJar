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
	public static File workdir = new File(Utils.getAppData() + "/.honertis");
    public static File jsonFile = new File(workdir, "versions/1.8.8/1.8.8.json");
    public String launcherVersion = "1.2.2";
    public Auth auth;
    public static Main window;
    public JTextField nameField;
    public JButton launchButton;
    public JButton btnAction;
    
    public static void main(String[] args) {
		if (!workdir.exists()) {
			workdir.mkdirs();
			System.out.println("created " + workdir);
		} 
		SwingUtilities.invokeLater(() -> {
			(window = new Main()).setVisible(true);
		});
    	
    }
    String[] versions = new String[] {"1.8"};

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
        	for (String ver : (versions = getTagsVersions())) 
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
			launch(auth, selectedVersion);
        }
	}
    
    public static void downloadLibraries() {
		try {
	        if (!jsonFile.exists()) throw new FileNotFoundException("1.8.8.json manquant");
	        JSONObject root = new JSONObject(new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8));
	        JSONArray libraries = root.getJSONArray("libraries");
	
	        File libDir = new File(workdir, "lib");
	        if (!libDir.exists()) libDir.mkdirs();
	
	        for (int i = 0; i < libraries.length(); i++) {
	            JSONObject lib = libraries.getJSONObject(i);
	            String name = lib.getString("name");
	
	            // Ex: "com.google.code.gson:gson:2.2.4"
	            String[] parts = name.split(":");
	            if (parts.length < 3) continue;
	
	            String group = parts[0].replace('.', '/');
	            String artifact = parts[1];
	            String version = parts[2];
	
	            String path = group + "/" + artifact + "/" + version + "/" + artifact + "-" + version + ".jar";
	            String downloadUrl = null;
	            try {
	            	downloadUrl = lib.getJSONObject("downloads").getJSONObject("artifact").getString("url");
	            } catch (JSONException e) {
	            	
	            }
	            File dest = new File(libDir, artifact + "-" + version + ".jar");
	            if (!dest.exists()) {
	            	try {
						Utils.download(downloadUrl, dest);
						System.out.println("[OK] Lib téléchargée : " + artifact);
					} catch (IOException e) {
						 System.err.println("[Erreur] Échec lib : " + artifact);
					}
	            }
	        }
		} catch (JSONException | IOException e) {
			e.printStackTrace();
		}
    }
    public static void downloadAssets() {
    	try {
	        JSONObject root = new JSONObject(new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8));
	        String assetIndexName = root.getString("assets");
	
	        // Télécharger l’index
	        String assetIndexUrl = "https://pixelpc.fr/honertis/" + assetIndexName + ".json";
	        File indexFile = new File(workdir, "assets/indexes/" + assetIndexName + ".json");
	        if (!indexFile.exists()) {
	            indexFile.getParentFile().mkdirs();
	            try {
					Utils.download(assetIndexUrl, indexFile);
					System.out.println("[OK] Index assets téléchargé.");
				} catch (IOException e) {
					System.err.println("[Erreur] Impossible de télécharger les index assets téléchargé.");
					e.printStackTrace();
				}
	        }
	
	        JSONObject index = new JSONObject(new String(Files.readAllBytes(indexFile.toPath()), StandardCharsets.UTF_8)).getJSONObject("objects");
	        File objectDir = new File(workdir, "assets/objects");
	
	        for (String key : index.keySet()) {
	            JSONObject entry = index.getJSONObject(key);
	            String hash = entry.getString("hash");
	            String subDir = hash.substring(0, 2);
	            String url = "https://resources.download.minecraft.net/" + subDir + "/" + hash;
	
	            File assetFile = new File(objectDir, subDir + "/" + hash);
	            if (!assetFile.exists()) {
	                assetFile.getParentFile().mkdirs();
	                try {
		                Utils.download(url, assetFile);
	                    System.out.println("[OK] Asset téléchargé : " + key);
	                } catch (Exception e) {
	                    System.err.println("[Erreur] Asset : " + key);
	                    e.printStackTrace();
	                }
	            }
	        }
    	} catch (Exception e) {
    		e.printStackTrace();
    	}
    }
    
    public String owner = "TheAltening156";
    public String repo = "Honertis";   
    
    private String buildClasspath(String selectedVersion) {
        File libDir = new File(workdir, "lib");
        File zipFile = new File(workdir, "Honertis" + selectedVersion + ".zip");
        
        libDir.mkdirs();
        
        try {
			Utils.download("https://github.com/" + owner + "/" + repo +"/releases/download/" + selectedVersion + "/Honertis." + selectedVersion + ".zip",
						   zipFile);
		} catch (IOException e) {
			 System.err.println("[Erreur] Impossible d'installer la version : Honertis" + selectedVersion + ".zip");
			 e.printStackTrace();
			 return null;
		}
        
        try {
			extractFromZip(zipFile, libDir, "Honertis " + selectedVersion + "/Honertis " + selectedVersion + ".jar");
			zipFile.delete();
        } catch (IOException e) {
			e.printStackTrace();
		}
        
        ArrayList<String> classpathElements = new ArrayList<String>();
        try (Stream<Path> paths = Files.walk(libDir.toPath())) {
        	paths.filter(Files::isRegularFile)
        		 .filter(p -> p.getFileName().toString().endsWith(".jar"))
        		 .forEach(path -> {
        			 String name = path.getFileName().toString();
        			 
        			 boolean isHonertis = name.startsWith("Honertis ");
        			 boolean isCorrectVersion = name.contains("Honertis " + selectedVersion);

        			 if (!isHonertis || isCorrectVersion) {
        				 classpathElements.add("" + path.toAbsolutePath());
        			 }
        		 });
        } catch (Exception e) {
        	e.printStackTrace();
        }
        return String.join(File.pathSeparator, classpathElements);
    }
    
    public static void extractFromZip(File zipFilePath, File outputDir, String specificFile) throws IOException {
    	try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
    		ZipEntry entry;
    		
    		byte[] buffer = new byte[8192];
    		while((entry = zis.getNextEntry()) != null) {
    			if (entry.isDirectory()) {
    				zis.closeEntry();
    				continue;
    			}
    			
    			if (specificFile != null && !entry.getName().equals(specificFile)) {
    				zis.closeEntry();
    				continue;
    			}
    			
    			File outFile = (specificFile != null) ?
    							new File(outputDir, specificFile) : 
    							new File(outputDir, entry.getName());
    			outFile.getParentFile().mkdirs();
    			
    			try (FileOutputStream fos = new FileOutputStream(outFile)){
    				int len;
    				while ((len = zis.read(buffer)) > 0) {
    					fos.write(buffer, 0, len);
    				}
    			}
    			zis.closeEntry();
    			if (specificFile != null) {
    				return;
    			}
    		}
    	}
    	if (specificFile != null) {
    		throw new FileNotFoundException("Fichier " + specificFile + " introuvable dans le zip.");
    	}
    }
    
    private void launch(Auth auth, String version) {
    	new Thread(() -> {
	    	File json = new File(workdir, "versions/1.8.8");
			Utils.download("https://pixelpc.fr/honertis/1.8.8.json", json, "1.8.8.json");
			try {
				downloadLibraries();
				downloadAssets();
				downloadNatives();
			} catch (Exception e) {
				e.printStackTrace();
			}
	    	
			window.setVisible(false);
			
	    	System.out.println("[Launcher] Version sélectionnée : " + version);
	    	String classpath = buildClasspath(version);
	    	System.out.println("[Launcher] Classpath utilisé : " + classpath);

	        ProcessBuilder builder = new ProcessBuilder(
	            "java",
	            "-Djava.library.path=" + workdir.getPath() + "/natives", 
	            "-Djava.net.preferIPv4Stack=true",
	            "-cp", classpath, "net.minecraft.client.main.Main", 
	            "--version", "1.8.8", 
	            "--gameDir", workdir.getPath(), 
	            "--assetsDir", "assets", 
	            "--assetIndex", "1.8", 
	            "--accessToken", auth.getAccessToken(), 
	            "--username", auth.getUsername(), 
	            "--uuid", auth.isMicrosoftAccount() ? auth.getUuid() : "", 
	            "--userProperties", "{}",
	            "--launcherVersion", launcherVersion
	        );
	        	        
	        builder.directory(workdir);
	        builder.inheritIO();
	        Process process= null;
	        try {
	        	process = builder.start();
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
	        window.setVisible(true);
		}).start();
    	if (auth == null) {
    		nameField.setEnabled(true);
    	}
    	launchButton.setEnabled(true);

    }
    private String[] getTagsVersions() {
        try {
            String urlString = String.format("https://api.github.com/repos/%s/%s/tags", owner, repo);
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/vnd.github.v3+json");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                StringBuilder content = new StringBuilder();
                String inputLine;
                while ((inputLine = in.readLine()) != null) content.append(inputLine);
                in.close();
                conn.disconnect();

                JsonArray tagsArray = JsonParser.parseString(content.toString()).getAsJsonArray();
                List<String> tagList = new ArrayList<String>();
                for (JsonElement tagElement : tagsArray) {
                    JsonObject tagObj = tagElement.getAsJsonObject();
                    String tagName = tagObj.get("name").getAsString();
                    if (tagName.contains("Honertis") || tagName.equals("1.3")) continue;

                    tagList.add(tagName);
                }

                return tagList.toArray(new String[] {"1.8"});
            } else {
                System.out.println("[Launcher] Failed to fetch tags. Response code: " + responseCode);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new String[] {"1.8"};
    }

	public static void downloadNatives() {
        String os = detectOS();
        String urlString = String.format("https://pixelpc.fr/honertis/natives/%s/natives.zip", os);
        
        File destZip = new File("natives.zip");        
        File nativesDir = new File(workdir.getAbsolutePath() + "/natives");
        
        try {
            Utils.download(urlString, destZip);
            nativesDir.mkdirs();
            
            extractFromZip(destZip, nativesDir, null);
            
            destZip.delete();
            System.out.println("[Launcher] Natives téléchargés et prêts !");
        } catch (Exception e) {
        	e.printStackTrace();
        }
	}

	public static String detectOS() {
	    switch (Utils.getOSType()) {
    		case WINDOWS:
    			return "windows";
    		case LINUX:
    			return "linux";
    		case MACOS:
    			return "macos";
    		case SOLARIS:
    			throw new RuntimeErrorException(new Error("OS Not suported."));
    		case UNKNOWN:
    			throw new RuntimeErrorException(new Error("OS Not suported."));
		}
	    return null;
    }
	
}
