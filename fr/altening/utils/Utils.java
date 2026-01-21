package fr.altening.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.management.RuntimeErrorException;

import org.apache.commons.io.FileUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import fr.altening.AccountData;
import fr.altening.launcher.Auth;
import fr.altening.launcher.Main;

public class Utils {
	public static File workdir = new File(Utils.getAppData() + "/.honertis");
	public static File jsonFile = new File(workdir, "versions/1.8.8/1.8.8.json");

	public static void downloadNatives() {
		String os = detectOS();
		String urlString = String.format("https://pixelpc.fr/honertis/natives/%s/natives.zip", os);

		File destZip = new File("natives.zip");
		File nativesDir = new File(workdir.getAbsolutePath() + "/natives");

		try {
			download(urlString, destZip);
			nativesDir.mkdirs();

			extractFromZip(destZip, nativesDir, null);

			destZip.delete();
			System.out.println("[Launcher] Natives téléchargés et prêts !");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void downloadLibraries() {
		try {
			if (!jsonFile.exists())
				throw new FileNotFoundException("1.8.8.json manquant");
			JSONObject root = new JSONObject(new String(Files.readAllBytes(jsonFile.toPath()), StandardCharsets.UTF_8));
			JSONArray libraries = root.getJSONArray("libraries");

			File libDir = new File(workdir, "lib");
			if (!libDir.exists())
				libDir.mkdirs();

			for (int i = 0; i < libraries.length(); i++) {
				JSONObject lib = libraries.getJSONObject(i);
				String name = lib.getString("name");

				// Ex: "com.google.code.gson:gson:2.2.4"
				String[] parts = name.split(":");
				if (parts.length < 3)
					continue;

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
						download(downloadUrl, dest);
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
					download(assetIndexUrl, indexFile);
					System.out.println("[OK] Index assets téléchargé.");
				} catch (IOException e) {
					System.err.println("[Erreur] Impossible de télécharger les index assets téléchargé.");
					e.printStackTrace();
				}
			}

			JSONObject index = new JSONObject(
					new String(Files.readAllBytes(indexFile.toPath()), StandardCharsets.UTF_8))
					.getJSONObject("objects");
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
						download(url, assetFile);
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

	public static void extractFromZip(File zipFilePath, File outputDir, String specificFile) throws IOException {
		try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
			ZipEntry entry;

			byte[] buffer = new byte[8192];
			while ((entry = zis.getNextEntry()) != null) {
				if (entry.isDirectory()) {
					zis.closeEntry();
					continue;
				}

				if (specificFile != null && !entry.getName().equals(specificFile)) {
					zis.closeEntry();
					continue;
				}

				File outFile = (specificFile != null) ? new File(outputDir, specificFile)
						: new File(outputDir, entry.getName());
				outFile.getParentFile().mkdirs();

				try (FileOutputStream fos = new FileOutputStream(outFile)) {
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

	public static String owner = "TheAltening156";
	public static String repo = "Honertis";

	public static String buildClasspath(String selectedVersion) {
		File libDir = new File(workdir, "lib");
		File zipFile = new File(workdir, "Honertis" + selectedVersion + ".zip");

		libDir.mkdirs();

		try {
			Utils.download("https://github.com/" + owner + "/" + repo + "/releases/download/" + selectedVersion
					+ "/Honertis." + selectedVersion + ".zip", zipFile);
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
			paths.filter(Files::isRegularFile).filter(p -> p.getFileName().toString().endsWith(".jar"))
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

	public static void launch(Auth auth, String version) {
		new Thread(() -> {
			File json = new File(workdir, "versions/1.8.8");
			download("https://pixelpc.fr/honertis/1.8.8.json", json, "1.8.8.json");
			try {
				downloadLibraries();
				downloadAssets();
				downloadNatives();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Main.window.setVisible(false);

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
		            "--launcherVersion", Main.launcherVersion
		        );
			builder.directory(workdir);
			builder.inheritIO();
			Process process = null;
			try {
				process = builder.start();
				process.waitFor();
			} catch (IOException | InterruptedException e) {
				e.printStackTrace();
			}
			Main.window.setVisible(true);
		}).start();
		if (auth == null) {
			Main.nameField.setEnabled(true);
		}
		Main.launchButton.setEnabled(true);
	}
	
	public static String[] getTagsVersions() {
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
				while ((inputLine = in.readLine()) != null)
					content.append(inputLine);
				in.close();
				conn.disconnect();

				JsonArray tagsArray = JsonParser.parseString(content.toString()).getAsJsonArray();
				List<String> tagList = new ArrayList<String>();
				for (JsonElement tagElement : tagsArray) {
					JsonObject tagObj = tagElement.getAsJsonObject();
					String tagName = tagObj.get("name").getAsString();
					if (tagName.contains("Honertis") || tagName.equals("1.3"))
						continue;

					tagList.add(tagName);
				}
				tagList.sort((a, b) -> {
					Version va = Version.parse(a);
					Version vb = Version.parse(b);
					if (va.major != vb.major) return Integer.compare(vb.major, vb.minor);
					if (va.minor != vb.minor) return Integer.compare(vb.minor, vb.minor);
					return Integer.compare(vb.update, va.update);
				});
				return tagList.toArray(new String[] { "1.8U1" });
			} else {
				System.out.println("[Launcher] Failed to fetch tags. Response code: " + responseCode);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return new String[] { "1.8U1" };
	}
	
	static class Version {
	    int major;
	    int minor;
	    int update;

	    static Version parse(String s) {
	        Version v = new Version();

	        String[] parts = s.split("U");
	        String[] main = parts[0].split("\\.");

	        v.major = Integer.parseInt(main[0]);
	        v.minor = main.length > 1 ? Integer.parseInt(main[1]) : 0;
	        v.update = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;

	        return v;
	    }
	}

	public static boolean isValidMinecraftUsername(String username) {
		return username.matches("^[a-zA-Z0-9_]{3,16}$");
	}

	public static void download(String url, File dest) throws IOException {
		try (InputStream in = new URL(url).openStream()) {
			Files.copy(in, dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
		}
	}

	public static void download(String url, File directory, String name) {
		try {
			saveFileFromUrlWithCommonsIO(String.valueOf(directory.toString()) + "/" + name, url);
			System.out.println("[Launcher] Finished downloading " + name);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void saveFileFromUrlWithCommonsIO(String fileName, String fileUrl)
			throws MalformedURLException, IOException {
		FileUtils.copyURLToFile(new URL(fileUrl), new File(fileName));
	}

	public enum EnumOS {
		WINDOWS, MACOS, LINUX, SOLARIS, UNKNOWN;
	}

	public static EnumOS getOSType() {
		String s = System.getProperty("os.name").toLowerCase();
		return s.contains("win") ? EnumOS.WINDOWS
				: (s.contains("mac") ? EnumOS.MACOS
						: (s.contains("solaris") ? EnumOS.SOLARIS
								: (s.contains("sunos") ? EnumOS.SOLARIS
										: (s.contains("linux") ? EnumOS.LINUX
												: (s.contains("unix") ? EnumOS.LINUX : EnumOS.UNKNOWN)))));
	}

	public static String detectOS() {
		switch (getOSType()) {
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

	public static File getAppData() {
		EnumOS os = getOSType();
		if (os == EnumOS.WINDOWS)
			return new File(System.getenv("APPDATA"));
		if (os == EnumOS.MACOS)
			return new File(System.getProperty("user.home") + "/Library/Application Support");
		return new File(System.getProperty("user.home"));
	}
	

    public static File accountJson = new File(Utils.workdir, "accounts.json");

	public static void saveAccount(String username, String refreshToken) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (Writer writer = new FileWriter(accountJson)) {
        	gson.toJson(new AccountData(username, refreshToken), writer);
        }
    }
	
	public static AccountData loadAccount() throws IOException {
	    if (!accountJson.exists()) return null;
	    
	    Gson gson = new Gson();
	    
	    try (Reader reader = new FileReader(accountJson)) {
	    	return gson.fromJson(reader, AccountData.class);
	    }
	}

	
}
